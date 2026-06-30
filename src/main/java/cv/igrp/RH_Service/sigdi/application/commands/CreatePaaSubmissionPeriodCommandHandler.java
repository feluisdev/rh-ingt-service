package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.dto.PaaSubmissionPeriodResponseDTO;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.PaaSubmissionPeriod;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PaaSubmissionPeriodRepository;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Component
public class CreatePaaSubmissionPeriodCommandHandler implements CommandHandler<CreatePaaSubmissionPeriodCommand, ResponseEntity<PaaSubmissionPeriodResponseDTO>> {

    private final PaaSubmissionPeriodRepository repository;

    public CreatePaaSubmissionPeriodCommandHandler(PaaSubmissionPeriodRepository repository) {
        this.repository = repository;
    }

    @IgrpCommandHandler
    @Transactional
    @Override
    public ResponseEntity<PaaSubmissionPeriodResponseDTO> handle(CreatePaaSubmissionPeriodCommand command) {
        var dto = command.getPeriod();
        PaaLevel type = PaaLevel.fromCodeOrThrow(dto.getType());

        // Rule 1: No active period of the same type and year
        Optional<PaaSubmissionPeriod> activePeriod = repository.findByTypeAndYearAndStatus(type, dto.getYear(), "OPEN");
        if (activePeriod.isPresent()) {
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Já existe um período aberto para o tipo " + type.getDescription() + " no ano " + dto.getYear());
        }

        // Rule 2: For INDIVIDUAL_LEVEL, a closed UNIT_LEVEL period must exist for the same year
        if (PaaLevel.INDIVIDUAL_LEVEL.equals(type)) {
            Optional<PaaSubmissionPeriod> unitPeriod = repository.findByTypeAndYearAndStatus(PaaLevel.UNIT_LEVEL, dto.getYear(), "CLOSED");
            if (unitPeriod.isEmpty()) {
                throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Não é possível abrir período Individual sem que o período da Unidade Orgânica esteja fechado para o ano " + dto.getYear());
            }
        }

        PaaSubmissionPeriod period = PaaSubmissionPeriod.create(
                type,
                dto.getStartDate(),
                dto.getEndDate(),
                dto.getYear()
        );

        PaaSubmissionPeriod saved = repository.save(period);

        PaaSubmissionPeriodResponseDTO response = new PaaSubmissionPeriodResponseDTO();
        response.setId(saved.getId());
        response.setType(saved.getType().getCode());
        response.setTypeDesc(saved.getType().getDescription());
        response.setStartDate(saved.getStartDate());
        response.setEndDate(saved.getEndDate());
        response.setStatus(saved.getStatus());
        response.setStatusDesc("Aberto");
        response.setYear(saved.getYear());

        long days = ChronoUnit.DAYS.between(java.time.LocalDate.now(), saved.getEndDate());
        response.setDaysRemaining(Math.max(0, days));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
