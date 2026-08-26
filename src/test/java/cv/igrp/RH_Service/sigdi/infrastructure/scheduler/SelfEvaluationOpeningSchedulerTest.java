package cv.igrp.RH_Service.sigdi.infrastructure.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.sigdi.application.constants.EvaluationPhase;
import cv.igrp.RH_Service.sigdi.application.service.SelfEvaluationWindowPolicy;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapEvaluation;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapEvaluationRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.IndividualObjective;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * First scheduler test of the project. {@link TacitAcceptanceScheduler} has none, and serves
 * only as a negative reference for what not to copy. There is no internal scheduler-test mould
 * to follow, so the style below borrows from {@code CreatePaaSubmissionPeriodCommandHandlerTest}
 * (Mockito idioms), not from any prior {@code @Scheduled} test.
 * <p>
 * {@code processSelfEvaluationOpenings()} is public and takes no arguments, so it is invoked
 * directly like any other method. {@code @Scheduled} and {@code @Transactional} are metadata
 * read by the Spring container; without a container they do nothing, and nothing here needs
 * them to do anything -- what is exercised is the method body with its two ports mocked. The
 * one place the {@code @Scheduled} annotation itself matters is asserted by reflection (test 9).
 * No Spring test slice is started here and no full application context is bootstrapped, and no
 * integration test class is added either: there is no Docker on this machine and the Surefire
 * configuration excludes that category of test.
 */
@ExtendWith(MockitoExtension.class)
class SelfEvaluationOpeningSchedulerTest {

    private static final Integer YEAR = 2026;

    @Mock
    private SiadapEvaluationRepository evaluationRepository;

    @Mock
    private SelfEvaluationWindowPolicy windowPolicy;

    @InjectMocks
    private SelfEvaluationOpeningScheduler scheduler;

    /**
     * Builds an evaluation in {@link EvaluationPhase#IN_PROGRESS} via the real domain
     * transitions -- {@code create(...).contractualizeObjectives(objs).acceptObjectives()} --
     * copied from {@code SubmitSelfEvaluationCommandHandlerTest.buildPendingSelfEvaluationEvaluation()}
     * and stopped one transition earlier. Never assembled through the persistence-rehydration
     * factory with a forced phase: using the real transitions means a future change to the
     * guard on {@code openSelfEvaluationPhase()} would surface here too.
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

    /**
     * Builds an evaluation still in {@link EvaluationPhase#OPEN} (only {@code create(...)}, no
     * objectives accepted) to simulate a data inconsistency that makes
     * {@code openSelfEvaluationPhase()} reject it -- used by test 7 to prove one bad evaluation
     * does not abort the batch.
     */
    private SiadapEvaluation buildOpenEvaluation(int year) {
        return SiadapEvaluation.create(
                UUID.randomUUID().toString(),
                year,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                new BigDecimal("60"),
                new BigDecimal("40"));
    }

