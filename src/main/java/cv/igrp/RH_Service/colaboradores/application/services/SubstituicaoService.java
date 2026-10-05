package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.ReservaLugarRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.AssignmentId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.PositionId;
import cv.igrp.RH_Service.parametrizacoes.domain.models.SituacaoFuncional;
import cv.igrp.RH_Service.parametrizacoes.domain.models.WorkerState;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.WorkerStateRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.WorkerStateId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Substituição de funcionário <b>temporariamente impedido</b> — contrato a termo do
 * art. 73.º al. a) a c) e nomeação em substituição do art. 91.º n.º 1 al. a) da
 * Lei n.º 20/X/2023.
 *
 * <p>O substituto entra no Lugar <b>sem desalojar o titular</b>: as duas afectações
 * coexistem, a do titular a título {@code PRINCIPAL} e a do substituto a título
 * {@code SUBSTITUICAO}. É isso que o índice parcial da V45 passou a permitir, e a
 * ligação entre elas ({@code titular_assignment_id}, V46) é o que diz quando a
 * substituição acaba: o vínculo do substituto <b>caduca quando cessa a situação que
 * o justificou</b> (art. 77.º n.º 2), e não numa data combinada à parte.
 *
 * <p>Quem decide se o titular está impedido não é este serviço nem uma lista de
 * códigos: é a <b>situação funcional</b> do estado em que ele se encontra, pela
 * regra do art. 117.º — ver {@link SituacaoFuncional#permiteSubstituicao()}.
 *
 * <p>Fora do âmbito: férias e faltas curtas, que não dão lugar a substituição.
 */
@Service
@RequiredArgsConstructor
public class SubstituicaoService {

    private final AssignmentRepository assignmentRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final PositionRepository positionRepository;
    private final WorkerStateRepository workerStateRepository;
    private final AssignmentService assignmentService;
    private final ReservaLugarRepository reservaLugarRepository;

    /** O que a substituição produziu: a afectação nova, o Lugar, e quem está a ser substituído. */
    public record Substituicao(Assignment afectacao, Position lugar,
                               Assignment afectacaoTitular, Funcionario titular) {}

    /**
     * Põe {@code substitutoId} a substituir o titular do Lugar {@code positionId}.
     *
     * <p>O Lugar identifica-se pelo próprio Lugar e não pelo titular: é o Lugar que fica
     * por exercer, e é nele que o substituto entra.
     */
    @Transactional
    public Substituicao substituir(FuncionarioId substitutoId, UUID positionId, UUID gradeId,
                                   UUID functionId, LocalDate dataInicio, String notes) {

        Position lugar = positionRepository.findById(PositionId.from(positionId))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Lugar não encontrado: " + positionId));

        // BR-AF-27: quem tem Lugar reservado ainda não tem contrato — não ocupa Lugar nenhum, nem a substituir
        if (reservaLugarRepository.findActivaByFuncionario(substitutoId).isPresent())
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Este colaborador tem um Lugar reservado e ainda não tem contrato. Registe primeiro o contrato.");

        Assignment afectacaoTitular = assignmentRepository.findTitularByPosition(positionId)
                .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                        "O Lugar '" + lugar.getNumeroLugar() + "' não tem titular — está vago. "
                                + "Um Lugar vago provê-se com um titular, não com um substituto."));

        if (afectacaoTitular.getFuncionarioId().equals(substitutoId))
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "O colaborador é o titular do Lugar — não se substitui a si próprio.");

        Funcionario titular = funcionarioRepository.findById(afectacaoTitular.getFuncionarioId())
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Titular do Lugar não encontrado: " + afectacaoTitular.getFuncionarioId().getStringValor()));

        exigirTitularImpedido(titular, lugar);

        assignmentRepository.findSubstitutoCorrente(afectacaoTitular.getId()).ifPresent(s -> {
            throw IgrpResponseStatusException.conflict(
                    "O titular do Lugar '" + lugar.getNumeroLugar() + "' já está a ser substituído.");
        });

        if (dataInicio.isBefore(afectacaoTitular.getDataInicio()))
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "A substituição não pode começar antes da afectação do titular ("
                            + afectacaoTitular.getDataInicio() + ").");

        Assignment nova = assignmentService.afectarSubstituicao(
                substitutoId, positionId, gradeId, functionId,
                afectacaoTitular.getId(), dataInicio, notes);

        return new Substituicao(nova, lugar, afectacaoTitular, titular);
    }

    /**
     * Encerra as substituições em vigor da afectação de um titular, por o impedimento ter
     * cessado. Idempotente: sem substituição em vigor, não faz nada — é chamado do regresso
     * do titular, que acontece quer haja substituto quer não.
     */
    @Transactional
    public List<Assignment> encerrarPorRegressoDoTitular(AssignmentId titularAssignmentId, LocalDate dataFim) {
        return assignmentRepository.findSubstituicoesCorrentes(titularAssignmentId).stream()
                .map(s -> {
                    s.encerrar(dataFim);
                    return assignmentRepository.save(s);
                })
                .toList();
    }

    /**
     * Encerra as substituições em vigor do Lugar de um funcionário que regressa. Atalho para
     * quem só tem o funcionário à mão — o caso do regresso de licença e da mudança de estado.
     */
    @Transactional
    public List<Assignment> encerrarPorRegressoDoTitular(FuncionarioId titularId, LocalDate dataFim) {
        return assignmentRepository.findCurrentPrincipalByFuncionario(titularId)
                .map(a -> encerrarPorRegressoDoTitular(a.getId(), dataFim))
                .orElseGet(List::of);
    }

    /**
     * Só se substitui quem mantém o Lugar e não o exerce (art. 119.º e 120.º). Quem está em
     * actividade no quadro está a exercer; quem já perdeu o Lugar não tem nada a substituir,
     * e nesse caso o Lugar está vago e nomeia-se titular.
     */
    private void exigirTitularImpedido(Funcionario titular, Position lugar) {
        Optional<SituacaoFuncional> situacao = situacaoDe(titular);

        if (situacao.isEmpty())
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "O estado do titular do Lugar '" + lugar.getNumeroLugar() + "' não tem situação funcional "
                            + "atribuída — sem ela não se sabe se está impedido. Classifique o estado no catálogo.");

        if (!situacao.get().permiteSubstituicao())
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "O titular do Lugar '" + lugar.getNumeroLugar() + "' está em " + situacao.get()
                            + " — só se substitui quem está temporariamente impedido, isto é, quem mantém "
                            + "o Lugar sem o exercer (art. 119.º e 120.º).");
    }

    private Optional<SituacaoFuncional> situacaoDe(Funcionario funcionario) {
        UUID workerStateId = funcionario.getWorkerStateId();
        if (workerStateId == null) return Optional.empty();
        return workerStateRepository.findById(WorkerStateId.from(workerStateId))
                .flatMap(WorkerState::situacao);
    }
}
