package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.EstadoColaboradorResponseDTO;
import cv.igrp.RH_Service.colaboradores.application.services.CessacaoService;
import cv.igrp.RH_Service.colaboradores.domain.models.Contrato;
import cv.igrp.RH_Service.colaboradores.domain.models.HistoricoEstadoColaborador;
import cv.igrp.RH_Service.colaboradores.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.HistoricoEstadoColaboradorRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
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
 * o vínculo, partilhado com o encerramento de contrato. Nos restantes estados aplica apenas
 * os efeitos no contrato corrente (suspender/reactivar) e regista o histórico.
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

        funcionario.atualizarWorkerState(novoEstado.getId().getValor(), true);
        funcionarioRepository.save(funcionario);

        UUID contratoAfectadoId = aplicarEfeitosContrato(funcionarioId, novoEstado.getCode());

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
                null));
    }

    /**
     * Efeitos no contrato corrente dos estados que <b>não</b> cessam o vínculo: suspender e
     * reactivar. A cessação do contrato vive no {@link CessacaoService}.
     */
    private UUID aplicarEfeitosContrato(FuncionarioId funcionarioId, String novoCode) {
        var contratoOpt = contratoRepository.findCurrentByFuncionarioId(funcionarioId);
        if (contratoOpt.isEmpty()) return null;

        var contrato = contratoOpt.get();
        switch (novoCode) {
            case "SUSPENDED" -> {
                if (Contrato.ATIVO.equals(contrato.getStatus())) {
                    contrato.suspender();
                    contratoRepository.save(contrato);
                }
            }
            case "ACTIVE" -> {
                if (Contrato.SUSPENSO.equals(contrato.getStatus())) {
                    contrato.reativar();
                    contratoRepository.save(contrato);
                }
            }
            default -> { /* estados sem efeito no contrato */ }
        }
        return contrato.getId().getValor();
    }

    private static String texto(UUID valor) {
        return valor == null ? null : valor.toString();
    }
}
