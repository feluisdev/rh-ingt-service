package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.domain.models.HistoricoEstadoColaborador;
import cv.igrp.RH_Service.colaboradores.domain.models.LicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.models.SubtipoLicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.HistoricoEstadoColaboradorRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.parametrizacoes.domain.models.SituacaoFuncional;
import cv.igrp.RH_Service.parametrizacoes.domain.models.WorkerState;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.WorkerStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Efeitos da <b>licença</b> no Lugar do funcionário (DL n.º 3/2010).
 *
 * <p>Nem todas as licenças são iguais. A licença sem vencimento até três anos mantém
 * o lugar (art. 46.º e 48.º), mas a de longa duração abre vaga e suspende o vínculo
 * (art. 50.º a 53.º); outras abrem vaga só a partir de um prazo — cônjuge no
 * estrangeiro além de um ano (art. 56.º n.º 2), formação além de seis meses
 * (art. 67.º n.º 3), tal como a Lei n.º 20/X/2023 diz no art. 118.º n.º 2.
 *
 * <p>Qual é qual não está aqui: está no subtipo ({@code position_effect},
 * {@code vacancy_after_days}, {@code return_effect} — V43). Este serviço limita-se a
 * aplicar o que o catálogo diz, e a escolher o estado do trabalhador <b>pela situação
 * da lei</b> em vez de por códigos escritos no código.
 *
 * <p>A mobilidade não passa por aqui: mantém sempre o Lugar (art. 135.º n.º 7) e tem o
 * seu próprio serviço, o {@link MobilidadeService}.
 */
@Service
@RequiredArgsConstructor
public class LicencaService {

    private final AssignmentService assignmentService;
    private final FuncionarioRepository funcionarioRepository;
    private final WorkerStateRepository workerStateRepository;
    private final HistoricoEstadoColaboradorRepository historicoRepository;

    /** O que a entrada em vigor produziu: afectação encerrada e estado atribuído. */
    public record EfeitoAplicado(UUID afectacaoEncerradaId, UUID estadoAtribuidoId) {
        public static EfeitoAplicado nenhum() { return new EfeitoAplicado(null, null); }
    }

    /**
     * Põe a licença em vigor. Se o subtipo abrir vaga para esta duração, encerra a
     * afectação corrente — o Lugar fica vago — e passa o funcionário à inactividade
     * fora do quadro (art. 121.º), se a instituição tiver esse estado no catálogo.
     */
    @Transactional
    public EfeitoAplicado aplicarEntradaEmVigor(LicencaMobilidade licenca, SubtipoLicencaMobilidade subtipo) {
        if (subtipo.isMobilidade() || !subtipo.abreVaga(licenca.duracaoEmDias()))
            return EfeitoAplicado.nenhum();

        FuncionarioId funcionarioId = licenca.getFuncionarioId();

        UUID afectacaoEncerradaId = assignmentService
                .encerrarAfectacaoCorrente(funcionarioId, licenca.getDataInicio())
                .map(a -> a.getId().getValor())
                .orElse(null);

        UUID estadoId = passarASituacao(funcionarioId, SituacaoFuncional.INACTIVIDADE_FORA_QUADRO,
                licenca.getDataInicio(), subtipo.getCodigo()).orElse(null);

        return new EfeitoAplicado(afectacaoEncerradaId, estadoId);
    }

    /**
     * Regresso. Quem manteve o Lugar volta a ele e não há nada a fazer. Quem o perdeu
     * fica na <b>disponibilidade</b> (art. 122.º), a aguardar vaga na sua categoria,
     * com direito a contagem de tempo e abonos — a nova afectação é um acto do RH,
     * não um efeito automático, porque depende de haver Lugar livre.
     */
    @Transactional
    public Optional<UUID> aplicarRegresso(LicencaMobilidade licenca, SubtipoLicencaMobilidade subtipo,
                                          LocalDate dataRegresso) {
        if (subtipo.isMobilidade() || !subtipo.regressaEmDisponibilidade())
            return Optional.empty();

        return passarASituacao(licenca.getFuncionarioId(), SituacaoFuncional.DISPONIBILIDADE,
                dataRegresso, subtipo.getCodigo());
    }

    /**
     * Passa o funcionário ao primeiro estado activo classificado nesta situação. Se a
     * instituição não tiver nenhum, não faz nada: o catálogo é dela, e a licença não
     * deve ser recusada por causa disso.
     */
    private Optional<UUID> passarASituacao(FuncionarioId funcionarioId, SituacaoFuncional situacao,
                                           LocalDate data, String motivo) {
        Optional<WorkerState> destino = workerStateRepository.findBySituacao(situacao);
        if (destino.isEmpty()) return Optional.empty();

        WorkerState estado = destino.get();

        return funcionarioRepository.findById(funcionarioId).map(funcionario -> {
            UUID estadoAnteriorId = funcionario.getWorkerStateId();
            if (estado.getId().getValor().equals(estadoAnteriorId)) return estado.getId().getValor();

            funcionario.atualizarWorkerState(estado.getId().getValor(), true);
            funcionarioRepository.save(funcionario);

            historicoRepository.save(HistoricoEstadoColaborador.criar(
                    funcionarioId, estadoAnteriorId, estado.getId().getValor(),
                    motivo, data, null));

            return estado.getId().getValor();
        });
    }
}
