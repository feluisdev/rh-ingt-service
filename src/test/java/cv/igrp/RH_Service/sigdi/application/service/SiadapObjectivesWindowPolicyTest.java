package cv.igrp.RH_Service.sigdi.application.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
 * Covers the single owner of "is the individual-level SIADAP contractualization/revision window
 * of year N active?" -- {@code SiadapObjectivesWindowPolicy} replaces the six in-line/missing
 * queries that {@code A-132-114}/{@code A-132-115} found (Plano 136-05) with two named variants
 * over the same {@code (INDIVIDUAL_LEVEL, year, Purpose)} criterion.
 */
@ExtendWith(MockitoExtension.class)
class SiadapObjectivesWindowPolicyTest {

    private static final Integer YEAR = 2026;

    @Mock
    private PaaSubmissionPeriodRepository periodRepository;

    @InjectMocks
    private SiadapObjectivesWindowPolicy policy;

    @Test
    void requireContractualizationOpenForDoesNotThrowWhenWindowIsActive() {
        PaaSubmissionPeriod activePeriod = mock(PaaSubmissionPeriod.class);
        when(periodRepository.findActiveByTypeAndYearAndPurpose(PaaLevel.INDIVIDUAL_LEVEL, YEAR, Purpose.SIADAP))
                .thenReturn(Optional.of(activePeriod));

        assertDoesNotThrow(() -> policy.requireContractualizationOpenFor(YEAR));
    }

    @Test
    void requireContractualizationOpenForThrowsBadRequestWhenWindowIsAbsent() {
        when(periodRepository.findActiveByTypeAndYearAndPurpose(PaaLevel.INDIVIDUAL_LEVEL, YEAR, Purpose.SIADAP))
                .thenReturn(Optional.empty());

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> policy.requireContractualizationOpenFor(YEAR));

        assertEquals(400, exception.getBody().getStatus());
        assertEquals("Prazo não configurado para este ano", exception.getBody().getTitle());
    }

    @Test
    void requireRevisionOpenForDoesNotThrowWhenWindowIsActive() {
        PaaSubmissionPeriod activePeriod = mock(PaaSubmissionPeriod.class);
        when(periodRepository.findActiveByTypeAndYearAndPurpose(PaaLevel.INDIVIDUAL_LEVEL, YEAR, Purpose.SIADAP_INTERIM))
                .thenReturn(Optional.of(activePeriod));

        assertDoesNotThrow(() -> policy.requireRevisionOpenFor(YEAR));
    }

    @Test
    void requireRevisionOpenForThrowsBadRequestWhenWindowIsAbsent() {
        when(periodRepository.findActiveByTypeAndYearAndPurpose(PaaLevel.INDIVIDUAL_LEVEL, YEAR, Purpose.SIADAP_INTERIM))
                .thenReturn(Optional.empty());

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> policy.requireRevisionOpenFor(YEAR));

        assertEquals(400, exception.getBody().getStatus());
        assertEquals("Prazo não configurado para este ano", exception.getBody().getTitle());
    }

    /**
     * A {@code null} year is not special-cased with a dedicated guard -- it is recused by the
     * same path as any other year with no active window, because the repository simply has
     * nothing to match against a null year.
     */
    @Test
    void requireContractualizationOpenForThrowsBadRequestWhenYearIsNull() {
        when(periodRepository.findActiveByTypeAndYearAndPurpose(PaaLevel.INDIVIDUAL_LEVEL, null, Purpose.SIADAP))
                .thenReturn(Optional.empty());

        IgrpResponseStatusException exception = assertThrows(IgrpResponseStatusException.class,
                () -> policy.requireContractualizationOpenFor(null));

        assertEquals(400, exception.getBody().getStatus());
    }

    @Test
    void requireContractualizationOpenForQueriesWithExactlyIndividualLevelYearAndSiadapPurpose() {
        when(periodRepository.findActiveByTypeAndYearAndPurpose(PaaLevel.INDIVIDUAL_LEVEL, YEAR, Purpose.SIADAP))
                .thenReturn(Optional.of(mock(PaaSubmissionPeriod.class)));

        policy.requireContractualizationOpenFor(YEAR);

        ArgumentCaptor<PaaLevel> levelCaptor = ArgumentCaptor.forClass(PaaLevel.class);
        ArgumentCaptor<Integer> yearCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Purpose> purposeCaptor = ArgumentCaptor.forClass(Purpose.class);
        org.mockito.Mockito.verify(periodRepository)
                .findActiveByTypeAndYearAndPurpose(levelCaptor.capture(), yearCaptor.capture(), purposeCaptor.capture());

        assertEquals(PaaLevel.INDIVIDUAL_LEVEL, levelCaptor.getValue());
        assertEquals(YEAR, yearCaptor.getValue());
        assertEquals(Purpose.SIADAP, purposeCaptor.getValue());
    }

    @Test
    void requireRevisionOpenForQueriesWithExactlyIndividualLevelYearAndSiadapInterimPurpose() {
        when(periodRepository.findActiveByTypeAndYearAndPurpose(PaaLevel.INDIVIDUAL_LEVEL, YEAR, Purpose.SIADAP_INTERIM))
                .thenReturn(Optional.of(mock(PaaSubmissionPeriod.class)));

        policy.requireRevisionOpenFor(YEAR);

        ArgumentCaptor<PaaLevel> levelCaptor = ArgumentCaptor.forClass(PaaLevel.class);
        ArgumentCaptor<Integer> yearCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Purpose> purposeCaptor = ArgumentCaptor.forClass(Purpose.class);
        org.mockito.Mockito.verify(periodRepository)
                .findActiveByTypeAndYearAndPurpose(levelCaptor.capture(), yearCaptor.capture(), purposeCaptor.capture());

        assertEquals(PaaLevel.INDIVIDUAL_LEVEL, levelCaptor.getValue());
        assertEquals(YEAR, yearCaptor.getValue());
        assertEquals(Purpose.SIADAP_INTERIM, purposeCaptor.getValue());
    }
}
