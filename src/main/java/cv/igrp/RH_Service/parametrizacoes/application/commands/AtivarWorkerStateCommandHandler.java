package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.domain.models.WorkerState;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.WorkerStateRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
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
public class AtivarWorkerStateCommandHandler implements CommandHandler<AtivarWorkerStateCommand, ResponseEntity<Map<String, ?>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AtivarWorkerStateCommandHandler.class);

    private final WorkerStateRepository workerStateRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(AtivarWorkerStateCommand command) {
        var id = ExternalID.from(UUID.fromString(command.getWorkerStateId()));

        WorkerState workerState = workerStateRepository.findById(id)
            .orElseThrow(() -> IgrpResponseStatusException.notFound(
                "Não encontrado: " + command.getWorkerStateId()));

        workerState.reativar();
        workerStateRepository.save(workerState);

        return ResponseEntity.ok(Map.of("message", "Ativado com sucesso"));
    }
}
