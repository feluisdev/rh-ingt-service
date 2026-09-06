package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.config.AppTimeZone;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.application.dto.PaaSubmissionPeriodResponseDTO;
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
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class CreatePaaSubmissionPeriodCommandHandler implements CommandHandler<CreatePaaSubmissionPeriodCommand, ResponseEntity<PaaSubmissionPeriodResponseDTO>> {

    private final PaaSubmissionPeriodRepository repository;
    // Instantiated directly rather than constructor-injected: PaaSubmissionPeriodSequenceRules
    // is stateless (no dependencies of its own), and CreatePaaSubmissionPeriodCommandHandlerTest
    // uses Mockito's @InjectMocks against the single-argument constructor with no mock for this
    // type -- adding it as a constructor parameter makes Mockito pass null for it, which is a
    // test file this plan is not allowed to touch (133-03 Task 1 acceptance criteria).
    private final PaaSubmissionPeriodSequenceRules sequenceRules = new PaaSubmissionPeriodSequenceRules();

    public CreatePaaSubmissionPeriodCommandHandler(PaaSubmissionPeriodRepository repository) {
        this.repository = repository;
    }

    @IgrpCommandHandler
    @Transactional
    @Override
    public ResponseEntity<PaaSubmissionPeriodResponseDTO> handle(CreatePaaSubmissionPeriodCommand command) {
        var dto = command.getPeriod();
        PaaLevel type = PaaLevel.fromCodeOrThrow(dto.getType());
        if (dto.getPurpose() == null || dto.getPurpose().isBlank()) {
            throw IgrpResponseStatusException.of(HttpStatus.BAD_REQUEST, "O campo <purpose> é obrigatório");
        }
        Purpose purpose = Purpose.fromCodeOrThrow(dto.getPurpose());

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

        // Rule 3: Cross-type overlap — sequence-based nearest-neighbor comparison (SOBREP-01/02/03).
        // Extracted into PaaSubmissionPeriodSequenceRules (133-03) so creation and alteration
        // share a single implementation of the two-stage D-14 logic instead of two copies.
        List<PaaSubmissionPeriod> yearPeriods = repository.findAllByYear(dto.getYear());
        int newPosition = purpose.getPosition();
        sequenceRules.enforceNoOverlap(yearPeriods, newPosition, dto.getStartDate(), dto.getEndDate(), null);

        // Rule 4: annual-cycle precedence (FIX-08, milestone v28.0).
        //
        // (a) WHICH FINDING THIS CLOSES. "A ordem do ciclo anual é declarada e não é imposta"
        //     (Alta, Phase 121) -- docs/qa/121-ACHADO-ordem-do-ciclo.md. Purpose.java declares a
        //     fixed position per finalidade (BSC -> PAA -> SIADAP), deliberately NOT derived from
        //     ordinal(); until this rule existed nothing enforced it, and a POST could open
        //     SIADAP_FINAL (position 6) with none of the five preceding phases in place.
        //     Opening a window at position N now requires a window at position N-1, for the SAME
        //     year, with status CLOSED (operator decision 1, 2026-09-04, 130-CONTEXT.md).
        //
        // (b) IT GENERALIZES RULE 2, IT DOES NOT REPLACE IT. Rule 2 above is the only precedence
        //     guard that existed, and it covers the cascade WITHIN a single position: PAA
        //     INDIVIDUAL_LEVEL requires a closed PAA UNIT_LEVEL window. Rule 4 is precedence
        //     BETWEEN positions. The two do not collide and Rule 2 stays untouched -- it still
        //     runs first, so a PAA individual window with no closed unit sibling is still refused
        //     by Rule 2's own message, not by this one.
        //
        // (c) IT IS LEVEL-AGNOSTIC, AND THE REASON IS WRITTEN RATHER THAN CHOSEN IN SILENCE. The
        //     decision reads "a window at position N-1 in the same year with status CLOSED" and
        //     names no level. Level is Rule 2's business, inside a position. So any closed window
        //     at position N-1 satisfies this rule, whatever its PaaLevel.
        //
        // (d) POSITION 1 IS NOT GATED. It has no predecessor, so there is nothing to require.
        //     The guard is skipped entirely for it.
        //
        // (e) THE POSITION IN THE FILE IS AFTER RULE 2 AND AFTER RULE 3, AND THAT IS DELIBERATE.
        //     docs/qa/130-PRECONDICOES.md Section 4.4 measured, before this wave started, what
        //     each placement costs: ahead of Rule 3 it breaks three existing title assertions
        //     that are about overlap and not about precedence, and ahead of Rule 2 it starves the
        //     stub of createPaaIndividualPeriodStillRequiresClosedPaaUnitLevelPeriod, producing an
        //     UnnecessaryStubbingException with nothing actually wrong. Placed here it reads
        //     yearPeriods, which Rule 3 has already fetched, so no new repository port is opened.
        //
        // (f) THE ACCEPTED COST. This prevents two phases of the cycle being open in parallel,
        //     even when the business would sometimes want it. That cost was stated and accepted by
        //     the operator in decision 1; it is not a side effect discovered afterwards.
        //
        // The predecessor is looked up over the FULL yearPeriods list and not over the
        // dedup map that PaaSubmissionPeriodSequenceRules builds internally for Rule 3: that map
        // keeps only the newest record per (position, level) pair, so a closed predecessor hidden
        // behind a newer reopened sibling would be lost, and this rule only asks whether *some*
        // closed window exists at that position.
        int previousPosition = newPosition - 1;
        if (previousPosition >= 1) {
            Optional<Purpose> requiredPredecessor = Arrays.stream(Purpose.values())
                    .filter(p -> p.getPosition() == previousPosition)
                    .findFirst();
            // If no finalidade declares position N-1 the declared sequence has a hole and there is
            // nothing to require; the guard is skipped rather than inventing a predecessor.
            if (requiredPredecessor.isPresent()) {
                boolean predecessorClosed = yearPeriods.stream()
                        .filter(p -> p.getPurpose().getPosition() == previousPosition)
                        .anyMatch(p -> p.isClosed());
                if (!predecessorClosed) {
                    throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                            "Não é possível abrir o período de " + purpose.getDescription()
                                    + " sem que o período de " + requiredPredecessor.get().getDescription()
                                    + " esteja fechado para o ano " + dto.getYear());
                }
            }
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

        long days = ChronoUnit.DAYS.between(LocalDate.now(AppTimeZone.CABO_VERDE), saved.getEndDate());
        // NAV-03 (ACH-M-03): this used to be Math.max(0, days), which reported an expired
        // period as having exactly zero days left -- indistinguishable from one ending today.
        // The dashboard read that 0 and rendered "Hoje" in red for a deadline that had passed
        // three weeks earlier, and the frontend had no way to tell the two apart because the
        // information had already been destroyed here. A negative value is the honest answer
        // and is what deriveDeadlineNotifications' own `daysRemaining < 0` branch was written
        // to consume -- that branch had never once been reached.
        response.setDaysRemaining(days);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
