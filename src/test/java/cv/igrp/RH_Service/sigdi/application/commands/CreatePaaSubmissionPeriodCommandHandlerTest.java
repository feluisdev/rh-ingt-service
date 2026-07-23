package cv.igrp.RH_Service.sigdi.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.shared.config.AppTimeZone;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.application.dto.CreatePaaSubmissionPeriodDTO;
import cv.igrp.RH_Service.sigdi.application.dto.PaaSubmissionPeriodResponseDTO;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.PaaSubmissionPeriod;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PaaSubmissionPeriodRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class CreatePaaSubmissionPeriodCommandHandlerTest {

    @Mock
    private PaaSubmissionPeriodRepository repository;

    @InjectMocks
    private CreatePaaSubmissionPeriodCommandHandler handler;

    @Test
    void createSiadapIndividualPeriodSucceedsEvenWhenNoClosedPaaUnitLevelPeriodExists() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(10);
        CreatePaaSubmissionPeriodDTO dto = new CreatePaaSubmissionPeriodDTO(
                PaaLevel.INDIVIDUAL_LEVEL.getCode(), start, end, 2026, Purpose.SIADAP.getCode());

        when(repository.findByTypeAndYearAndStatusAndPurpose(
                PaaLevel.INDIVIDUAL_LEVEL, 2026, "OPEN", Purpose.SIADAP))
                .thenReturn(Optional.empty());
        when(repository.save(any(PaaSubmissionPeriod.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.findAllByYear(2026)).thenReturn(List.of());

        ResponseEntity<PaaSubmissionPeriodResponseDTO> response =
                handler.handle(new CreatePaaSubmissionPeriodCommand(dto));

        assertEquals(201, response.getStatusCode().value());
        assertEquals(Purpose.SIADAP.getCode(), response.getBody().getPurpose());

        // Pitfall-1 regression guard: the PAA org->individual cascade must never be consulted for SIADAP
        verify(repository, never()).findByTypeAndYearAndStatusAndPurpose(
                eq(PaaLevel.UNIT_LEVEL), any(), eq("CLOSED"), eq(Purpose.PAA));
    }

    @Test
    void createPaaIndividualPeriodStillRequiresClosedPaaUnitLevelPeriod() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(10);
        CreatePaaSubmissionPeriodDTO dto = new CreatePaaSubmissionPeriodDTO(
                PaaLevel.INDIVIDUAL_LEVEL.getCode(), start, end, 2026, Purpose.PAA.getCode());

        when(repository.findByTypeAndYearAndStatusAndPurpose(
                PaaLevel.INDIVIDUAL_LEVEL, 2026, "OPEN", Purpose.PAA))
                .thenReturn(Optional.empty());
        when(repository.findByTypeAndYearAndStatusAndPurpose(
                PaaLevel.UNIT_LEVEL, 2026, "CLOSED", Purpose.PAA))
                .thenReturn(Optional.empty());

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(new CreatePaaSubmissionPeriodCommand(dto)));

        assertEquals(422, exception.getBody().getStatus());
    }

    @Test
    void createPaaUnitLevelPeriodWithNoPurposeInDtoDefaultsToPaa() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(10);
        CreatePaaSubmissionPeriodDTO dto = new CreatePaaSubmissionPeriodDTO(
                PaaLevel.UNIT_LEVEL.getCode(), start, end, 2026, null);

        when(repository.findByTypeAndYearAndStatusAndPurpose(
                PaaLevel.UNIT_LEVEL, 2026, "OPEN", Purpose.PAA))
                .thenReturn(Optional.empty());
        when(repository.save(any(PaaSubmissionPeriod.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.findAllByYear(2026)).thenReturn(List.of());

        ResponseEntity<PaaSubmissionPeriodResponseDTO> response =
                handler.handle(new CreatePaaSubmissionPeriodCommand(dto));

        assertEquals(Purpose.PAA.getCode(), response.getBody().getPurpose());
    }

    @Test
    void rejectsWhenNewPeriodStartsBeforeNearestPrecedingConfiguredPurposeEnds() {
        // nearestBefore: SIADAP (position 3), 2026-01-01 .. 2026-06-30
        // new: SIADAP_INTERIM (position 4), starts 2026-06-30 (same day as prev end -> reject)
        LocalDate prevEnd = LocalDate.of(2026, 6, 30);
        PaaSubmissionPeriod siadap = PaaSubmissionPeriod.reconstruct(
                UUID.randomUUID(), Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL,
                LocalDate.of(2026, 1, 1), prevEnd, "CLOSED", 2026);

        CreatePaaSubmissionPeriodDTO dto = new CreatePaaSubmissionPeriodDTO(
                PaaLevel.INDIVIDUAL_LEVEL.getCode(), prevEnd, prevEnd.plusDays(30), 2026,
                Purpose.SIADAP_INTERIM.getCode());

        when(repository.findByTypeAndYearAndStatusAndPurpose(
                PaaLevel.INDIVIDUAL_LEVEL, 2026, "OPEN", Purpose.SIADAP_INTERIM))
                .thenReturn(Optional.empty());
        when(repository.findAllByYear(2026)).thenReturn(List.of(siadap));

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(new CreatePaaSubmissionPeriodCommand(dto)));

        assertEquals(422, exception.getBody().getStatus());
        assertTrue(exception.getBody().getTitle().contains("Avaliação de Desempenho (SIADAP)"));
    }

    @Test
    void rejectsWhenNearestFollowingConfiguredPurposeStartsBeforeNewPeriodEnds() {
        // nearestAfter: SIADAP_INTERIM (position 4), 2026-07-01 .. 2026-07-31
        // new: SIADAP (position 3), 2026-06-01 .. 2026-07-15 (end reaches into nearestAfter's
        // start -> reject). No position < 3 is configured, so only the nearestAfter branch fires.
        PaaSubmissionPeriod siadapInterim = PaaSubmissionPeriod.reconstruct(
                UUID.randomUUID(), Purpose.SIADAP_INTERIM, PaaLevel.INDIVIDUAL_LEVEL,
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31), "CLOSED", 2026);

        CreatePaaSubmissionPeriodDTO dto = new CreatePaaSubmissionPeriodDTO(
                PaaLevel.INDIVIDUAL_LEVEL.getCode(), LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 7, 15), 2026, Purpose.SIADAP.getCode());

        when(repository.findByTypeAndYearAndStatusAndPurpose(
                PaaLevel.INDIVIDUAL_LEVEL, 2026, "OPEN", Purpose.SIADAP))
                .thenReturn(Optional.empty());
        when(repository.findAllByYear(2026)).thenReturn(List.of(siadapInterim));

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(new CreatePaaSubmissionPeriodCommand(dto)));

        assertEquals(422, exception.getBody().getStatus());
        assertTrue(exception.getBody().getTitle().contains("Avaliação Intercalar SIADAP"));
    }

    @Test
    void acceptsGapOfSeveralDaysBetweenConsecutivePhases() {
        // SOBREP-02: a multi-day gap must NOT trigger the overlap error.
        PaaSubmissionPeriod siadap = PaaSubmissionPeriod.reconstruct(
                UUID.randomUUID(), Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), "CLOSED", 2026);

        CreatePaaSubmissionPeriodDTO dto = new CreatePaaSubmissionPeriodDTO(
                PaaLevel.INDIVIDUAL_LEVEL.getCode(), LocalDate.of(2026, 7, 15),
                LocalDate.of(2026, 8, 15), 2026, Purpose.SIADAP_INTERIM.getCode());

        when(repository.findByTypeAndYearAndStatusAndPurpose(
                PaaLevel.INDIVIDUAL_LEVEL, 2026, "OPEN", Purpose.SIADAP_INTERIM))
                .thenReturn(Optional.empty());
        when(repository.findAllByYear(2026)).thenReturn(List.of(siadap));
        when(repository.save(any(PaaSubmissionPeriod.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<PaaSubmissionPeriodResponseDTO> response =
                handler.handle(new CreatePaaSubmissionPeriodCommand(dto));

        assertEquals(201, response.getStatusCode().value());
    }

    @Test
    void usesMostRecentPeriodPerPositionWhenReopened() {
        // Two SIADAP (position 3) records for 2026, ordered newest-first (mirrors the
        // findAllByYear JPQL's ORDER BY createdDate DESC): the newer one conflicts with the
        // new period, the older one alone would not -- proves putIfAbsent keeps the first-seen
        // (newest) record per position, not the oldest (Pitfall 4 / reopening).
        PaaSubmissionPeriod newerSiadap = PaaSubmissionPeriod.reconstruct(
                UUID.randomUUID(), Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL,
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 8, 31), "CLOSED", 2026);
        PaaSubmissionPeriod olderSiadap = PaaSubmissionPeriod.reconstruct(
                UUID.randomUUID(), Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31), "CLOSED", 2026);

        CreatePaaSubmissionPeriodDTO dto = new CreatePaaSubmissionPeriodDTO(
                PaaLevel.INDIVIDUAL_LEVEL.getCode(), LocalDate.of(2026, 8, 15),
                LocalDate.of(2026, 9, 15), 2026, Purpose.SIADAP_INTERIM.getCode());

        when(repository.findByTypeAndYearAndStatusAndPurpose(
                PaaLevel.INDIVIDUAL_LEVEL, 2026, "OPEN", Purpose.SIADAP_INTERIM))
                .thenReturn(Optional.empty());
        when(repository.findAllByYear(2026)).thenReturn(List.of(newerSiadap, olderSiadap));

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(new CreatePaaSubmissionPeriodCommand(dto)));

        String title = exception.getBody().getTitle();
        assertEquals(422, exception.getBody().getStatus());
        assertTrue(title.contains("31/08/2026"));
        assertTrue(!title.contains("31/03/2026"));
    }

    @Test
    void skipsUnconfiguredPositionsWhenFindingNearestNeighbor() {
        // Only SIADAP (position 3) configured; position 4 (SIADAP_INTERIM) has no record this
        // year. New SIADAP_FINAL (position 5) must compare against the nearest CONFIGURED
        // predecessor (position 3), skipping the empty position 4 gap.
        PaaSubmissionPeriod siadap = PaaSubmissionPeriod.reconstruct(
                UUID.randomUUID(), Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), "CLOSED", 2026);

        CreatePaaSubmissionPeriodDTO dto = new CreatePaaSubmissionPeriodDTO(
                PaaLevel.INDIVIDUAL_LEVEL.getCode(), LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30), 2026, Purpose.SIADAP_FINAL.getCode());

        when(repository.findByTypeAndYearAndStatusAndPurpose(
                PaaLevel.INDIVIDUAL_LEVEL, 2026, "OPEN", Purpose.SIADAP_FINAL))
                .thenReturn(Optional.empty());
        when(repository.findAllByYear(2026)).thenReturn(List.of(siadap));
        when(repository.save(any(PaaSubmissionPeriod.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<PaaSubmissionPeriodResponseDTO> response =
                handler.handle(new CreatePaaSubmissionPeriodCommand(dto));

        assertEquals(201, response.getStatusCode().value());
    }

    @Test
    void overlapErrorMessageNamesConflictingFinalidadeAndExactDates() {
        // SOBREP-03: the conflict message must name the Finalidade and format both dates
        // dd/MM/yyyy (not the domain's ISO LocalDate.toString() form).
        PaaSubmissionPeriod siadapInterim = PaaSubmissionPeriod.reconstruct(
                UUID.randomUUID(), Purpose.SIADAP_INTERIM, PaaLevel.INDIVIDUAL_LEVEL,
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 10, 15), "CLOSED", 2026);

        CreatePaaSubmissionPeriodDTO dto = new CreatePaaSubmissionPeriodDTO(
                PaaLevel.INDIVIDUAL_LEVEL.getCode(), LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 11, 15), 2026, Purpose.SIADAP_FINAL.getCode());

        when(repository.findByTypeAndYearAndStatusAndPurpose(
                PaaLevel.INDIVIDUAL_LEVEL, 2026, "OPEN", Purpose.SIADAP_FINAL))
                .thenReturn(Optional.empty());
        when(repository.findAllByYear(2026)).thenReturn(List.of(siadapInterim));

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(new CreatePaaSubmissionPeriodCommand(dto)));

        String title = exception.getBody().getTitle();
        assertTrue(title.contains("Avaliação Intercalar SIADAP"));
        assertTrue(title.contains("01/09/2026"));
        assertTrue(title.contains("15/10/2026"));
        assertTrue(!title.contains("2026-09-01"));
    }

    @Test
    void acceptsWhenSandwichedBetweenTwoConfiguredNeighborsWithGapsOnBothSides() {
        // Extra coverage beyond the plan's 6 cases (plan-checker suggestion): positions 1 and 5
        // are BOTH configured simultaneously around a new position-3 period, exercising
        // nearestBefore and nearestAfter together in one call -- every other test here only
        // ever populates one side of the sequence.
        PaaSubmissionPeriod bscObjectives = PaaSubmissionPeriod.reconstruct(
                UUID.randomUUID(), Purpose.PAA_BSC_OBJECTIVES, PaaLevel.UNIT_LEVEL,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 28), "CLOSED", 2026);
        PaaSubmissionPeriod siadapFinal = PaaSubmissionPeriod.reconstruct(
                UUID.randomUUID(), Purpose.SIADAP_FINAL, PaaLevel.INDIVIDUAL_LEVEL,
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 12, 15), "CLOSED", 2026);

        CreatePaaSubmissionPeriodDTO dto = new CreatePaaSubmissionPeriodDTO(
                PaaLevel.INDIVIDUAL_LEVEL.getCode(), LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 8, 31), 2026, Purpose.SIADAP.getCode());

        when(repository.findByTypeAndYearAndStatusAndPurpose(
                PaaLevel.INDIVIDUAL_LEVEL, 2026, "OPEN", Purpose.SIADAP))
                .thenReturn(Optional.empty());
        when(repository.findAllByYear(2026)).thenReturn(List.of(bscObjectives, siadapFinal));
        when(repository.save(any(PaaSubmissionPeriod.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<PaaSubmissionPeriodResponseDTO> response =
                handler.handle(new CreatePaaSubmissionPeriodCommand(dto));

        assertEquals(201, response.getStatusCode().value());
    }

    @Test
    void createReturnsDaysRemainingComputedFromCaboVerdeZone() {
        // DATA-01 regression (79-RESEARCH.md Pitfall 1): daysRemaining must be computed with
        // AppTimeZone.CABO_VERDE, not the JVM default zone. Both start/end are anchored to the
        // same Cabo Verde "today" the handler itself uses, so the assertion stays correct on any
        // machine -- the first daysRemaining assertion in this file (79-02-PLAN.md Task 1).
        LocalDate today = LocalDate.now(AppTimeZone.CABO_VERDE);
        LocalDate start = today;
        LocalDate end = today.plusDays(15);
        CreatePaaSubmissionPeriodDTO dto = new CreatePaaSubmissionPeriodDTO(
                PaaLevel.UNIT_LEVEL.getCode(), start, end, 2026, Purpose.PAA.getCode());

        when(repository.findByTypeAndYearAndStatusAndPurpose(
                PaaLevel.UNIT_LEVEL, 2026, "OPEN", Purpose.PAA))
                .thenReturn(Optional.empty());
        when(repository.save(any(PaaSubmissionPeriod.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.findAllByYear(2026)).thenReturn(List.of());

        ResponseEntity<PaaSubmissionPeriodResponseDTO> response =
                handler.handle(new CreatePaaSubmissionPeriodCommand(dto));

        assertEquals(201, response.getStatusCode().value());
        assertEquals(15L, response.getBody().getDaysRemaining());
    }
}
