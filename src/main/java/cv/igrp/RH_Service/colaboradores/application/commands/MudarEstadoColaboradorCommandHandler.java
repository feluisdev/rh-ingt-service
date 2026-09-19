package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.EstadoColaboradorResponseDTO;
import cv.igrp.RH_Service.colaboradores.application.services.AssignmentService;
import cv.igrp.RH_Service.colaboradores.application.services.CessacaoService;
import cv.igrp.RH_Service.colaboradores.application.services.SubstituicaoService;
import cv.igrp.RH_Service.colaboradores.domain.models.EstadoContrato;
import cv.igrp.RH_Service.colaboradores.domain.models.HistoricoEstadoColaborador;
import cv.igrp.RH_Service.colaboradores.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.HistoricoEstadoColaboradorRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.parametrizacoes.domain.models.SituacaoFuncional;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.WorkerStateRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.WorkerStateId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Mudança de estado do trabalhador. Quando o estado de destino é de <b>cessação</b>
 * ({@code ends_employment}), delega no {@link CessacaoService} — o único caminho que termina
 * o vínculo, partilhado com o encerramento de contrato.
 *
 * <p>Nos restantes estados os efeitos derivam da <b>situação administrativa</b> do estado de
 * destino (Lei n.º 20/X/2023, art. 117.º), parametrizada em
 * {@code t_worker_state.situacao_funcional} (V42) e não escrita no código:
 * a inactividade suspende o contrato (art. 120.º e 121.º), a actividade e a disponibilidade
 * reactivam-no, e a inactividade <b>fora</b> do quadro encerra ainda a afectação corrente,
 * porque abre vaga (art. 121.º n.º 2). Um estado sem situação classificada só regista histórico.
 */
@Component
@RequiredArgsConstructor
public class MudarEstadoColaboradorCommandHandler
        implements CommandHandler<MudarEstadoColaboradorCommand, ResponseEntity<EstadoColaboradorResponseDTO>> {

    private final FuncionarioRepository funcionarioRepository;
    private final WorkerStateRepository workerStateRepository;
    private final ContratoRepository contratoRepository;
    private final HistoricoEstadoColaboradorRepository historicoRepository;
    private final CessacaoService cessacaoService;
    private final AssignmentService assignmentService;
    private final SubstituicaoService substituicaoService;

    @IgrpCommandHandler
    @Transactional
    public ResponseEntity<EstadoColaboradorResponseDTO> handle(MudarEstadoColaboradorCommand command) {
        var req = command.getRequest();
        var funcionarioId = FuncionarioId.from(command.getFuncionarioId());

        var funcionario = funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Funcionário não encontrado: " + command.getFuncionarioId()));

        var novoEstado = workerStateRepository
                .findById(WorkerStateId.from(UUID.fromString(req.getWorkerStateId())))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Estado não encontrado: " + req.getWorkerStateId()));

        if (!novoEstado.isActive()) {
            throw IgrpResponseStatusException.conflict(
                    "O estado '" + novoEstado.getCode() + "' está inactivo e não pode ser atribuído.");
        }

        UUID estadoAnteriorId = funcionario.getWorkerStateId();
        if (estadoAnteriorId != null && estadoAnteriorId.toString().equals(req.getWorkerStateId())) {
            throw IgrpResponseStatusException.conflict("O colaborador já se encontra nesse estado.");
        }

        LocalDate dataEfectividade = req.getDataEfectividade() != null ? req.getDataEfectividade() : LocalDate.now();

        // Cessação: um só caminho, partilhado com o encerramento de contrato.
        if (novoEstado.isEndsEmployment()) {
            var cessacao = cessacaoService.cessar(funcionarioId, novoEstado, dataEfectividade,
                    req.getMotivoCkey(), req.getObservacao());

            return ResponseEntity.ok(new EstadoColaboradorResponseDTO(
                    funcionarioId.getStringValor(),
                    texto(cessacao.estadoAnteriorId()),
                    novoEstado.getId().getStringValor(),
                    novoEstado.getCode(),
                    dataEfectividade,
                    true,
                    texto(cessacao.contratoCessadoId()),
                    texto(cessacao.afectacaoEncerradaId())));
        }

        // O trabalhador continua activo enquanto não exercer funções: só a cessação o desactiva.
        funcionario.atualizarWorkerState(novoEstado.getId().getValor(), true);
        funcionarioRepository.save(funcionario);

        var situacao = novoEstado.situacao();
        UUID contratoAfectadoId = aplicarEfeitosContrato(funcionarioId, situacao.orElse(null));

        // Quem deixa de estar impedido retoma o seu Lugar, e a substituição que o cobria
        // caduca (art. 77.º n.º 2) -- não é preciso ninguém a ir fechá-la à mão. Vale para
        // qualquer saída do impedimento, e não só para o regresso à actividade no quadro:
        // se o titular passar a uma situação que abre vaga, a afectação dele é encerrada
        // logo a seguir, e uma substituição sem titular não tem o que substituir.
        if (situacao.filter(SituacaoFuncional::permiteSubstituicao).isEmpty())
            substituicaoService.encerrarPorRegressoDoTitular(funcionarioId, dataEfectividade);

        UUID afectacaoEncerradaId = situacao.filter(SituacaoFuncional::abreVaga)
                .flatMap(s -> assignmentService.encerrarAfectacaoCorrente(funcionarioId, dataEfectividade))
                .map(a -> a.getId().getValor())
                .orElse(null);

        historicoRepository.save(HistoricoEstadoColaborador.criar(
                funcionarioId, estadoAnteriorId, novoEstado.getId().getValor(),
                req.getMotivoCkey(), dataEfectividade, req.getObservacao()));

        return ResponseEntity.ok(new EstadoColaboradorResponseDTO(
                funcionarioId.getStringValor(),
                texto(estadoAnteriorId),
                novoEstado.getId().getStringValor(),
                novoEstado.getCode(),
                dataEfectividade,
                false,
                texto(contratoAfectadoId),
                texto(afectacaoEncerradaId)));
    }

    /**
     * Efeitos no contrato corrente dos estados que <b>não</b> cessam o vínculo: a inactividade
     * suspende-o, a actividade e a disponibilidade reactivam-no. Um estado sem situação
     * classificada não lhe toca. A cessação do contrato vive no {@link CessacaoService}.
     */
    private UUID aplicarEfeitosContrato(FuncionarioId funcionarioId, SituacaoFuncional situacao) {
        var contratoOpt = contratoRepository.findCurrentByFuncionarioId(funcionarioId);
        if (contratoOpt.isEmpty()) return null;

        var contrato = contratoOpt.get();
        if (situacao == null) return contrato.getId().getValor();

        if (situacao.suspendeVinculo()) {
            if (contrato.getStatus() == EstadoContrato.ATIVO) {
                contrato.suspender();
                contratoRepository.save(contrato);
            }
        } else if (contrato.getStatus() == EstadoContrato.SUSPENSO) {
            contrato.reativar();
            contratoRepository.save(contrato);
        }
        return contrato.getId().getValor();
    }

    private static String texto(UUID valor) {
        return valor == null ? null : valor.toString();
    }
}
