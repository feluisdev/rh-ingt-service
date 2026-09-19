package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.domain.models.Contrato;
import cv.igrp.RH_Service.colaboradores.domain.models.EstadoContrato;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.HistoricoEstadoColaborador;
import cv.igrp.RH_Service.colaboradores.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.HistoricoEstadoColaboradorRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.parametrizacoes.domain.models.WorkerState;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.WorkerStateRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Cessação da relação de emprego público — o <b>único</b> caminho para terminar o vínculo
 * (Lei n.º 20/X/2023, art. 93.º, 96.º e 97.º: exoneração, aposentação, mútuo acordo, caducidade,
 * pena disciplinar expulsiva, abandono de lugar…).
 *
 * <p>A cessação é um só acontecimento com uma causa e uma data de produção de efeitos, e produz
 * sempre os mesmos quatro efeitos. Antes havia dois caminhos (mudar estado e encerrar contrato)
 * que faziam coisas diferentes — o segundo deixava o trabalhador ACTIVE, sem contrato e sem Lugar,
 * e sem registo no histórico de estados.
 *
 * <p>Que estados cessam o vínculo é <b>parametrizado</b> (<code>t_worker_state.ends_employment</code>),
 * não escrito no código. O motivo é texto livre, deliberadamente: os motivos são parametrizados
 * pelo utilizador no catálogo <code>Option</code> (<code>WORKER_STATE_REASON</code>).
 */
@Service
@RequiredArgsConstructor
public class CessacaoService {

    private final FuncionarioRepository funcionarioRepository;
    private final ContratoRepository contratoRepository;
    private final WorkerStateRepository workerStateRepository;
    private final HistoricoEstadoColaboradorRepository historicoRepository;
    private final AssignmentService assignmentService;
    private final SubstituicaoService substituicaoService;

    /**
     * Resultado da cessação: o estado atribuído, o contrato cessado (se havia) e se a afectação
     * corrente foi encerrada — o Lugar volta a vago.
     */
    public record Cessacao(Funcionario funcionario, WorkerState estado, UUID estadoAnteriorId,
                           UUID contratoCessadoId, UUID afectacaoEncerradaId, LocalDate dataEfeito) {}

    /**
     * Cessa a relação de emprego público na data indicada.
     *
     * @param estadoCessacao estado de destino; tem de ser um estado de cessação
     *                       ({@code ends_employment = true}) e estar activo no catálogo
     * @param motivo         causa da cessação (chave do catálogo de motivos); livre
     */
    @Transactional
    public Cessacao cessar(FuncionarioId funcionarioId, WorkerState estadoCessacao, LocalDate dataEfeito,
                           String motivo, String observacao) {

        if (!estadoCessacao.isEndsEmployment())
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "O estado '" + estadoCessacao.getCode() + "' não termina a relação de emprego público.");

        Funcionario funcionario = funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Funcionário não encontrado: " + funcionarioId.getStringValor()));

        UUID estadoAnteriorId = funcionario.getWorkerStateId();

        // 1. Contrato corrente → CESSADO (se ainda não estiver)
        UUID contratoCessadoId = contratoRepository.findCurrentByFuncionarioId(funcionarioId)
                .map(contrato -> {
                    if (contrato.getStatus() != EstadoContrato.CESSADO) {
                        contrato.encerrar(dataEfeito, motivo != null ? motivo : estadoCessacao.getCode());
                        contratoRepository.save(contrato);
                    }
                    return contrato.getId().getValor();
                })
                .orElse(null);

        // 2. Substituições do seu Lugar encerradas, antes de o Lugar ficar vago: quem cessa
        // deixa de ter impedimento a cobrir (art. 77.º n.º 2). Tem de ser por esta ordem —
        // depois de a afectação do titular fechar, já não há por onde as encontrar.
        substituicaoService.encerrarPorRegressoDoTitular(funcionarioId, dataEfeito);

        // 3. Afectação corrente encerrada — o Lugar volta a vago (derivado)
        UUID afectacaoEncerradaId = assignmentService.encerrarAfectacaoCorrente(funcionarioId, dataEfeito)
                .map(a -> a.getId().getValor())
                .orElse(null);

        // 4. Estado do trabalhador
        funcionario.atualizarWorkerState(estadoCessacao.getId().getValor(), false);
        funcionarioRepository.save(funcionario);

        // 5. Histórico — a cessação fica sempre registada, venha de onde vier
        historicoRepository.save(HistoricoEstadoColaborador.criar(
                funcionarioId, estadoAnteriorId, estadoCessacao.getId().getValor(),
                motivo, dataEfeito, observacao));

        return new Cessacao(funcionario, estadoCessacao, estadoAnteriorId,
                contratoCessadoId, afectacaoEncerradaId, dataEfeito);
    }

    /**
     * Estado de cessação por omissão, para quem cessa sem escolher estado (encerramento de
     * contrato): o primeiro estado activo marcado como {@code ends_employment}, preferindo
     * {@code INACTIVE} quando existe.
     */
    public WorkerState estadoDeCessacaoPorOmissao() {
        var candidatos = workerStateRepository.findAllEndingEmployment();

        Optional<WorkerState> inactive = candidatos.stream()
                .filter(e -> "INACTIVE".equals(e.getCode()))
                .findFirst();

        return inactive.or(() -> candidatos.stream().findFirst())
                .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Não há nenhum estado de cessação configurado (t_worker_state.ends_employment). "
                                + "Configure um estado de cessação antes de encerrar o vínculo."));
    }
}
