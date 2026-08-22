package cv.igrp.RH_Service.sigdi.infrastructure.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.sigdi.application.constants.EvaluationPhase;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapEvaluation;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapEvaluationRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.IndividualObjective;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.PaaSubmissionPeriod;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PaaSubmissionPeriodRepository;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
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
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Mirrors {@link SelfEvaluationOpeningSchedulerTest} (Phase 102), inverting phase and condition:
 * this scheduler looks at {@link EvaluationPhase#SELF_EVALUATION} evaluations and acts when the
 * individual {@code SIADAP_SELF_EVAL} window is {@code isEmpty()} -- the opposite of the Phase
 * 102 scheduler, which acts on {@code isPresent()}. Same Mockito idioms, no Spring container, no
 * Docker.
 */
@ExtendWith(MockitoExtension.class)
class SelfEvaluationTacitAcceptanceSchedulerTest {

    private static final Integer YEAR = 2026;
    private static final Integer OTHER_YEAR = 2025;

    @Mock
    private SiadapEvaluationRepository evaluationRepository;

    @Mock
    private PaaSubmissionPeriodRepository periodRepository;

    @InjectMocks
    private SelfEvaluationTacitAcceptanceScheduler scheduler;

    /**
     * Builds an evaluation in {@link EvaluationPhase#SELF_EVALUATION} via the real domain
     * transitions -- {@code create(...).contractualizeObjectives(objs).acceptObjectives()
     * .openSelfEvaluationPhase()} -- never assembled through the persistence-rehydration factory
     * with a forced phase, for the same reason the Phase 102 mould gives: a future change to the
     * guard on {@code applyTacitSelfEvaluationAcceptance()} would surface here too.
     */
    private SiadapEvaluation buildSelfEvaluationEvaluation(int year) {
        SiadapEvaluation created = SiadapEvaluation.create(
                UUID.randomUUID().toString(),
                year,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                new BigDecimal("60"),
                new BigDecimal("40"));

        List<IndividualObjective> objectives = List.of(
                IndividualObjective.create("OBJ-1", "Descrição do objetivo 1", "Indicador 1", new BigDecimal("100"), new BigDecimal("40")),
                IndividualObjective.create("OBJ-2", "Descrição do objetivo 2", "Indicador 2", new BigDecimal("100"), new BigDecimal("30")),
                IndividualObjective.create("OBJ-3", "Descrição do objetivo 3", "Indicador 3", new BigDecimal("100"), new BigDecimal("30")));

        return created.contractualizeObjectives(objectives).acceptObjectives().openSelfEvaluationPhase();
    }

    /**
     * Builds an evaluation still in {@link EvaluationPhase#IN_PROGRESS} (stopped one transition
     * earlier than {@link #buildSelfEvaluationEvaluation}) to simulate a data inconsistency that
     * makes {@code applyTacitSelfEvaluationAcceptance()} reject it -- used by test 7 to prove one
     * bad evaluation does not abort the batch.
     */
    private SiadapEvaluation buildInProgressEvaluation(int year) {
        SiadapEvaluation created = SiadapEvaluation.create(
                UUID.randomUUID().toString(),
                year,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                new BigDecimal("60"),
                new BigDecimal("40"));

        List<IndividualObjective> objectives = List.of(
                IndividualObjective.create("OBJ-1", "Descrição do objetivo 1", "Indicador 1", new BigDecimal("100"), new BigDecimal("40")),
                IndividualObjective.create("OBJ-2", "Descrição do objetivo 2", "Indicador 2", new BigDecimal("100"), new BigDecimal("30")),
                IndividualObjective.create("OBJ-3", "Descrição do objetivo 3", "Indicador 3", new BigDecimal("100"), new BigDecimal("30")));

        return created.contractualizeObjectives(objectives).acceptObjectives();
    }

    @Test
    void appliesTacitAcceptanceWhenWindowIsInactiveForTheEvaluationYear() {
        SiadapEvaluation evaluation = buildSelfEvaluationEvaluation(YEAR);

        when(evaluationRepository.findAll(any(), any(), eq(EvaluationPhase.SELF_EVALUATION), anyInt(), anyInt()))
                .thenReturn(List.of(evaluation));
        when(periodRepository.findActiveByTypeAndYearAndPurpose(PaaLevel.INDIVIDUAL_LEVEL, YEAR, Purpose.SIADAP_SELF_EVAL))
                .thenReturn(Optional.empty());
        when(evaluationRepository.save(any(SiadapEvaluation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        scheduler.processTacitSelfEvaluationAcceptances();

        ArgumentCaptor<SiadapEvaluation> captor = ArgumentCaptor.forClass(SiadapEvaluation.class);
        verify(evaluationRepository).save(captor.capture());
        assertEquals(EvaluationPhase.MANAGER_EVALUATION, captor.getValue().getPhase());
        assertNull(captor.getValue().getSelfEvaluationScore());
        assertTrue(captor.getValue().isSelfEvaluationTacitlyAccepted());
    }

    @Test
    void doesNotApplyTacitAcceptanceWhenWindowIsStillActive() {
        SiadapEvaluation evaluation = buildSelfEvaluationEvaluation(YEAR);
        PaaSubmissionPeriod activePeriod = mock(PaaSubmissionPeriod.class);

        when(evaluationRepository.findAll(any(), any(), eq(EvaluationPhase.SELF_EVALUATION), anyInt(), anyInt()))
                .thenReturn(List.of(evaluation));
        when(periodRepository.findActiveByTypeAndYearAndPurpose(any(), eq(YEAR), any()))
                .thenReturn(Optional.of(activePeriod));

        scheduler.processTacitSelfEvaluationAcceptances();

        verify(evaluationRepository, never()).save(any());
    }

    /**
     * Deliberately identical in outcome to {@code doesNotApplyTacitAcceptanceWhenWindowIsStillActive},
     * proven through another data path -- a different evaluation year, still with an active
     * window. The predicate does not treat any year specially: presence blocks the tácito
     * regardless of which year is being examined. The redundancy between this test and the
     * previous one mirrors the Phase 102 mould's rationale for its own pair of "does not act"
     * tests: a future regression that special-cases a year would break silently without it.
     */
    @Test
    void doesNotApplyTacitAcceptanceWhenWindowWasNeverConfiguredForAnotherActiveYear() {
        SiadapEvaluation evaluation = buildSelfEvaluationEvaluation(OTHER_YEAR);
        PaaSubmissionPeriod activePeriod = mock(PaaSubmissionPeriod.class);

        when(evaluationRepository.findAll(any(), any(), eq(EvaluationPhase.SELF_EVALUATION), anyInt(), anyInt()))
                .thenReturn(List.of(evaluation));
        when(periodRepository.findActiveByTypeAndYearAndPurpose(any(), eq(OTHER_YEAR), any()))
                .thenReturn(Optional.of(activePeriod));

        scheduler.processTacitSelfEvaluationAcceptances();

        verify(evaluationRepository, never()).save(any());
    }

    @Test
    void queriesOnlySelfEvaluationEvaluationsWithoutYearOrOrganicUnitFilter() {
        ArgumentCaptor<Integer> yearCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<String> unitCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<EvaluationPhase> phaseCaptor = ArgumentCaptor.forClass(EvaluationPhase.class);

        when(evaluationRepository.findAll(yearCaptor.capture(), unitCaptor.capture(), phaseCaptor.capture(), anyInt(), anyInt()))
                .thenReturn(List.of());

        scheduler.processTacitSelfEvaluationAcceptances();

        assertNull(yearCaptor.getValue());
        assertNull(unitCaptor.getValue());
        assertEquals(EvaluationPhase.SELF_EVALUATION, phaseCaptor.getValue());
    }

    @Test
    void asksForTheIndividualLevelSelfEvaluationWindowOfTheEvaluationYear() {
        SiadapEvaluation evaluation = buildSelfEvaluationEvaluation(YEAR);

        when(evaluationRepository.findAll(any(), any(), eq(EvaluationPhase.SELF_EVALUATION), anyInt(), anyInt()))
                .thenReturn(List.of(evaluation));

        ArgumentCaptor<PaaLevel> levelCaptor = ArgumentCaptor.forClass(PaaLevel.class);
        ArgumentCaptor<Integer> yearCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Purpose> purposeCaptor = ArgumentCaptor.forClass(Purpose.class);
        when(periodRepository.findActiveByTypeAndYearAndPurpose(levelCaptor.capture(), yearCaptor.capture(), purposeCaptor.capture()))
                .thenReturn(Optional.of(mock(PaaSubmissionPeriod.class)));

        scheduler.processTacitSelfEvaluationAcceptances();

        assertEquals(PaaLevel.INDIVIDUAL_LEVEL, levelCaptor.getValue());
        assertEquals(YEAR, yearCaptor.getValue());
        assertEquals(Purpose.SIADAP_SELF_EVAL, purposeCaptor.getValue());
    }

    @Test
    void queriesTheWindowOncePerYearEvenWithSeveralEvaluationsOfThatYear() {
        SiadapEvaluation first = buildSelfEvaluationEvaluation(YEAR);
        SiadapEvaluation second = buildSelfEvaluationEvaluation(YEAR);
        SiadapEvaluation third = buildSelfEvaluationEvaluation(YEAR);

        when(evaluationRepository.findAll(any(), any(), eq(EvaluationPhase.SELF_EVALUATION), anyInt(), anyInt()))
                .thenReturn(List.of(first, second, third));
        when(periodRepository.findActiveByTypeAndYearAndPurpose(any(), eq(YEAR), any()))
                .thenReturn(Optional.of(mock(PaaSubmissionPeriod.class)));

        scheduler.processTacitSelfEvaluationAcceptances();

        verify(periodRepository, times(1)).findActiveByTypeAndYearAndPurpose(any(), eq(YEAR), any());
    }

    @Test
    void continuesProcessingTheBatchWhenOneEvaluationFails() {
        SiadapEvaluation succeedingFirst = buildSelfEvaluationEvaluation(YEAR);
        // Still IN_PROGRESS -- applyTacitSelfEvaluationAcceptance() rejects it, the catch absorbs it.
        SiadapEvaluation failing = buildInProgressEvaluation(YEAR);
        SiadapEvaluation succeedingThird = buildSelfEvaluationEvaluation(YEAR);

        when(evaluationRepository.findAll(any(), any(), eq(EvaluationPhase.SELF_EVALUATION), anyInt(), anyInt()))
                .thenReturn(List.of(succeedingFirst, failing, succeedingThird));
        when(periodRepository.findActiveByTypeAndYearAndPurpose(any(), eq(YEAR), any()))
                .thenReturn(Optional.empty());
        when(evaluationRepository.save(any(SiadapEvaluation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        scheduler.processTacitSelfEvaluationAcceptances();

        ArgumentCaptor<SiadapEvaluation> captor = ArgumentCaptor.forClass(SiadapEvaluation.class);
        verify(evaluationRepository, times(2)).save(captor.capture());
        List<SiadapEvaluation> saved = captor.getAllValues();
        assertEquals(succeedingFirst.getId(), saved.get(0).getId());
        assertEquals(succeedingThird.getId(), saved.get(1).getId());
    }

    @Test
    void processesEveryPageUntilAPartialBatchIsReturned() {
        List<SiadapEvaluation> firstPage = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            firstPage.add(buildSelfEvaluationEvaluation(YEAR));
        }
        List<SiadapEvaluation> secondPage = List.of(buildSelfEvaluationEvaluation(YEAR));

        when(evaluationRepository.findAll(any(), any(), eq(EvaluationPhase.SELF_EVALUATION), eq(0), anyInt()))
                .thenReturn(firstPage);
        when(evaluationRepository.findAll(any(), any(), eq(EvaluationPhase.SELF_EVALUATION), eq(1), anyInt()))
                .thenReturn(secondPage);
        when(periodRepository.findActiveByTypeAndYearAndPurpose(any(), eq(YEAR), any()))
                .thenReturn(Optional.empty());
        when(evaluationRepository.save(any(SiadapEvaluation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        scheduler.processTacitSelfEvaluationAcceptances();

        verify(evaluationRepository, times(101)).save(any());
        verify(evaluationRepository, times(2)).findAll(any(), any(), any(), anyInt(), anyInt());
    }

    @Test
    void scheduledCronDefaultsToOneAmAndStaysOverridable() throws NoSuchMethodException {
        Method method = SelfEvaluationTacitAcceptanceScheduler.class.getMethod("processTacitSelfEvaluationAcceptances");
        Scheduled scheduled = method.getAnnotation(Scheduled.class);

        assertEquals("${sigdi.siadap.self-evaluation-tacit-acceptance.cron:0 0 1 * * ?}", scheduled.cron());
    }

    @Test
    void hasNoActorResolvingDependency() {
        Constructor<?>[] constructors = SelfEvaluationTacitAcceptanceScheduler.class.getDeclaredConstructors();
        assertEquals(1, constructors.length);

        Class<?>[] parameterTypes = constructors[0].getParameterTypes();
        assertEquals(2, parameterTypes.length);
        assertEquals(SiadapEvaluationRepository.class, parameterTypes[0]);
        assertEquals(PaaSubmissionPeriodRepository.class, parameterTypes[1]);
    }
}