    @Test
    void opensSelfEvaluationPhaseWhenWindowIsActiveForTheEvaluationYear() {
        SiadapEvaluation evaluation = buildInProgressEvaluation(YEAR);

        when(evaluationRepository.findAll(any(), any(), eq(EvaluationPhase.IN_PROGRESS), anyInt(), anyInt()))
                .thenReturn(List.of(evaluation));
        when(windowPolicy.isOpenFor(YEAR)).thenReturn(true);
        when(evaluationRepository.save(any(SiadapEvaluation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        scheduler.processSelfEvaluationOpenings();

        ArgumentCaptor<SiadapEvaluation> captor = ArgumentCaptor.forClass(SiadapEvaluation.class);
        verify(evaluationRepository).save(captor.capture());
        assertEquals(EvaluationPhase.SELF_EVALUATION, captor.getValue().getPhase());
    }

    @Test
    void doesNotOpenWhenWindowWasClosedManuallyByRh() {
        SiadapEvaluation evaluation = buildInProgressEvaluation(YEAR);

        when(evaluationRepository.findAll(any(), any(), eq(EvaluationPhase.IN_PROGRESS), anyInt(), anyInt()))
                .thenReturn(List.of(evaluation));
        when(windowPolicy.isOpenFor(YEAR)).thenReturn(false);

        scheduler.processSelfEvaluationOpenings();

        verify(evaluationRepository, never()).save(any());
    }

    /**
     * Deliberately identical, in arrangement and in result, to
     * {@code doesNotOpenWhenWindowWasClosedManuallyByRh}. Before this task, the redundancy proved
     * that {@code findActiveByTypeAndYearAndPurpose} -- backed by
     * {@code PaaSubmissionPeriod.isActiveToday()} -- folds "closed manually" and "end date already
     * passed" into the very same {@code Optional.empty()} signal, one level below this scheduler.
     * <b>Conversion note (Task 2 of the window-policy extraction, 111-01):</b> that unification now
     * happens one layer further down, inside {@link SelfEvaluationWindowPolicy}, which this
     * scheduler only ever sees as a single {@code boolean}. From this scheduler's vantage point the
     * two tests are now literally identical -- both stub {@code windowPolicy.isOpenFor(YEAR)} to
     * {@code false} and assert the same non-save. Kept rather than deleted, per plan instruction:
     * this is exactly the "case that may lose sense after conversion" the plan asked to flag, not
     * hide. The real distinction between "closed manually" and "expired naturally" is proven once,
     * at the source, by {@code SelfEvaluationWindowPolicyTest} and the two symmetric cases in
     * {@code PaaSubmissionPeriodRepositoryImpl}'s own tests -- not here anymore.
     */
    @Test
    void doesNotOpenWhenWindowExpiredNaturally() {
        SiadapEvaluation evaluation = buildInProgressEvaluation(YEAR);

        when(evaluationRepository.findAll(any(), any(), eq(EvaluationPhase.IN_PROGRESS), anyInt(), anyInt()))
                .thenReturn(List.of(evaluation));
        when(windowPolicy.isOpenFor(YEAR)).thenReturn(false);

        scheduler.processSelfEvaluationOpenings();

        verify(evaluationRepository, never()).save(any());
    }

    @Test
    void queriesOnlyInProgressEvaluationsWithoutYearOrOrganicUnitFilter() {
        ArgumentCaptor<Integer> yearCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<String> unitCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<EvaluationPhase> phaseCaptor = ArgumentCaptor.forClass(EvaluationPhase.class);

        when(evaluationRepository.findAll(yearCaptor.capture(), unitCaptor.capture(), phaseCaptor.capture(), anyInt(), anyInt()))
                .thenReturn(List.of());

        scheduler.processSelfEvaluationOpenings();

        assertNull(yearCaptor.getValue());
        assertNull(unitCaptor.getValue());
        assertEquals(EvaluationPhase.IN_PROGRESS, phaseCaptor.getValue());
    }

    /**
     * <b>Conversion note (Task 2, 111-01):</b> before the extraction, this test asserted the full
     * triplet {@code (PaaLevel.INDIVIDUAL_LEVEL, year, Purpose.SIADAP_SELF_EVAL)} received by
     * {@code periodRepository}. That assertion moved to {@code SelfEvaluationWindowPolicyTest}
     * (Task 1), which is now the only place the triplet is checked -- this scheduler no longer
     * knows the triplet exists, it only knows a year goes in and a boolean comes out. What this
     * test still owns, and keeps asserting, is that the year it passes to
     * {@link SelfEvaluationWindowPolicy#isOpenFor(Integer)} is the evaluation's own year.
     */
    @Test
    void asksTheWindowPolicyForTheEvaluationYear() {
        SiadapEvaluation evaluation = buildInProgressEvaluation(YEAR);

        when(evaluationRepository.findAll(any(), any(), eq(EvaluationPhase.IN_PROGRESS), anyInt(), anyInt()))
                .thenReturn(List.of(evaluation));

        ArgumentCaptor<Integer> yearCaptor = ArgumentCaptor.forClass(Integer.class);
        when(windowPolicy.isOpenFor(yearCaptor.capture())).thenReturn(false);

        scheduler.processSelfEvaluationOpenings();

        assertEquals(YEAR, yearCaptor.getValue());
    }

    @Test
    void queriesTheWindowOncePerYearEvenWithSeveralEvaluationsOfThatYear() {
        SiadapEvaluation first = buildInProgressEvaluation(YEAR);
        SiadapEvaluation second = buildInProgressEvaluation(YEAR);
        SiadapEvaluation third = buildInProgressEvaluation(YEAR);

        when(evaluationRepository.findAll(any(), any(), eq(EvaluationPhase.IN_PROGRESS), anyInt(), anyInt()))
                .thenReturn(List.of(first, second, third));
        when(windowPolicy.isOpenFor(YEAR)).thenReturn(false);

        scheduler.processSelfEvaluationOpenings();

        verify(windowPolicy, times(1)).isOpenFor(YEAR);
    }

    @Test
    void continuesProcessingTheBatchWhenOneEvaluationFails() {
        // Still OPEN -- openSelfEvaluationPhase() rejects it, the catch absorbs the exception.
        SiadapEvaluation failing = buildOpenEvaluation(YEAR);
        SiadapEvaluation succeeding = buildInProgressEvaluation(YEAR);

        when(evaluationRepository.findAll(any(), any(), eq(EvaluationPhase.IN_PROGRESS), anyInt(), anyInt()))
                .thenReturn(List.of(failing, succeeding));
        when(windowPolicy.isOpenFor(YEAR)).thenReturn(true);
        when(evaluationRepository.save(any(SiadapEvaluation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        scheduler.processSelfEvaluationOpenings();

        ArgumentCaptor<SiadapEvaluation> captor = ArgumentCaptor.forClass(SiadapEvaluation.class);
        verify(evaluationRepository, times(1)).save(captor.capture());
        assertEquals(succeeding.getId(), captor.getValue().getId());
    }

    @Test
    void processesEveryPageUntilAPartialBatchIsReturned() {
        List<SiadapEvaluation> firstPage = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            firstPage.add(buildInProgressEvaluation(YEAR));
        }
        List<SiadapEvaluation> secondPage = List.of(buildInProgressEvaluation(YEAR));

        when(evaluationRepository.findAll(any(), any(), eq(EvaluationPhase.IN_PROGRESS), eq(0), anyInt()))
                .thenReturn(firstPage);
        when(evaluationRepository.findAll(any(), any(), eq(EvaluationPhase.IN_PROGRESS), eq(1), anyInt()))
                .thenReturn(secondPage);
        when(windowPolicy.isOpenFor(YEAR)).thenReturn(true);
        when(evaluationRepository.save(any(SiadapEvaluation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        scheduler.processSelfEvaluationOpenings();

        verify(evaluationRepository, times(101)).save(any());
        verify(evaluationRepository, times(2)).findAll(any(), any(), any(), anyInt(), anyInt());
    }

    @Test
    void scheduledCronDefaultsToOneAmAndStaysOverridable() throws NoSuchMethodException {
        Method method = SelfEvaluationOpeningScheduler.class.getMethod("processSelfEvaluationOpenings");
        Scheduled scheduled = method.getAnnotation(Scheduled.class);

        assertEquals("${sigdi.siadap.self-evaluation-opening.cron:0 0 1 * * ?}", scheduled.cron());
    }

    @Test
    void hasNoActorResolvingDependency() {
        Constructor<?>[] constructors = SelfEvaluationOpeningScheduler.class.getDeclaredConstructors();
        assertEquals(1, constructors.length);

        Class<?>[] parameterTypes = constructors[0].getParameterTypes();
        assertEquals(2, parameterTypes.length);
        assertEquals(SiadapEvaluationRepository.class, parameterTypes[0]);
        assertEquals(SelfEvaluationWindowPolicy.class, parameterTypes[1]);
    }
}
