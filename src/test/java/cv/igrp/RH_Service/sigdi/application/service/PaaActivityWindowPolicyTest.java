package cv.igrp.RH_Service.sigdi.application.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.PaaSubmissionPeriod;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PaaSubmissionPeriodRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.Year;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Covers the single owner of "is the {@code Purpose.PAA} submission window of the current year
 * open for a given {@code PaaLevel}?" -- the criterion this class exists to keep identical to
 * {@code CreateTacticalActivityCommandHandler} and {@code UpdateTacticalActivityCommandHandler}
 * (D-27). Test 3 below is the one that would catch a diverging criterion; the other three would
 * still pass with a wrong purpose or a wrong year.
 */
@ExtendWith(MockitoExtension.class)
class PaaActivityWindowPolicyTest {

    private static final PaaLevel LEVEL = PaaLevel.UNIT_LEVEL;

    @Mock
    private PaaSubmissionPeriodRepository periodRepository;

    @InjectMocks
    private PaaActivityWindowPolicy policy;

    @Test
    void requireOpenForDoesNotThrowWhenWindowIsActive() {
        PaaSubmissionPeriod activePeriod = mock(PaaSubmissionPeriod.class);
        when(periodRepository.findActiveByTypeAndYearAndPurpose(LEVEL, Year.now().getValue(), Purpose.PAA))
                .thenReturn(Optional.of(activePeriod));

        assertDoesNotThrow(() -> policy.requireOpenFor(LEVEL));
    }

    @Test
    void requireOpenForThrowsBadRequestWithTheProductMessageWhenWindowIsNotActive() {
        when(periodRepository.findActiveByTypeAndYearAndPurpose(LEVEL, Year.now().getValue(), Purpose.PAA))
                .thenReturn(Optional.empty());

        IgrpResponseStatusException exception =
                assertThrows(IgrpResponseStatusException.class, () -> policy.requireOpenFor(LEVEL));

        assertEquals(400, exception.getBody().getStatus());
        assertTrue(exception.getBody().getTitle().contains(
                "Prazo não configurado para a submissão de atividades do PAA"));
    }

    /**
     * The triplet assertion that D-27 exists to guarantee. Proves the query is made with exactly
     * {@code (paaLevel, Year.now().getValue(), Purpose.PAA)} -- the pair and year criterion from
     * the mould handlers, not any other permutation.
     */
    @Test
    void queriesWithExactlyTheCallerLevelCurrentYearAndPaaPurpose() {
        when(periodRepository.findActiveByTypeAndYearAndPurpose(LEVEL, Year.now().getValue(), Purpose.PAA))
                .thenReturn(Optional.empty());

        assertThrows(IgrpResponseStatusException.class, () -> policy.requireOpenFor(LEVEL));

        ArgumentCaptor<PaaLevel> levelCaptor = ArgumentCaptor.forClass(PaaLevel.class);
        ArgumentCaptor<Integer> yearCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Purpose> purposeCaptor = ArgumentCaptor.forClass(Purpose.class);
        Mockito.verify(periodRepository)
                .findActiveByTypeAndYearAndPurpose(levelCaptor.capture(), yearCaptor.capture(), purposeCaptor.capture());

        assertEquals(LEVEL, levelCaptor.getValue());
        assertEquals(Year.now().getValue(), yearCaptor.getValue());
        assertEquals(Purpose.PAA, purposeCaptor.getValue());
    }

    /**
     * Fail-closed with a null level: refuses with the same message, never a NullPointerException.
     * Proves the fail-closed behaviour is the one the spec describes and not an accident of the
     * mock returning empty for any argument.
     */
    @Test
    void requireOpenForRefusesWithTheSameMessageWhenLevelIsNull() {
        when(periodRepository.findActiveByTypeAndYearAndPurpose(null, Year.now().getValue(), Purpose.PAA))
                .thenReturn(Optional.empty());

        IgrpResponseStatusException exception =
                assertThrows(IgrpResponseStatusException.class, () -> policy.requireOpenFor(null));

        assertEquals(400, exception.getBody().getStatus());
        assertTrue(exception.getBody().getTitle().contains(
                "Prazo não configurado para a submissão de atividades do PAA"));
    }
}
