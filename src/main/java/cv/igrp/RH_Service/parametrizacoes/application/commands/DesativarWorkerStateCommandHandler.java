package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.domain.models.WorkerState;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.WorkerStateRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.WorkerStateId;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DesativarWorkerStateCommandHandler implements CommandHandler<DesativarWorkerStateCommand, ResponseEntity<Map<String, ?>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DesativarWorkerStateCommandHandler.class);

    private final WorkerStateRepository workerStateRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(DesativarWorkerStateCommand command) {
        var id = WorkerStateId.from(UUID.fromString(command.getWorkerStateId()));

        WorkerState workerState = workerStateRepository.findById(id)
            .orElseThrow(() -> IgrpResponseStatusException.notFound(
                "Não encontrado: " + command.getWorkerStateId()));

        workerState.desativar();
        workerStateRepository.save(workerState);

        return ResponseEntity.ok(Map.of("message", "Desativado com sucesso"));
    }
}
