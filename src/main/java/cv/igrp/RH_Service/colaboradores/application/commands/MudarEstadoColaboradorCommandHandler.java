package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.models.HistoricoEstadoColaborador;
import cv.igrp.RH_Service.colaboradores.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.HistoricoEstadoColaboradorRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsColocacaoEntityRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.WorkerStateRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.WorkerStateId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MudarEstadoColaboradorCommandHandler
        implements CommandHandler<MudarEstadoColaboradorCommand, ResponseEntity<Map<String, ?>>> {

    private final FuncionarioRepository funcionarioRepository;
    private final WorkerStateRepository workerStateRepository;
    private final ContratoRepository contratoRepository;
    private final ColabsColocacaoEntityRepository colocacaoRepository;
    private final HistoricoEstadoColaboradorRepository historicoRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(MudarEstadoColaboradorCommand command) {
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
        String novoCode = novoEstado.getCode();

        boolean isActiveFlag = !"INACTIVE".equals(novoCode) && !"RETIRED".equals(novoCode);
        funcionario.atualizarWorkerState(novoEstado.getId().getValor(), isActiveFlag);
        funcionarioRepository.save(funcionario);

        aplicarEfeitosContrato(funcionarioId, novoCode, dataEfectividade, req.getMotivoCkey());
        if (!isActiveFlag) {
            colocacaoRepository.fecharColocacaoAtual(funcionarioId.getValor(), dataEfectividade);
        }

        var historico = HistoricoEstadoColaborador.criar(
                funcionarioId, estadoAnteriorId,
                novoEstado.getId().getValor(),
                req.getMotivoCkey(), dataEfectividade, req.getObservacao());
        historicoRepository.save(historico);

        return ResponseEntity.ok(Map.of("message", "Estado do colaborador actualizado com sucesso"));
    }

    private void aplicarEfeitosContrato(FuncionarioId funcionarioId, String novoCode,
                                         LocalDate dataEfectividade, String motivo) {
        var contratoOpt = contratoRepository.findCurrentByFuncionarioId(funcionarioId);
        if (contratoOpt.isEmpty()) return;

        var contrato = contratoOpt.get();
        switch (novoCode) {
            case "SUSPENDED" -> {
                if ("ATIVO".equals(contrato.getStatus())) {
                    contrato.suspender();
                    contratoRepository.save(contrato);
                }
            }
            case "RETIRED", "INACTIVE" -> {
                if (!"CESSADO".equals(contrato.getStatus())) {
                    contrato.encerrar(dataEfectividade, motivo != null ? motivo : novoCode);
                    contratoRepository.save(contrato);
                }
            }
            case "ACTIVE" -> {
                if ("SUSPENSO".equals(contrato.getStatus())) {
                    contrato.reativar();
                    contratoRepository.save(contrato);
                }
            }
        }
    }
}
