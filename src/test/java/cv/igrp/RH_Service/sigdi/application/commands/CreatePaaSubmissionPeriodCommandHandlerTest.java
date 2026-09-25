package cv.igrp.RH_Service.sigdi.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
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

    // FIX-08 / Rule 4 (Phase 130 wave 6) -- RENAMED AND INVERTED.
    // PREVIOUS NAME: createSiadapIndividualPeriodSucceedsEvenWhenNoClosedPaaUnitLevelPeriodExists
    // WHAT IT ASSERTED: a SIADAP window (position 3) opened with an EMPTY year returned 201, and
    //   the PAA org->individual cascade (Rule 2) was never consulted for it. The first half was
    //   the finding itself -- docs/qa/121-ACHADO-ordem-do-ciclo.md -- recorded as behaviour, not
    //   as intent; the test was not defective, it registered what the server did then.
    // WHAT IT NOW ASSERTS: the same call is refused with 422 by the Rule 4 precedence guard,
    //   because no closed position-2 window exists for that year, and the refusal names the
    //   missing finalidade. The Rule 2 non-regression guard (verify ... never()) is PRESERVED
    //   verbatim: SIADAP must still never be gated by the PAA cascade, and the fact that the
    //   refusal now comes from Rule 4 is exactly what proves the two rules did not merge.
    // WHERE THE 201 HALF WENT: it did not disappear. It moved, with the same never() guard, to
    //   siadapIndividualPeriodSucceedsWhenTheClosedPaaPredecessorExists below.
    // REQUIREMENT: FIX-08, milestone v28.0.
    @Test
    void siadapIndividualPeriodIsRefusedWhenNoClosedPaaPredecessorExistsAndNeverConsultsThePaaCascade() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(10);
        CreatePaaSubmissionPeriodDTO dto = new CreatePaaSubmissionPeriodDTO(
                PaaLevel.INDIVIDUAL_LEVEL.getCode(), start, end, 2026, Purpose.SIADAP.getCode());

        when(repository.findByTypeAndYearAndStatusAndPurpose(
                PaaLevel.INDIVIDUAL_LEVEL, 2026, "OPEN", Purpose.SIADAP))
                .thenReturn(Optional.empty());
        when(repository.findAllByYear(2026)).thenReturn(List.of());

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(new CreatePaaSubmissionPeriodCommand(dto)));

        assertEquals(422, exception.getBody().getStatus());
        assertTrue(exception.getBody().getTitle().contains("Plano de Atividades Anual"));
        verify(repository, never()).save(any());

        // Pitfall-1 regression guard: the PAA org->individual cascade must never be consulted for SIADAP
        verify(repository, never()).findByTypeAndYearAndStatusAndPurpose(
                eq(PaaLevel.UNIT_LEVEL), any(), eq("CLOSED"), eq(Purpose.PAA));
    }

    // FIX-08 / Rule 4 (Phase 130 wave 6) -- NOT INVERTED, and the reason is written here because
    // its survival is the proof that Rule 4 did not swallow Rule 2.
    // WHAT IT ASSERTED AND STILL ASSERTS: PAA INDIVIDUAL_LEVEL (position 2) with no closed PAA
    //   UNIT_LEVEL sibling is refused with 422. Rule 2 runs BEFORE Rule 4 and throws first, so
    //   findAllByYear is never even reached and the message is Rule 2's own.
    // WHAT WAS ADDED: an assertion on the message, so that a future reordering that let Rule 4
    //   answer first would fail here loudly instead of passing on the status code alone.
    // REQUIREMENT: FIX-08, milestone v28.0.
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
        assertTrue(exception.getBody().getTitle().contains("Unidade Orgânica"));
        verify(repository, never()).findAllByYear(2026);
    }

    @Test
    void rejectsCreateWhenPurposeIsAbsentFromDto() {
        // PRZ-03: o caminho de produção que atribuía Purpose.PAA em silêncio deixou de
        // existir -- este teste substitui createPaaUnitLevelPeriodWithNoPurposeInDtoDefaultsToPaa,
        // que afirmava exactamente o comportamento que esta fase elimina.
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(10);
        CreatePaaSubmissionPeriodDTO dto = new CreatePaaSubmissionPeriodDTO(
                PaaLevel.UNIT_LEVEL.getCode(), start, end, 2026, null);

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(new CreatePaaSubmissionPeriodCommand(dto)));

        assertEquals(400, exception.getBody().getStatus());
        verify(repository, never()).save(any());
    }

    @Test
    void rejectsCreateWhenPurposeIsBlankInDto() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(10);
        CreatePaaSubmissionPeriodDTO dto = new CreatePaaSubmissionPeriodDTO(
                PaaLevel.UNIT_LEVEL.getCode(), start, end, 2026, "   ");

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(new CreatePaaSubmissionPeriodCommand(dto)));

        assertEquals(400, exception.getBody().getStatus());
        verify(repository, never()).save(any());
    }

    @Test
    void rejectsCreateWhenPurposeCodeIsUnknown() {
        // Guarda de não-regressão: Purpose.fromCodeOrThrow já recusa um código desconhecido
        // com 400 hoje; este teste prova que a nova guarda de ausência/branco não o substitui.
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(10);
        CreatePaaSubmissionPeriodDTO dto = new CreatePaaSubmissionPeriodDTO(
                PaaLevel.UNIT_LEVEL.getCode(), start, end, 2026, "NAO_EXISTE");

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(new CreatePaaSubmissionPeriodCommand(dto)));

        assertEquals(400, exception.getBody().getStatus());
        verify(repository, never()).save(any());
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

    // FIX-08 / Rule 4 (Phase 130 wave 6) -- ORDER-DEPENDENT, PRESERVED UNCHANGED.
    // docs/qa/130-PRECONDICOES.md Section 4.3 flagged this test as one of three whose TITLE
    // assertion would break if the precedence guard ran ahead of Rule 3: the new period is at
    // position 3 with no position-2 window in the year, so Rule 4 would answer first and the
    // asserted title would become the precedence message instead of the overlap one.
    // WHAT IT ASSERTED AND STILL ASSERTS: unchanged -- Rule 4 was placed AFTER Rule 3 precisely
    // so that an overlap keeps being reported as an overlap. See point (e) of the Rule 4 comment
    // in CreatePaaSubmissionPeriodCommandHandler.
    // REQUIREMENT: FIX-08, milestone v28.0.
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

    // FIX-08 / Rule 4 (Phase 130 wave 6) -- RENAMED AND INVERTED.
    // PREVIOUS NAME: skipsUnconfiguredPositionsWhenFindingNearestNeighbor
    // WHAT IT ASSERTED: that Rule 3 walks back to the nearest CONFIGURED predecessor, skipping
    //   empty positions, and -- since there was no overlap -- returned 201.
    // WHAT IT NOW ASSERTS: Rule 3 still skips the gap and still does NOT reject (the message is
    //   not an overlap message), but Rule 4 then refuses with 422 because the immediately
    //   preceding position 5 has no closed window that year. The skipping behaviour the test
    //   exists for is therefore still exercised and still asserted, by asserting the shape of the
    //   message rather than the status code alone; only the final outcome moved.
    // REQUIREMENT: FIX-08, milestone v28.0.
    @Test
    void skipsUnconfiguredPositionsWhenFindingNearestNeighborThenRefusesForTheMissingPredecessor() {
        // Only SIADAP (position 3) configured; position 4 (SIADAP_INTERIM) and position 5
        // (SIADAP_SELF_EVAL) have no record this year. New SIADAP_FINAL (position 6) must compare
        // against the nearest CONFIGURED predecessor (position 3), skipping the empty gap.
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

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(new CreatePaaSubmissionPeriodCommand(dto)));

        String title = exception.getBody().getTitle();
        assertEquals(422, exception.getBody().getStatus());
        // Rule 3 skipped the gap without rejecting: no overlap message was produced.
        assertTrue(!title.contains("Sobrepõe-se"));
        // Rule 4 names the immediately preceding position (5), not the nearest configured one (3).
        assertTrue(title.contains("Autoavaliação SIADAP"));
        verify(repository, never()).save(any());
    }

    @Test
    void detectsOverlapWithClosedUnitLevelSiblingHiddenBehindNewerIndividualLevelAtSamePosition() {
        // WR-02 regression: PAA (position 2) has two periods live in the same year --
        // UNIT_LEVEL closed early per Rule 2 (status CLOSED well before its date range
        // elapses) and a newer INDIVIDUAL_LEVEL sibling created afterwards with a much
        // narrower window. Deduping by position alone would keep only the newer
        // INDIVIDUAL_LEVEL record and miss that the new period actually overlaps the
        // still-live UNIT_LEVEL date range.
        PaaSubmissionPeriod unitPeriod = PaaSubmissionPeriod.reconstruct(
                UUID.randomUUID(), Purpose.PAA, PaaLevel.UNIT_LEVEL,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 8, 31), "CLOSED", 2026);
        PaaSubmissionPeriod individualPeriod = PaaSubmissionPeriod.reconstruct(
                UUID.randomUUID(), Purpose.PAA, PaaLevel.INDIVIDUAL_LEVEL,
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 15), "CLOSED", 2026);

        CreatePaaSubmissionPeriodDTO dto = new CreatePaaSubmissionPeriodDTO(
                PaaLevel.INDIVIDUAL_LEVEL.getCode(), LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 6, 1), 2026, Purpose.SIADAP.getCode());

        when(repository.findByTypeAndYearAndStatusAndPurpose(
                PaaLevel.INDIVIDUAL_LEVEL, 2026, "OPEN", Purpose.SIADAP))
                .thenReturn(Optional.empty());
        // newest-first, mirrors findAllByYear's ORDER BY createdDate DESC
        when(repository.findAllByYear(2026)).thenReturn(List.of(individualPeriod, unitPeriod));

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(new CreatePaaSubmissionPeriodCommand(dto)));

        String title = exception.getBody().getTitle();
        assertEquals(422, exception.getBody().getStatus());
        assertTrue(title.contains("Plano de Atividades Anual"));
        assertTrue(title.contains("31/08/2026"));
    }

    @Test
    void acceptsWhenNewPeriodOverlapsNeitherSamePositionSiblingOfDifferentType() {
        // Sanity check for the (position, type) grouping: two PAA siblings (UNIT_LEVEL and
        // INDIVIDUAL_LEVEL) share position 2, but neither range actually overlaps the new
        // period -- must not be rejected just because the position now carries two candidates.
        PaaSubmissionPeriod unitPeriod = PaaSubmissionPeriod.reconstruct(
                UUID.randomUUID(), Purpose.PAA, PaaLevel.UNIT_LEVEL,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 28), "CLOSED", 2026);
        PaaSubmissionPeriod individualPeriod = PaaSubmissionPeriod.reconstruct(
                UUID.randomUUID(), Purpose.PAA, PaaLevel.INDIVIDUAL_LEVEL,
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 15), "CLOSED", 2026);

        CreatePaaSubmissionPeriodDTO dto = new CreatePaaSubmissionPeriodDTO(
                PaaLevel.INDIVIDUAL_LEVEL.getCode(), LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 6, 1), 2026, Purpose.SIADAP.getCode());

        when(repository.findByTypeAndYearAndStatusAndPurpose(
                PaaLevel.INDIVIDUAL_LEVEL, 2026, "OPEN", Purpose.SIADAP))
                .thenReturn(Optional.empty());
        when(repository.findAllByYear(2026)).thenReturn(List.of(individualPeriod, unitPeriod));
        when(repository.save(any(PaaSubmissionPeriod.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<PaaSubmissionPeriodResponseDTO> response =
                handler.handle(new CreatePaaSubmissionPeriodCommand(dto));

        assertEquals(201, response.getStatusCode().value());
    }

    // FIX-08 / Rule 4 (Phase 130 wave 6) -- ORDER-DEPENDENT, PRESERVED UNCHANGED.
    // Second of the three title-asserting tests flagged by docs/qa/130-PRECONDICOES.md
    // Section 4.3: SIADAP_FINAL is position 6 and position 5 is absent from the year, so a guard
    // placed ahead of Rule 3 would answer with the precedence message and destroy the SOBREP-03
    // affirmation about the overlap message naming the Finalidade and formatting both dates.
    // WHAT IT ASSERTED AND STILL ASSERTS: unchanged, by the ordering decision.
    // REQUIREMENT: FIX-08, milestone v28.0.
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

    // FIX-08 / Rule 4 (Phase 130 wave 6) -- ALTERED, INTENT PRESERVED.
    // WHAT IT ASSERTED: positions 1 and 6 configured around a new position-3 period, exercising
    //   nearestBefore and nearestAfter together in one call, with 201 as the outcome.
    // WHAT IT NOW ASSERTS: the same thing. Only the fixture changed: a closed PAA window at
    //   position 2 was ADDED, because Rule 4 now requires the immediately preceding position to
    //   exist and be closed, and without it this test would have inverted to 422 and stopped
    //   exercising the two-sided nearest-neighbour comparison it was written for. This is the
    //   minimal alteration the plan prescribes for a test whose purpose is to exercise another
    //   rule. nearestBefore becomes position 2 instead of position 1; both sides still fire.
    // REQUIREMENT: FIX-08, milestone v28.0.
    @Test
    void acceptsWhenSandwichedBetweenTwoConfiguredNeighborsWithGapsOnBothSides() {
        // Extra coverage beyond the plan's 6 cases (plan-checker suggestion): positions 1 and 6
        // are BOTH configured simultaneously around a new position-3 period, exercising
        // nearestBefore and nearestAfter together in one call -- every other test here only
        // ever populates one side of the sequence.
        PaaSubmissionPeriod bscObjectives = PaaSubmissionPeriod.reconstruct(
                UUID.randomUUID(), Purpose.PAA_BSC_OBJECTIVES, PaaLevel.UNIT_LEVEL,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 28), "CLOSED", 2026);
        // Added by Phase 130 wave 6: the position-2 predecessor Rule 4 requires, with a gap on
        // both sides so it changes nothing about what Rule 3 is being asked here.
        PaaSubmissionPeriod paa = PaaSubmissionPeriod.reconstruct(
                UUID.randomUUID(), Purpose.PAA, PaaLevel.UNIT_LEVEL,
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 4, 15), "CLOSED", 2026);
        PaaSubmissionPeriod siadapFinal = PaaSubmissionPeriod.reconstruct(
                UUID.randomUUID(), Purpose.SIADAP_FINAL, PaaLevel.INDIVIDUAL_LEVEL,
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 12, 15), "CLOSED", 2026);

        CreatePaaSubmissionPeriodDTO dto = new CreatePaaSubmissionPeriodDTO(
                PaaLevel.INDIVIDUAL_LEVEL.getCode(), LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 8, 31), 2026, Purpose.SIADAP.getCode());

        when(repository.findByTypeAndYearAndStatusAndPurpose(
                PaaLevel.INDIVIDUAL_LEVEL, 2026, "OPEN", Purpose.SIADAP))
                .thenReturn(Optional.empty());
        when(repository.findAllByYear(2026)).thenReturn(List.of(bscObjectives, paa, siadapFinal));
        when(repository.save(any(PaaSubmissionPeriod.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<PaaSubmissionPeriodResponseDTO> response =
                handler.handle(new CreatePaaSubmissionPeriodCommand(dto));

        assertEquals(201, response.getStatusCode().value());
    }

    // FIX-08 / Rule 4 (Phase 130 wave 6) -- ALTERED, INTENT PRESERVED.
    // WHAT IT ASSERTED: daysRemaining is computed in AppTimeZone.CABO_VERDE, proved on a 201.
    // WHAT IT NOW ASSERTS: the same thing. Only the fixture changed: a closed position-1 window
    //   was ADDED to what findAllByYear returns, because the period being created is PAA
    //   (position 2) and Rule 4 now requires its predecessor. Without it the test would have
    //   inverted to 422 and stopped saying anything at all about the time zone -- which is the
    //   affirmation it exists for.
    // REQUIREMENT: FIX-08, milestone v28.0.
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

        // Added by Phase 130 wave 6: PAA is position 2, so Rule 4 now requires a closed
        // position-1 window for the year. Dates are relative to the same anchor the assertion
        // uses, so the predecessor is always strictly before the new window on any machine.
        PaaSubmissionPeriod bscObjectives = PaaSubmissionPeriod.reconstruct(
                UUID.randomUUID(), Purpose.PAA_BSC_OBJECTIVES, PaaLevel.UNIT_LEVEL,
                start.minusDays(60), start.minusDays(30), "CLOSED", 2026);

        when(repository.findByTypeAndYearAndStatusAndPurpose(
                PaaLevel.UNIT_LEVEL, 2026, "OPEN", Purpose.PAA))
                .thenReturn(Optional.empty());
        when(repository.save(any(PaaSubmissionPeriod.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.findAllByYear(2026)).thenReturn(List.of(bscObjectives));

        ResponseEntity<PaaSubmissionPeriodResponseDTO> response =
                handler.handle(new CreatePaaSubmissionPeriodCommand(dto));

        assertEquals(201, response.getStatusCode().value());
        assertEquals(15L, response.getBody().getDaysRemaining());
    }

    @Test
    void acceptsSelfEvaluationWindowSandwichedBetweenInterimAndFinal() {
        // SIA-02 criterion 3: the sixth Purpose value (SIADAP_SELF_EVAL, position 5) must slot
        // between SIADAP_INTERIM (position 4) and the renumbered SIADAP_FINAL (position 6)
        // without triggering the overlap rule when there is a gap on both sides.
        PaaSubmissionPeriod siadapInterim = PaaSubmissionPeriod.reconstruct(
                UUID.randomUUID(), Purpose.SIADAP_INTERIM, PaaLevel.INDIVIDUAL_LEVEL,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), "CLOSED", 2026);
        PaaSubmissionPeriod siadapFinal = PaaSubmissionPeriod.reconstruct(
                UUID.randomUUID(), Purpose.SIADAP_FINAL, PaaLevel.INDIVIDUAL_LEVEL,
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 12, 15), "CLOSED", 2026);

        CreatePaaSubmissionPeriodDTO dto = new CreatePaaSubmissionPeriodDTO(
                PaaLevel.INDIVIDUAL_LEVEL.getCode(), LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 10, 31), 2026, Purpose.SIADAP_SELF_EVAL.getCode());

        when(repository.findByTypeAndYearAndStatusAndPurpose(
                PaaLevel.INDIVIDUAL_LEVEL, 2026, "OPEN", Purpose.SIADAP_SELF_EVAL))
                .thenReturn(Optional.empty());
        when(repository.findAllByYear(2026)).thenReturn(List.of(siadapInterim, siadapFinal));
        when(repository.save(any(PaaSubmissionPeriod.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<PaaSubmissionPeriodResponseDTO> response =
                handler.handle(new CreatePaaSubmissionPeriodCommand(dto));

        assertEquals(201, response.getStatusCode().value());
    }

    // FIX-08 / Rule 4 (Phase 130 wave 6) -- ORDER-DEPENDENT, PRESERVED UNCHANGED.
    // Third and last of the three title-asserting tests flagged by
    // docs/qa/130-PRECONDICOES.md Section 4.3: SIADAP_SELF_EVAL is position 5 and position 4 is
    // absent from the year, so a guard placed ahead of Rule 3 would answer first.
    // WHAT IT ASSERTED AND STILL ASSERTS: unchanged, by the ordering decision.
    // REQUIREMENT: FIX-08, milestone v28.0.
    @Test
    void rejectsSelfEvaluationWindowThatOverlapsTheFinalEvaluationPhase() {
        // The nearest-following neighbor is now SIADAP_FINAL at the renumbered position 6 --
        // proves the Rule 3 comparison still catches the overlap after the renumbering.
        PaaSubmissionPeriod siadapFinal = PaaSubmissionPeriod.reconstruct(
                UUID.randomUUID(), Purpose.SIADAP_FINAL, PaaLevel.INDIVIDUAL_LEVEL,
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 12, 15), "CLOSED", 2026);

        CreatePaaSubmissionPeriodDTO dto = new CreatePaaSubmissionPeriodDTO(
                PaaLevel.INDIVIDUAL_LEVEL.getCode(), LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 11, 15), 2026, Purpose.SIADAP_SELF_EVAL.getCode());

        when(repository.findByTypeAndYearAndStatusAndPurpose(
                PaaLevel.INDIVIDUAL_LEVEL, 2026, "OPEN", Purpose.SIADAP_SELF_EVAL))
                .thenReturn(Optional.empty());
        when(repository.findAllByYear(2026)).thenReturn(List.of(siadapFinal));

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(new CreatePaaSubmissionPeriodCommand(dto)));

        String title = exception.getBody().getTitle();
        assertEquals(422, exception.getBody().getStatus());
        assertTrue(title.contains("Avaliação Final SIADAP"));
        assertTrue(title.contains("01/11/2026"));
    }

    // ---------------------------------------------------------------------------------------
    // FIX-08 / Rule 4 -- the six cases added by Phase 130 wave 6.
    // The positive side of the rule is proved HERE, in memory, and never by writing windows into
    // the database: there is no endpoint that deletes a submission window, so proving the happy
    // path against a live year would leave permanent rows behind for nothing.
    // ---------------------------------------------------------------------------------------

    // New case 1 of 6: position 1 is not gated at all -- it has no predecessor.
    @Test
    void positionOneIsNeverGatedByPrecedenceEvenWhenTheYearIsEmpty() {
        CreatePaaSubmissionPeriodDTO dto = new CreatePaaSubmissionPeriodDTO(
                PaaLevel.UNIT_LEVEL.getCode(), LocalDate.of(2030, 1, 1),
                LocalDate.of(2030, 2, 28), 2030, Purpose.PAA_BSC_OBJECTIVES.getCode());

        when(repository.findByTypeAndYearAndStatusAndPurpose(
                PaaLevel.UNIT_LEVEL, 2030, "OPEN", Purpose.PAA_BSC_OBJECTIVES))
                .thenReturn(Optional.empty());
        when(repository.findAllByYear(2030)).thenReturn(List.of());
        when(repository.save(any(PaaSubmissionPeriod.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<PaaSubmissionPeriodResponseDTO> response =
                handler.handle(new CreatePaaSubmissionPeriodCommand(dto));

        assertEquals(201, response.getStatusCode().value());
        assertEquals(Purpose.PAA_BSC_OBJECTIVES.getCode(), response.getBody().getPurpose());
    }

    // New case 2 of 6: position N is accepted when position N-1 exists and is CLOSED.
    // This is where the 201 half of the former
    // createSiadapIndividualPeriodSucceedsEvenWhenNoClosedPaaUnitLevelPeriodExists lives now,
    // including its never() guard on the PAA cascade.
    @Test
    void siadapIndividualPeriodSucceedsWhenTheClosedPaaPredecessorExists() {
        PaaSubmissionPeriod paa = PaaSubmissionPeriod.reconstruct(
                UUID.randomUUID(), Purpose.PAA, PaaLevel.INDIVIDUAL_LEVEL,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 28), "CLOSED", 2026);

        CreatePaaSubmissionPeriodDTO dto = new CreatePaaSubmissionPeriodDTO(
                PaaLevel.INDIVIDUAL_LEVEL.getCode(), LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 6, 30), 2026, Purpose.SIADAP.getCode());

        when(repository.findByTypeAndYearAndStatusAndPurpose(
                PaaLevel.INDIVIDUAL_LEVEL, 2026, "OPEN", Purpose.SIADAP))
                .thenReturn(Optional.empty());
        when(repository.findAllByYear(2026)).thenReturn(List.of(paa));
        when(repository.save(any(PaaSubmissionPeriod.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<PaaSubmissionPeriodResponseDTO> response =
                handler.handle(new CreatePaaSubmissionPeriodCommand(dto));

        assertEquals(201, response.getStatusCode().value());
        assertEquals(Purpose.SIADAP.getCode(), response.getBody().getPurpose());

        // Pitfall-1 regression guard, carried over intact: the PAA org->individual cascade must
        // never be consulted for SIADAP.
        verify(repository, never()).findByTypeAndYearAndStatusAndPurpose(
                eq(PaaLevel.UNIT_LEVEL), any(), eq("CLOSED"), eq(Purpose.PAA));
    }

    // New case 3 of 6: position N-1 exists but is still OPEN -- refused with 422.
    // "Exists" is not enough; the decision says the predecessor must be CLOSED.
    @Test
    void refusesWhenThePrecedingPositionExistsButIsStillOpen() {
        PaaSubmissionPeriod openPaa = PaaSubmissionPeriod.reconstruct(
                UUID.randomUUID(), Purpose.PAA, PaaLevel.UNIT_LEVEL,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 28), "OPEN", 2026);

        CreatePaaSubmissionPeriodDTO dto = new CreatePaaSubmissionPeriodDTO(
                PaaLevel.INDIVIDUAL_LEVEL.getCode(), LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 6, 30), 2026, Purpose.SIADAP.getCode());

        when(repository.findByTypeAndYearAndStatusAndPurpose(
                PaaLevel.INDIVIDUAL_LEVEL, 2026, "OPEN", Purpose.SIADAP))
                .thenReturn(Optional.empty());
        when(repository.findAllByYear(2026)).thenReturn(List.of(openPaa));

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(new CreatePaaSubmissionPeriodCommand(dto)));

        assertEquals(422, exception.getBody().getStatus());
        assertTrue(exception.getBody().getTitle().contains("Plano de Atividades Anual"));
        verify(repository, never()).save(any());
    }

    // New case 4 of 6: no window of position N-1 at all in the year -- refused with 422, and the
    // message NAMES the missing finalidade by its description. A message that only said "ordem
    // inválida" would leave the user guessing which phase to open first.
    @Test
    void refusesWhenNoWindowOfThePrecedingPositionExistsAndNamesTheMissingFinalidade() {
        PaaSubmissionPeriod bscObjectives = PaaSubmissionPeriod.reconstruct(
                UUID.randomUUID(), Purpose.PAA_BSC_OBJECTIVES, PaaLevel.UNIT_LEVEL,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 28), "CLOSED", 2026);

        CreatePaaSubmissionPeriodDTO dto = new CreatePaaSubmissionPeriodDTO(
                PaaLevel.INDIVIDUAL_LEVEL.getCode(), LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 6, 30), 2026, Purpose.SIADAP_INTERIM.getCode());

        when(repository.findByTypeAndYearAndStatusAndPurpose(
                PaaLevel.INDIVIDUAL_LEVEL, 2026, "OPEN", Purpose.SIADAP_INTERIM))
                .thenReturn(Optional.empty());
        when(repository.findAllByYear(2026)).thenReturn(List.of(bscObjectives));

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(new CreatePaaSubmissionPeriodCommand(dto)));

        String title = exception.getBody().getTitle();
        assertEquals(422, exception.getBody().getStatus());
        assertTrue(title.contains("Avaliação Intercalar SIADAP"));
        assertTrue(title.contains("Avaliação de Desempenho (SIADAP)"));
        assertTrue(title.contains("2026"));
        verify(repository, never()).save(any());
    }

    // New case 5 of 6: Rule 2 was NOT swallowed by the generalization. PAA INDIVIDUAL_LEVEL with
    // position 1 closed -- so Rule 4 would be satisfied -- is still refused, and refused by
    // RULE 2's message and not by Rule 4's, because Rule 2 runs first and short-circuits before
    // findAllByYear is ever called. The position-1 window is stubbed leniently precisely to model
    // the scenario the decision describes while proving that it is never reached.
    @Test
    void rule2CascadeStillRefusesPaaIndividualEvenWhenPositionOneIsClosed() {
        PaaSubmissionPeriod bscObjectives = PaaSubmissionPeriod.reconstruct(
                UUID.randomUUID(), Purpose.PAA_BSC_OBJECTIVES, PaaLevel.UNIT_LEVEL,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 28), "CLOSED", 2026);

        CreatePaaSubmissionPeriodDTO dto = new CreatePaaSubmissionPeriodDTO(
                PaaLevel.INDIVIDUAL_LEVEL.getCode(), LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 6, 30), 2026, Purpose.PAA.getCode());

        when(repository.findByTypeAndYearAndStatusAndPurpose(
                PaaLevel.INDIVIDUAL_LEVEL, 2026, "OPEN", Purpose.PAA))
                .thenReturn(Optional.empty());
        when(repository.findByTypeAndYearAndStatusAndPurpose(
                PaaLevel.UNIT_LEVEL, 2026, "CLOSED", Purpose.PAA))
                .thenReturn(Optional.empty());
        lenient().when(repository.findAllByYear(2026)).thenReturn(List.of(bscObjectives));

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(new CreatePaaSubmissionPeriodCommand(dto)));

        String title = exception.getBody().getTitle();
        assertEquals(422, exception.getBody().getStatus());
        // Rule 2's message ...
        assertTrue(title.contains("Unidade Orgânica"));
        // ... and NOT Rule 4's, which would have named the position-1 finalidade.
        assertTrue(!title.contains("Objetivos Estratégicos PAA/BSC"));
        verify(repository, never()).findAllByYear(2026);
        verify(repository, never()).save(any());
    }

    // New case 6 of 6: the rule is level-agnostic. The operator's decision names no PaaLevel --
    // level is Rule 2's business, inside a position -- so a closed predecessor at UNIT_LEVEL
    // satisfies Rule 4 for a new window at INDIVIDUAL_LEVEL.
    @Test
    void precedenceIsLevelAgnosticSoAClosedPredecessorAtAnotherLevelSatisfiesIt() {
        PaaSubmissionPeriod paaUnitLevel = PaaSubmissionPeriod.reconstruct(
                UUID.randomUUID(), Purpose.PAA, PaaLevel.UNIT_LEVEL,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 28), "CLOSED", 2026);

        CreatePaaSubmissionPeriodDTO dto = new CreatePaaSubmissionPeriodDTO(
                PaaLevel.INDIVIDUAL_LEVEL.getCode(), LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 6, 30), 2026, Purpose.SIADAP.getCode());

        when(repository.findByTypeAndYearAndStatusAndPurpose(
                PaaLevel.INDIVIDUAL_LEVEL, 2026, "OPEN", Purpose.SIADAP))
                .thenReturn(Optional.empty());
        when(repository.findAllByYear(2026)).thenReturn(List.of(paaUnitLevel));
        when(repository.save(any(PaaSubmissionPeriod.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<PaaSubmissionPeriodResponseDTO> response =
                handler.handle(new CreatePaaSubmissionPeriodCommand(dto));

        assertEquals(201, response.getStatusCode().value());
    }
}
