package cv.igrp.RH_Service.estrutura.application.commands;

import cv.igrp.RH_Service.estrutura.domain.models.Job;
import cv.igrp.RH_Service.estrutura.domain.repository.JobRepository;
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
public class CreateJobCommandHandler
        implements CommandHandler<CreateJobCommand, ResponseEntity<Map<String, ?>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateJobCommandHandler.class);

    private final JobRepository jobRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(CreateJobCommand command) {
        var dto = command.getRequest();

        if (jobRepository.existsByCode(dto.getCode())) {
            throw IgrpResponseStatusException.conflict(
                    "Já existe um cargo com code='" + dto.getCode() + "'.");
        }

        Job saved = jobRepository.save(
                Job.criar(dto.getCode(), dto.getName(), dto.getDescription()));

        return ResponseEntity.status(201).body(Map.of(
                "id", saved.getId().getStringValor(),
                "message", "Criado com sucesso"));
    }
}
