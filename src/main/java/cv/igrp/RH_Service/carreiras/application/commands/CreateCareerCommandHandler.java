package cv.igrp.RH_Service.carreiras.application.commands;

import cv.igrp.RH_Service.carreiras.domain.models.Career;
import cv.igrp.RH_Service.carreiras.domain.repository.CareerRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
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
public class CreateCareerCommandHandler
        implements CommandHandler<CreateCareerCommand, ResponseEntity<Map<String, ?>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateCareerCommandHandler.class);

    private final CareerRepository careerRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(CreateCareerCommand command) {
        var dto = command.getRequest();

        if (careerRepository.existsByCode(dto.getCode())) {
            throw IgrpResponseStatusException.conflict(
                    "Já existe uma carreira com code='" + dto.getCode() + "'.");
        }

        UUID regimeOptionId = dto.getRegimeOptionId() != null && !dto.getRegimeOptionId().isBlank()
                ? UUID.fromString(dto.getRegimeOptionId()) : null;

        Career saved = careerRepository.save(
                Career.criar(dto.getCode(), dto.getName(), dto.getDescription(), regimeOptionId));

        return ResponseEntity.status(201).body(Map.of(
                "id", saved.getId().getStringValor(),
                "message", "Criado com sucesso"));
    }
}
