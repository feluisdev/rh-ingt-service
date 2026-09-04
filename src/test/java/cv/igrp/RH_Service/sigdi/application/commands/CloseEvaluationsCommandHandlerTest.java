package cv.igrp.RH_Service.sigdi.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.CompetencyCategory;
import cv.igrp.RH_Service.sigdi.application.constants.EvaluationPhase;
import cv.igrp.RH_Service.sigdi.application.constants.SiadapMeritRating;
import cv.igrp.RH_Service.sigdi.application.dto.CloseEvaluationsRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.CloseEvaluationsResponseDTO;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapEvaluation;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapEvaluationRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.CompetencyItem;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.IndividualObjective;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.SiadapConfigEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.SiadapConfigEntityRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

/**
 * Unit tests for {@link CloseEvaluationsCommandHandler} covering the 5 behaviours added/fixed
 * in Phase 81 Plan 01: per-unit routing, the minCollaboratorsForQuota gate (and its disabled
 * valve), config-driven Bom ("Good") quota, Bom's independence from Excelente, and the
 * preserved compliant-close path. Fixtures are all built at HARMONIZATION so the Task 3
 * batch phase-guard (added afterward) is a no-op here.
 *
 * <p>Authorization (only a CCA member may close evaluations in batch) is no longer exercised
 * here (Phase 115/AUT-04): that coverage moved to {@code ComplianceControllerMethodSecurityTest}
 * (115-03), which asserts the {@code @PreAuthorize} guard on
 * {@code ComplianceController#closeEvaluations} in execution.
 */
@ExtendWith(MockitoExtension.class)
class CloseEvaluationsCommandHandlerTest {

    private static final Integer YEAR = 2026;

    @Mock
    private SiadapEvaluationRepository evaluationRepository;

    @Mock
    private SiadapConfigEntityRepository configRepository;

    @InjectMocks
    private CloseEvaluationsCommandHandler handler;

    // ============================================================
    // Fixture builders
    // ============================================================

    /**
     * Builds a fully-realized SiadapEvaluation in HARMONIZATION phase carrying the given
     * merit rating, by driving it through the real domain chain (create -> contractualize ->
     * accept -> set competencies -> record objective/competency scores -> self-evaluation ->
     * finalize) and then overriding the computed merit rating via assignMeritRating (which
     * preserves the phase reached by finalizeEvaluation). Mirrors
     * AcceptSiadapObjectivesCommandHandlerTest's buildPendingAcceptanceEvaluation approach of
     * using the real aggregate rather than mocking it.
     */
    private SiadapEvaluation buildHarmonizationEvaluation(SiadapMeritRating meritRating) {
        return buildHarmonizationEvaluation(meritRating, null);
    }

    /**
     * Overload of {@link #buildHarmonizationEvaluation(SiadapMeritRating)} taking an explicit
     * organicUnitId, added for the WR-01 regression tests below that need evaluations spread
     * across 2+ distinct real organic units within the same "close all units" call.
     */
    private SiadapEvaluation buildHarmonizationEvaluation(SiadapMeritRating meritRating, String organicUnitId) {
        SiadapEvaluation created = SiadapEvaluation.create(
                UUID.randomUUID().toString(),
                YEAR,
                organicUnitId,
                UUID.randomUUID().toString(),
                new BigDecimal("60"),
                new BigDecimal("40"));

        List<IndividualObjective> objectives = List.of(
                IndividualObjective.create("OBJ-1", "Descrição do objetivo 1", "Indicador 1", new BigDecimal("100"), new BigDecimal("40")),
                IndividualObjective.create("OBJ-2", "Descrição do objetivo 2", "Indicador 2", new BigDecimal("100"), new BigDecimal("30")),
                IndividualObjective.create("OBJ-3", "Descrição do objetivo 3", "Indicador 3", new BigDecimal("100"), new BigDecimal("30")));

        List<CompetencyItem> competencies = List.of(
                CompetencyItem.create("COMP-1", "Competência 1", CompetencyCategory.BEHAVIORAL));

        SiadapEvaluation evaluation = created.contractualizeObjectives(objectives)
                .acceptObjectives()
                .setCompetencies(competencies);

        for (IndividualObjective obj : evaluation.getObjectives()) {
            evaluation = evaluation.recordObjectiveAchievement(obj.getCode(), new BigDecimal("100"), 5);
        }
        for (CompetencyItem comp : evaluation.getCompetencies()) {
            evaluation = evaluation.evaluateCompetency(comp.getCompetencyCode(), 5);
        }

        evaluation = evaluation.openSelfEvaluationPhase()
                .submitSelfEvaluation(new BigDecimal("5"))
                .finalizeEvaluation();

        return evaluation.assignMeritRating(meritRating);
    }

