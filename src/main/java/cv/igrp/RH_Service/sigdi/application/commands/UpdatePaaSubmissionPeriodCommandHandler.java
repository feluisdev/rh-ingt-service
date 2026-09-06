package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.config.AppTimeZone;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.application.dto.PaaSubmissionPeriodResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.UpdatePaaSubmissionPeriodDTO;
import cv.igrp.RH_Service.sigdi.application.service.PaaSubmissionPeriodSequenceRules;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.PaaSubmissionPeriod;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PaaSubmissionPeriodRepository;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * The alteration verb the ciclo de vida da janela never had (133-03, JAN-03), delimited without
 * margin by the operator's T-140 decision (D-18, 133-CONTEXT.md): alteration only, and only while
 * the window's startDate is still in the future. There is no reopening path here -- a window that
 * already started or already closed stays without a correction path, and that residue of
 * A-132-100 is deferred as future requirement (D-20), not an oversight.
 */
@Component
public class UpdatePaaSubmissionPeriodCommandHandler implements CommandHandler<UpdatePaaSubmissionPeriodCommand, ResponseEntity<PaaSubmissionPeriodResponseDTO>> {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PaaSubmissionPeriodRepository repository;
    private final PaaSubmissionPeriodSequenceRules sequenceRules;

    public UpdatePaaSubmissionPeriodCommandHandler(PaaSubmissionPeriodRepository repository,
                                                    PaaSubmissionPeriodSequenceRules sequenceRules) {
        this.repository = repository;
        this.sequenceRules = sequenceRules;
    }

    @IgrpCommandHandler
    @Transactional
    @Override
    public ResponseEntity<PaaSubmissionPeriodResponseDTO> handle(UpdatePaaSubmissionPeriodCommand command) {
        UpdatePaaSubmissionPeriodDTO dto = command.getPeriod();

        // Guard 1: not found.
        PaaSubmissionPeriod period = repository.findById(command.getId())
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Período de submissão não encontrado"));

        // Guard 2: no reopening (D-18/T-140) -- a period that is already CLOSED has no
        // correction path through this verb.
        if (!period.isOpen()) {
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Não é possível alterar um período já fechado; não existe via de reabertura");
        }

        LocalDate today = LocalDate.now(AppTimeZone.CABO_VERDE);

        // Guard 3: the window's CURRENT startDate must still be in the future -- this is the
        // guard that makes the alteration action disappear the moment the window opens.
        if (!period.getStartDate().isAfter(today)) {
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Não é possível alterar um período que já começou em " + period.getStartDate().format(DATE_FORMAT));
        }

        // Guard 4: the NEW startDate must also be in the future -- moving it into the past would
        // be reopening through the back door, which T-140 refuses.
        if (!dto.getStartDate().isAfter(today)) {
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "A nova data de início tem de ser posterior a " + today.format(DATE_FORMAT)
                            + "; alterar para uma data já passada equivale a reabrir o período");
        }

        Purpose purpose = period.getPurpose();

        // Guard 5: Rule 3, always, over the NEW year, excluding this period's own id from the
        // candidate set -- without the exclusion the window would overlap itself.
        List<PaaSubmissionPeriod> yearPeriods = repository.findAllByYear(dto.getYear());
        sequenceRules.enforceNoOverlap(yearPeriods, purpose.getPosition(), dto.getStartDate(), dto.getEndDate(), period.getId());

        // Guard 6: Rules 1 and 4, only when the year changes. With the year unchanged the
        // (type, year, purpose) identity does not change, so the alteration cannot newly create
        // a second OPEN window of the same trio (Rule 1) nor change who the position predecessor
        // is (Rule 4). With the year changed both become legitimate questions again and re-run
        // with the same messages as creation.
        if (!dto.getYear().equals(period.getYear())) {
            Optional<PaaSubmissionPeriod> activePeriod = repository.findByTypeAndYearAndStatusAndPurpose(
                    period.getType(), dto.getYear(), "OPEN", purpose);
            if (activePeriod.isPresent() && !activePeriod.get().getId().equals(period.getId())) {
                throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Já existe um período aberto para o tipo " + period.getType().getDescription()
                                + " no ano " + dto.getYear());
            }

            int previousPosition = purpose.getPosition() - 1;
            if (previousPosition >= 1) {
                Optional<Purpose> requiredPredecessor = Arrays.stream(Purpose.values())
                        .filter(p -> p.getPosition() == previousPosition)
                        .findFirst();
                if (requiredPredecessor.isPresent()) {
                    boolean predecessorClosed = yearPeriods.stream()
                            .filter(p -> p.getPurpose().getPosition() == previousPosition)
                            .anyMatch(PaaSubmissionPeriod::isClosed);
                    if (!predecessorClosed) {
                        throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                                "Não é possível alterar o período de " + purpose.getDescription()
                                        + " sem que o período de " + requiredPredecessor.get().getDescription()
                                        + " esteja fechado para o ano " + dto.getYear());
                    }
                }
            }
        }

        PaaSubmissionPeriod changed = period.changeSchedule(dto.getStartDate(), dto.getEndDate(), dto.getYear());
        PaaSubmissionPeriod saved = repository.save(changed);

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
        response.setCreatedDate(saved.getCreatedDate());
        response.setCreatedBy(saved.getCreatedBy());

        long days = ChronoUnit.DAYS.between(LocalDate.now(AppTimeZone.CABO_VERDE), saved.getEndDate());
        response.setDaysRemaining(days);

        return ResponseEntity.ok(response);
    }
}
