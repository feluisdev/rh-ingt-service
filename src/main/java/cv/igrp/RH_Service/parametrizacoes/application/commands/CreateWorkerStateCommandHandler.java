package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.domain.models.WorkerState;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.WorkerStateRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class CreateWorkerStateCommandHandler implements CommandHandler<CreateWorkerStateCommand, ResponseEntity<Map<String, ?>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateWorkerStateCommandHandler.class);

    private final WorkerStateRepository workerStateRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(CreateWorkerStateCommand command) {
        var dto = command.getWorkerStateRequest();

        if (workerStateRepository.existsByCode(dto.getCode())) {
            throw IgrpResponseStatusException.conflict(
                "Já existe um registo com code='" + dto.getCode() + "'.");
        }

        WorkerState saved = workerStateRepository.save(
            WorkerState.criar(dto.getCode(), dto.getDescription(), dto.getIsCore())
        );

        return ResponseEntity.status(201).body(Map.of(
            "id", saved.getId().getStringValor(),
            "message", "Criado com sucesso"
        ));
    }
}
