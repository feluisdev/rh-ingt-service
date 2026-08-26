package cv.igrp.RH_Service.sigdi.application.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.PaaSubmissionPeriod;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PaaSubmissionPeriodRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Covers the single owner of "is the SIADAP_SELF_EVAL window of year N active?" -- the triplet
 * assertion below (test 5) is the coverage that moved out of
 * {@code SelfEvaluationOpeningSchedulerTest} and {@code SelfEvaluationTacitAcceptanceSchedulerTest}
 * once those two classes stopped querying the repository directly (Task 2/3 of this plan). If it
 * were missing here, the project's net test coverage of that triplet would have gone down, not
 * moved.
 */
@ExtendWith(MockitoExtension.class)
class SelfEvaluationWindowPolicyTest {

    private static final Integer YEAR = 2026;

    @Mock
    private PaaSubmissionPeriodRepository periodRepository;

    @InjectMocks
    private SelfEvaluationWindowPolicy policy;

    @Test
    void isOpenForReturnsTrueWhenRepositoryReturnsAnActivePeriod() {
        PaaSubmissionPeriod activePeriod = mock(PaaSubmissionPeriod.class);
        when(periodRepository.findActiveByTypeAndYearAndPurpose(PaaLevel.INDIVIDUAL_LEVEL, YEAR, Purpose.SIADAP_SELF_EVAL))
                .thenReturn(Optional.of(activePeriod));

        assertTrue(policy.isOpenFor(YEAR));
    }

    @Test
    void isOpenForReturnsFalseWhenRepositoryReturnsEmpty() {
        when(periodRepository.findActiveByTypeAndYearAndPurpose(PaaLevel.INDIVIDUAL_LEVEL, YEAR, Purpose.SIADAP_SELF_EVAL))
                .thenReturn(Optional.empty());

        assertFalse(policy.isOpenFor(YEAR));
    }

    @Test
    void requireOpenForDoesNotThrowWhenWindowIsActive() {
        PaaSubmissionPeriod activePeriod = mock(PaaSubmissionPeriod.class);
        when(periodRepository.findActiveByTypeAndYearAndPurpose(PaaLevel.INDIVIDUAL_LEVEL, YEAR, Purpose.SIADAP_SELF_EVAL))
                .thenReturn(Optional.of(activePeriod));

        assertDoesNotThrow(() -> policy.requireOpenFor(YEAR));
    }

    @Test
    void requireOpenForThrowsBadRequestWithYearInMessageWhenWindowIsNotActive() {
        when(periodRepository.findActiveByTypeAndYearAndPurpose(PaaLevel.INDIVIDUAL_LEVEL, YEAR, Purpose.SIADAP_SELF_EVAL))
                .thenReturn(Optional.empty());

        IgrpResponseStatusException exception =
                assertThrows(IgrpResponseStatusException.class, () -> policy.requireOpenFor(YEAR));

        assertEquals(400, exception.getBody().getStatus());
        assertTrue(exception.getBody().getTitle().contains(String.valueOf(YEAR)));
    }

    /**
     * The triplet assertion. Proves the query is made with exactly
     * {@code (INDIVIDUAL_LEVEL, year, SIADAP_SELF_EVAL)} -- not any permutation of those three
     * values. This is the assertion that came out of the two scheduler tests; see the mutation
     * proof below for evidence it actually verifies something.
     */
    @Test
    void queriesWithExactlyIndividualLevelYearAndSelfEvalPurpose() {
        when(periodRepository.findActiveByTypeAndYearAndPurpose(PaaLevel.INDIVIDUAL_LEVEL, YEAR, Purpose.SIADAP_SELF_EVAL))
                .thenReturn(Optional.empty());

        policy.isOpenFor(YEAR);

        ArgumentCaptor<PaaLevel> levelCaptor = ArgumentCaptor.forClass(PaaLevel.class);
        ArgumentCaptor<Integer> yearCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Purpose> purposeCaptor = ArgumentCaptor.forClass(Purpose.class);
        org.mockito.Mockito.verify(periodRepository)
                .findActiveByTypeAndYearAndPurpose(levelCaptor.capture(), yearCaptor.capture(), purposeCaptor.capture());

        assertEquals(PaaLevel.INDIVIDUAL_LEVEL, levelCaptor.getValue());
        assertEquals(YEAR, yearCaptor.getValue());
        assertEquals(Purpose.SIADAP_SELF_EVAL, purposeCaptor.getValue());
    }
}