    private List<SiadapEvaluation> nHarmonizationEvaluations(int count, SiadapMeritRating rating) {
        return nHarmonizationEvaluations(count, rating, null);
    }

    /**
     * Overload of {@link #nHarmonizationEvaluations(int, SiadapMeritRating)} taking an explicit
     * organicUnitId, so WR-01 regression tests can build a whole unit's worth of evaluations
     * sharing one real organicUnitId.
     */
    private List<SiadapEvaluation> nHarmonizationEvaluations(int count, SiadapMeritRating rating, String organicUnitId) {
        List<SiadapEvaluation> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(buildHarmonizationEvaluation(rating, organicUnitId));
        }
        return list;
    }

    /**
     * Builds a freshly-created evaluation still in phase OPEN (pre-HARMONIZATION) — used to
     * exercise the batch phase guard (Task 3). No merit rating is assigned (mirrors a real
     * not-yet-evaluated evaluation).
     */
    private SiadapEvaluation buildOpenEvaluation() {
        return SiadapEvaluation.create(
                UUID.randomUUID().toString(),
                YEAR,
                null,
                UUID.randomUUID().toString(),
                new BigDecimal("60"),
                new BigDecimal("40"));
    }

    private SiadapConfigEntity buildConfig(BigDecimal excellentQuota, BigDecimal goodQuota, Integer minCollaboratorsForQuota) {
        SiadapConfigEntity config = new SiadapConfigEntity();
        config.setFiscalYear(YEAR);
        config.setExcellentQuota(excellentQuota);
        config.setGoodQuota(goodQuota);
        config.setMinCollaboratorsForQuota(minCollaboratorsForQuota);
        return config;
    }

    private CloseEvaluationsCommand commandFor(Integer year, String organicUnitId) {
        return new CloseEvaluationsCommand(new CloseEvaluationsRequestDTO(year, organicUnitId));
    }

    // ============================================================
    // Behaviour 1: per-unit fetch routing
    // ============================================================

    @Test
    void handleUsesFindByYearAndOrganicUnitIdWhenOrganicUnitIdProvided() {
        List<SiadapEvaluation> evaluations = nHarmonizationEvaluations(3, SiadapMeritRating.REGULAR);
        when(evaluationRepository.findByYearAndOrganicUnitId(eq(YEAR), eq("unit-123"))).thenReturn(evaluations);
        when(evaluationRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<CloseEvaluationsResponseDTO> response = handler.handle(commandFor(YEAR, "unit-123"));

        assertEquals(200, response.getStatusCode().value());
        verify(evaluationRepository, times(1)).findByYearAndOrganicUnitId(eq(YEAR), eq("unit-123"));
        verify(evaluationRepository, never()).findByYear(any());
    }

    @Test
    void handleUsesFindByYearWhenOrganicUnitIdIsNull() {
        List<SiadapEvaluation> evaluations = nHarmonizationEvaluations(3, SiadapMeritRating.REGULAR);
        when(evaluationRepository.findByYear(eq(YEAR))).thenReturn(evaluations);
        when(evaluationRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<CloseEvaluationsResponseDTO> response = handler.handle(commandFor(YEAR, null));

        assertEquals(200, response.getStatusCode().value());
        verify(evaluationRepository, times(1)).findByYear(eq(YEAR));
        verify(evaluationRepository, never()).findByYearAndOrganicUnitId(any(), any());
    }

    @Test
    void handleUsesFindByYearWhenOrganicUnitIdIsBlank() {
        List<SiadapEvaluation> evaluations = nHarmonizationEvaluations(3, SiadapMeritRating.REGULAR);
        when(evaluationRepository.findByYear(eq(YEAR))).thenReturn(evaluations);
        when(evaluationRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<CloseEvaluationsResponseDTO> response = handler.handle(commandFor(YEAR, "   "));

        assertEquals(200, response.getStatusCode().value());
        verify(evaluationRepository, times(1)).findByYear(eq(YEAR));
        verify(evaluationRepository, never()).findByYearAndOrganicUnitId(any(), any());
    }

    // ============================================================
    // Behaviour 2: minCollaboratorsForQuota gate (+ disabled valve)
    // ============================================================

    @Test
    void minCollaboratorsGateThrowsWhenBelowConfiguredMinimum() {
        List<SiadapEvaluation> evaluations = nHarmonizationEvaluations(2, SiadapMeritRating.REGULAR);
        when(evaluationRepository.findByYear(eq(YEAR))).thenReturn(evaluations);
        when(configRepository.findByFiscalYear(YEAR))
                .thenReturn(Optional.of(buildConfig(new BigDecimal("25"), new BigDecimal("35"), 10)));

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(commandFor(YEAR, null)));

        assertEquals(422, exception.getBody().getStatus());
        assertTrue(exception.getBody().getTitle().contains("Mínimo: 10"));
        assertTrue(exception.getBody().getTitle().contains("atual: 2"));
        verify(evaluationRepository, never()).saveAll(any());
    }

    @Test
    void minCollaboratorsGateDisabledWhenConfigNull() {
        List<SiadapEvaluation> evaluations = nHarmonizationEvaluations(2, SiadapMeritRating.REGULAR);
        when(evaluationRepository.findByYear(eq(YEAR))).thenReturn(evaluations);
        when(configRepository.findByFiscalYear(YEAR))
                .thenReturn(Optional.of(buildConfig(new BigDecimal("25"), new BigDecimal("35"), null)));
        when(evaluationRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<CloseEvaluationsResponseDTO> response = handler.handle(commandFor(YEAR, null));

        assertEquals(200, response.getStatusCode().value());
        verify(evaluationRepository, times(1)).saveAll(any());
    }

    @Test
    void minCollaboratorsGateDisabledWhenConfigZero() {
        List<SiadapEvaluation> evaluations = nHarmonizationEvaluations(2, SiadapMeritRating.REGULAR);
        when(evaluationRepository.findByYear(eq(YEAR))).thenReturn(evaluations);
        when(configRepository.findByFiscalYear(YEAR))
                .thenReturn(Optional.of(buildConfig(new BigDecimal("25"), new BigDecimal("35"), 0)));
        when(evaluationRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<CloseEvaluationsResponseDTO> response = handler.handle(commandFor(YEAR, null));

        assertEquals(200, response.getStatusCode().value());
        verify(evaluationRepository, times(1)).saveAll(any());
    }

    // ============================================================
    // Behaviour 3: goodQuota read from config, not hardcoded 35
    // ============================================================

    @Test
    void goodQuotaExceededThrowsWithConfigDrivenPercentage() {
        List<SiadapEvaluation> evaluations = new ArrayList<>();
        evaluations.addAll(nHarmonizationEvaluations(4, SiadapMeritRating.GOOD));
        evaluations.addAll(nHarmonizationEvaluations(6, SiadapMeritRating.REGULAR));
        when(evaluationRepository.findByYear(eq(YEAR))).thenReturn(evaluations);
        // goodQuota = 30 (not the old hardcoded 35): allowed = floor(30*10/100) = 3; assigned = 4 -> violates
        when(configRepository.findByFiscalYear(YEAR))
                .thenReturn(Optional.of(buildConfig(new BigDecimal("25"), new BigDecimal("30"), null)));

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(commandFor(YEAR, null)));

        assertEquals(422, exception.getBody().getStatus());
        assertTrue(exception.getBody().getTitle().contains("Bom"));
        assertTrue(exception.getBody().getTitle().contains("Permitido: 3"));
        assertTrue(exception.getBody().getTitle().contains("Atribuído: 4"));
        verify(evaluationRepository, never()).saveAll(any());
    }

    @Test
    void goodQuotaWithinConfiguredPercentageDoesNotThrow() {
        List<SiadapEvaluation> evaluations = new ArrayList<>();
        evaluations.addAll(nHarmonizationEvaluations(4, SiadapMeritRating.GOOD));
        evaluations.addAll(nHarmonizationEvaluations(6, SiadapMeritRating.REGULAR));
        when(evaluationRepository.findByYear(eq(YEAR))).thenReturn(evaluations);
        // goodQuota = 50: allowed = floor(50*10/100) = 5; assigned = 4 -> compliant
        when(configRepository.findByFiscalYear(YEAR))
                .thenReturn(Optional.of(buildConfig(new BigDecimal("25"), new BigDecimal("50"), null)));
        when(evaluationRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<CloseEvaluationsResponseDTO> response = handler.handle(commandFor(YEAR, null));

        assertEquals(200, response.getStatusCode().value());
        verify(evaluationRepository, times(1)).saveAll(any());
    }

    // ============================================================
    // Behaviour 4: Bom is independent of Excelente
    // ============================================================

    @Test
    void bomThrowsEvenWhenExcelenteIsCompliant() {
        List<SiadapEvaluation> evaluations = new ArrayList<>();
        evaluations.addAll(nHarmonizationEvaluations(2, SiadapMeritRating.EXCELLENT));
        evaluations.addAll(nHarmonizationEvaluations(4, SiadapMeritRating.GOOD));
        evaluations.addAll(nHarmonizationEvaluations(4, SiadapMeritRating.REGULAR));
        when(evaluationRepository.findByYear(eq(YEAR))).thenReturn(evaluations);
        // excellentQuota = 25: allowed = floor(25*10/100) = 2; assigned = 2 -> compliant
        // goodQuota = 30: allowed = floor(30*10/100) = 3; assigned = 4 -> violates
        when(configRepository.findByFiscalYear(YEAR))
                .thenReturn(Optional.of(buildConfig(new BigDecimal("25"), new BigDecimal("30"), null)));

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(commandFor(YEAR, null)));

        assertEquals(422, exception.getBody().getStatus());
        assertTrue(exception.getBody().getTitle().contains("Bom"));
        verify(evaluationRepository, never()).saveAll(any());
    }

    // ============================================================
    // Behaviour 5: compliant path still closes and returns 200
    // ============================================================

    @Test
    void compliantCloseSavesAllAndReturns200() {
        List<SiadapEvaluation> evaluations = new ArrayList<>();
        evaluations.addAll(nHarmonizationEvaluations(1, SiadapMeritRating.EXCELLENT));
        evaluations.addAll(nHarmonizationEvaluations(2, SiadapMeritRating.GOOD));
        evaluations.addAll(nHarmonizationEvaluations(7, SiadapMeritRating.REGULAR));
        when(evaluationRepository.findByYear(eq(YEAR))).thenReturn(evaluations);
        when(configRepository.findByFiscalYear(YEAR))
                .thenReturn(Optional.of(buildConfig(new BigDecimal("25"), new BigDecimal("35"), 2)));
        when(evaluationRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<CloseEvaluationsResponseDTO> response = handler.handle(commandFor(YEAR, null));

        assertEquals(200, response.getStatusCode().value());
        CloseEvaluationsResponseDTO body = response.getBody();
        assertEquals(10, body.getEvaluationsClosed());
        assertEquals(YEAR, body.getYear());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<SiadapEvaluation>> captor = ArgumentCaptor.forClass(List.class);
        verify(evaluationRepository, times(1)).saveAll(captor.capture());
        List<SiadapEvaluation> closed = captor.getValue();
        assertEquals(10, closed.size());
        assertTrue(closed.stream().allMatch(e -> EvaluationPhase.CLOSED.equals(e.getPhase())));
        assertTrue(closed.stream().allMatch(SiadapEvaluation::isValidatedQuota));
    }

    // ============================================================
    // Batch HARMONIZATION-phase guard (Task 3)
    // ============================================================

    @Test
    void mixedPhaseSetThrowsSingleBatchErrorNamingCount() {
        List<SiadapEvaluation> evaluations = new ArrayList<>();
        evaluations.add(buildHarmonizationEvaluation(SiadapMeritRating.REGULAR));
        evaluations.add(buildOpenEvaluation());
        evaluations.add(buildOpenEvaluation());
        when(evaluationRepository.findByYear(eq(YEAR))).thenReturn(evaluations);

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(commandFor(YEAR, null)));

        assertEquals(422, exception.getBody().getStatus());
        assertTrue(exception.getBody().getTitle().contains("2"));
        assertTrue(exception.getBody().getTitle().contains("Harmonização"));
        verify(evaluationRepository, never()).saveAll(any());
    }

    @Test
    void phaseGuardWinsOverQuotaViolationWhenBothConditionsPresent() {
        // Composition would ALSO violate the (default, unconfigured) Bom quota if the phase
        // guard did not intercept first: total=10, default goodQuota=35% -> allowed=floor(35*10/100)=3,
        // but 4 are GOOD. Proves ordering: the phase-guard message must win, not the quota message.
        List<SiadapEvaluation> evaluations = new ArrayList<>();
        evaluations.add(buildOpenEvaluation());
        evaluations.addAll(nHarmonizationEvaluations(4, SiadapMeritRating.GOOD));
        evaluations.addAll(nHarmonizationEvaluations(5, SiadapMeritRating.REGULAR));
        when(evaluationRepository.findByYear(eq(YEAR))).thenReturn(evaluations);

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(commandFor(YEAR, null)));

        assertEquals(422, exception.getBody().getStatus());
        assertTrue(exception.getBody().getTitle().contains("Harmonização"),
                "Expected the phase-guard message to win, got: " + exception.getBody().getTitle());
        assertTrue(exception.getBody().getTitle().contains("1"));
        assertTrue(!exception.getBody().getTitle().contains("Bom"),
                "The quota message must not surface when the phase guard already fired");
        verify(evaluationRepository, never()).saveAll(any());
    }

    // ============================================================
    // WR-01 fix: per-unit quota validation on "close all units" (organicUnitId == null)
    // 81-BACKEND-REVIEW.md WR-01 -- every fixture above hardcodes organicUnitId=null for every
    // evaluation, so none of the tests above actually exercise cross-unit pooling vs. per-unit
    // correctness. These do, using 2+ distinct real organicUnitId values in one close-all call.
    // ============================================================

    @Test
    void closeAllUnitsCatchesPerUnitExcelenteViolationEvenWhenPooledTotalWouldNotViolate() {
        // Concrete scenario from 81-BACKEND-REVIEW.md WR-01: unit A has 2 evaluations, both
        // EXCELLENT (100%); unit B has 8 evaluations, all REGULAR.
        // Pooled (the bug): total=10, excellentAllowed=floor(25*10/100)=2, excellentCount=2
        // -> 2>2 is false -> would NOT violate if validated as one system-wide pool.
        // Per-unit (the fix): unit A alone has total=2, excellentAllowed=floor(25*2/100)=0,
        // excellentCount=2 -> 2>0 -> VIOLATES, and must be caught even though the request's
        // organicUnitId is null (close all units).
        String unitA = UUID.randomUUID().toString();
        String unitB = UUID.randomUUID().toString();

        List<SiadapEvaluation> evaluations = new ArrayList<>();
        evaluations.addAll(nHarmonizationEvaluations(2, SiadapMeritRating.EXCELLENT, unitA));
        evaluations.addAll(nHarmonizationEvaluations(8, SiadapMeritRating.REGULAR, unitB));
        when(evaluationRepository.findByYear(eq(YEAR))).thenReturn(evaluations);
        when(configRepository.findByFiscalYear(YEAR))
                .thenReturn(Optional.of(buildConfig(new BigDecimal("25"), new BigDecimal("35"), null)));

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(commandFor(YEAR, null)));

        assertEquals(422, exception.getBody().getStatus());
        assertTrue(exception.getBody().getTitle().contains("Excelente"),
                "Expected the per-unit Excelente violation to be caught, got: " + exception.getBody().getTitle());
        verify(evaluationRepository, never()).saveAll(any());
    }

    @Test
    void closeAllUnitsSucceedsWhenEveryDistinctUnitIsIndividuallyCompliant() {
        // Companion/negative case, same shape but 2 distinct real organic units where BOTH are
        // individually compliant: unit A has 1 EXCELLENT out of 4 (excellentAllowed=
        // floor(25*4/100)=1, excellentCount=1 -> compliant, boundary exactly met); unit B has 6,
        // all REGULAR (trivially compliant). Confirms the per-unit fix does not over-trigger when
        // every distinct unit is genuinely within its own quota.
        String unitA = UUID.randomUUID().toString();
        String unitB = UUID.randomUUID().toString();

        List<SiadapEvaluation> evaluations = new ArrayList<>();
        evaluations.addAll(nHarmonizationEvaluations(1, SiadapMeritRating.EXCELLENT, unitA));
        evaluations.addAll(nHarmonizationEvaluations(3, SiadapMeritRating.REGULAR, unitA));
        evaluations.addAll(nHarmonizationEvaluations(6, SiadapMeritRating.REGULAR, unitB));
        when(evaluationRepository.findByYear(eq(YEAR))).thenReturn(evaluations);
        when(configRepository.findByFiscalYear(YEAR))
                .thenReturn(Optional.of(buildConfig(new BigDecimal("25"), new BigDecimal("35"), null)));
        when(evaluationRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<CloseEvaluationsResponseDTO> response = handler.handle(commandFor(YEAR, null));

        assertEquals(200, response.getStatusCode().value());
        assertEquals(10, response.getBody().getEvaluationsClosed());
        verify(evaluationRepository, times(1)).saveAll(any());
    }

    // ============================================================
    // WR-02 fix: close response normalizes a blank organicUnitId to null
    // ============================================================

    @Test
    void closeAllUnitsNormalizesBlankOrganicUnitIdToNullInResponse() {
        List<SiadapEvaluation> evaluations = nHarmonizationEvaluations(3, SiadapMeritRating.REGULAR);
        when(evaluationRepository.findByYear(eq(YEAR))).thenReturn(evaluations);
        when(evaluationRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<CloseEvaluationsResponseDTO> response = handler.handle(commandFor(YEAR, "   "));

        assertEquals(200, response.getStatusCode().value());
        assertNull(response.getBody().getOrganicUnitId(),
                "A whitespace-only organicUnitId must not be echoed back as if a specific unit was scoped");
    }
}
