package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.application.dto.PaaSubmissionPeriodResponseDTO;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.PaaSubmissionPeriod;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PaaSubmissionPeriodRepository;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
        Purpose purpose = (dto.getPurpose() == null || dto.getPurpose().isBlank())
                ? Purpose.PAA
                : Purpose.fromCodeOrThrow(dto.getPurpose());

        // Rule 1: No active period of the same type, year and purpose
        Optional<PaaSubmissionPeriod> activePeriod = repository.findByTypeAndYearAndStatusAndPurpose(type, dto.getYear(), "OPEN", purpose);
        if (activePeriod.isPresent()) {
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Já existe um período aberto para o tipo " + type.getDescription() + " no ano " + dto.getYear());
        }

        // Rule 2: For PAA INDIVIDUAL_LEVEL, a closed PAA UNIT_LEVEL period must exist for the same year
        // (org->individual cascade applies only to purpose=PAA; SIADAP individual periods are never gated by it)
        if (Purpose.PAA.equals(purpose) && PaaLevel.INDIVIDUAL_LEVEL.equals(type)) {
            Optional<PaaSubmissionPeriod> unitPeriod = repository.findByTypeAndYearAndStatusAndPurpose(PaaLevel.UNIT_LEVEL, dto.getYear(), "CLOSED", Purpose.PAA);
            if (unitPeriod.isEmpty()) {
                throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Não é possível abrir período Individual sem que o período da Unidade Orgânica esteja fechado para o ano " + dto.getYear());
            }
        }

        // Rule 3: Cross-type overlap — sequence-based nearest-neighbor comparison (SOBREP-01/02/03)
        List<PaaSubmissionPeriod> yearPeriods = repository.findAllByYear(dto.getYear());

        // Reduce to the most-recent period per sequence position (handles reopening).
        // yearPeriods already arrives ordered createdDate DESC (see the JPQL below), so
        // putIfAbsent keeps the newest — the same "most recent wins" convention used by
        // findActiveByTypeAndPurpose/findActiveByTypeAndYearAndPurpose/findByTypeAndYearAndStatusAndPurpose
        // in PaaSubmissionPeriodRepositoryImpl, just keyed by position instead of taking a single result.
        Map<Integer, PaaSubmissionPeriod> latestByPosition = new LinkedHashMap<>();
        for (PaaSubmissionPeriod p : yearPeriods) {
            latestByPosition.putIfAbsent(p.getPurpose().getPosition(), p);
        }

        int newPosition = purpose.getPosition();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        PaaSubmissionPeriod nearestBefore = latestByPosition.entrySet().stream()
                .filter(e -> e.getKey() < newPosition)
                .max(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .orElse(null);

        PaaSubmissionPeriod nearestAfter = latestByPosition.entrySet().stream()
                .filter(e -> e.getKey() > newPosition)
                .min(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .orElse(null);

        if (nearestBefore != null && !dto.getStartDate().isAfter(nearestBefore.getEndDate())) {
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Sobrepõe-se a " + nearestBefore.getPurpose().getDescription() + ", "
                            + nearestBefore.getStartDate().format(fmt) + "–" + nearestBefore.getEndDate().format(fmt));
        }
        if (nearestAfter != null && !nearestAfter.getStartDate().isAfter(dto.getEndDate())) {
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Sobrepõe-se a " + nearestAfter.getPurpose().getDescription() + ", "
                            + nearestAfter.getStartDate().format(fmt) + "–" + nearestAfter.getEndDate().format(fmt));
        }

        PaaSubmissionPeriod period = PaaSubmissionPeriod.create(
                purpose,
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
        response.setPurpose(saved.getPurpose().getCode());
        response.setPurposeDesc(saved.getPurpose().getDescription());

        long days = ChronoUnit.DAYS.between(java.time.LocalDate.now(), saved.getEndDate());
        response.setDaysRemaining(Math.max(0, days));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
