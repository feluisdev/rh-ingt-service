package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.domain.models.Contrato;
import cv.igrp.RH_Service.colaboradores.domain.models.EstadoContrato;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.ReservaLugar;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoAfectacao;
import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.ReservaLugarRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.PositionId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.Datas;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * <b>Reserva de Lugar</b> (BR-AF-23 a BR-AF-28): quem é registado sem contrato pode deixar escolhido o Lugar, que
 * fica guardado para si e é ocupado quando o contrato for registado. A reserva não é uma afectação — ver
 * {@link ReservaLugar}. A regra BR-AF-18 (sem vínculo não há Lugar) mantém-se inteira: a ocupação só acontece pela
 * {@link ColocacaoService}, já com o contrato.
 */
@Service
@RequiredArgsConstructor
public class ReservaLugarService {

    private final ReservaLugarRepository reservaRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final AssignmentRepository assignmentRepository;
    private final ContratoRepository contratoRepository;
    private final PositionRepository positionRepository;
    private final AssignmentService assignmentService;
    private final ColocacaoService colocacaoService;

    /**
     * BR-AF-23 / BR-AF-24: reserva o Lugar para um colaborador sem contrato em vigor e sem Lugar. O Lugar passa
     * pelas mesmas validações da colocação (ocupável, escalão da sua categoria, função do seu cargo), não pode ter
     * titular nem outra reserva, e o colaborador só tem uma reserva de cada vez.
     */
    public ReservaLugar reservar(FuncionarioId funcionarioId, UUID positionId, UUID gradeId, UUID functionId,
                                 TipoAfectacao tipo, String notes) {
        if (tipo != null && tipo != TipoAfectacao.PRINCIPAL)
            throw recusa("Para pôr alguém a substituir o titular de um Lugar, use a acção «Substituição».");

        Funcionario funcionario = funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("O colaborador indicado não existe."));
        if (!Boolean.TRUE.equals(funcionario.getIsActive()))
            throw recusa("Este colaborador não está activo e não pode ter um Lugar reservado.");
        if (contratoRepository.findCurrentByFuncionarioId(funcionarioId)
                .filter(c -> c.getStatus() == EstadoContrato.ATIVO).isPresent())
            throw recusa("Este colaborador já tem contrato em vigor: coloque-o directamente no Lugar, sem reserva.");
        if (assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId).isPresent())
            throw recusa("Este colaborador já ocupa um Lugar. Para o mudar de Lugar, use a transferência, a promoção "
                    + "ou a mudança de carreira.");
        if (reservaRepository.findActivaByFuncionario(funcionarioId).isPresent())
            throw recusa("Este colaborador já tem um Lugar reservado. Cancele essa reserva antes de reservar outro.");

        Position lugar = assignmentService.validarLugarParaAfectacao(positionId, gradeId, functionId);
        if (assignmentRepository.temTitular(positionId))
            throw recusa("O Lugar " + lugar.getNumeroLugar() + " já tem titular. Escolha um Lugar vago.");
        if (reservaRepository.findActivaByPosition(positionId).isPresent())
            throw recusa("O Lugar " + lugar.getNumeroLugar() + " já está reservado para outro colaborador. "
                    + "Escolha outro Lugar vago.");

        return reservaRepository.save(ReservaLugar.reservar(funcionarioId, positionId, gradeId, functionId, notes,
                LocalDate.now()));
    }

    /**
     * BR-AF-25: o contrato acabou de ser registado — a reserva passa a colocação, pela {@link ColocacaoService} e
     * com todas as suas regras. A colocação começa no início do contrato, ou na admissão se esta for posterior.
     *
     * <p><b>Não recusa o contrato.</b> Se a colocação falhar (o Lugar foi congelado entretanto, por exemplo), o
     * contrato fica registado, a reserva continua activa e devolve-se um alerta a dizer o que falta. Por isso este
     * método não é transaccional: a recusa da colocação é apanhada aqui e não pode marcar para anular a transacção
     * do contrato.
     *
     * @return os alertas para o utilizador; vazio se o colaborador não tinha Lugar reservado
     */
    public List<String> concretizar(FuncionarioId funcionarioId, Contrato contrato) {
        var reserva = reservaRepository.findActivaByFuncionario(funcionarioId);
        if (reserva.isEmpty() || contrato == null || contrato.getStatus() != EstadoContrato.ATIVO) return List.of();
        ReservaLugar r = reserva.get();
        String numero = numeroLugar(r.getPositionId());

        LocalDate inicio = contrato.getStartDate();
        LocalDate admissao = funcionarioRepository.findById(funcionarioId).map(Funcionario::getDataAdmissao).orElse(null);
        if (inicio == null || (admissao != null && admissao.isAfter(inicio))) inicio = admissao;
        if (inicio == null) inicio = LocalDate.now();

        List<String> alertas = new ArrayList<>();
        try {
            var resultado = colocacaoService.colocar(funcionarioId, r.getPositionId(), r.getGradeId(), r.getFunctionId(),
                    null, TipoAfectacao.PRINCIPAL, inicio, r.getNotes());
            alertas.add("O colaborador foi colocado no Lugar " + numero + ", que lhe estava reservado, a partir de "
                    + Datas.pt(inicio) + ".");
            alertas.addAll(resultado.alertas());
        } catch (IgrpResponseStatusException e) {
            alertas.add("O contrato foi registado, mas o colaborador ainda não foi colocado no Lugar " + numero
                    + " que lhe estava reservado: " + e.getBody().getTitle()
                    + " Resolva a situação e use «Colocar num Lugar», ou cancele a reserva.");
        }
        return alertas;
    }

    /** BR-AF-26: cancela a reserva activa do colaborador; o Lugar fica livre para outros. */
    public ReservaLugar cancelar(FuncionarioId funcionarioId, String motivo) {
        ReservaLugar r = reservaRepository.findActivaByFuncionario(funcionarioId)
                .orElseThrow(() -> recusa("Este colaborador não tem nenhum Lugar reservado."));
        r.cancelar(motivo, LocalDate.now());
        return reservaRepository.save(r);
    }

    private String numeroLugar(UUID positionId) {
        return positionRepository.findById(PositionId.from(positionId)).map(Position::getNumeroLugar).orElse("");
    }

    private static IgrpResponseStatusException recusa(String motivo) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, motivo);
    }
}
