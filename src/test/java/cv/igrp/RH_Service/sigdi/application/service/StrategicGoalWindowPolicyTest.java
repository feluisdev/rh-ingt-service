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
 * Fase 136-06 / T-136-21: cobre o dono único do critério que
 * {@code CreateStrategicGoalCommandHandler}, {@code UpdateStrategicGoalsCommandHandler},
 * {@code CancelStrategicGoalCommandHandler} e {@code UpdateGoalPositionCommandHandler} passam a
 * consultar em vez de repetir em linha. A mensagem de recusa e a assinatura da consulta (teste 4)
 * são o contrato que os quatro chamadores dependem.
 */
@ExtendWith(MockitoExtension.class)
class StrategicGoalWindowPolicyTest {

    private static final Integer YEAR = 2026;

    @Mock
    private PaaSubmissionPeriodRepository periodRepository;

    @InjectMocks
    private StrategicGoalWindowPolicy policy;

    @Test
    void requireOpenForDoesNotThrowWhenWindowIsActive() {
        PaaSubmissionPeriod activePeriod = mock(PaaSubmissionPeriod.class);
        when(periodRepository.findActiveByTypeAndYearAndPurpose(
                PaaLevel.UNIT_LEVEL, YEAR, Purpose.PAA_BSC_OBJECTIVES))
                .thenReturn(Optional.of(activePeriod));

        assertDoesNotThrow(() -> policy.requireOpenFor(YEAR));
    }

    @Test
    void requireOpenForThrowsBadRequestWithTheExistingMessageWhenWindowIsNotActive() {
        when(periodRepository.findActiveByTypeAndYearAndPurpose(
                PaaLevel.UNIT_LEVEL, YEAR, Purpose.PAA_BSC_OBJECTIVES))
                .thenReturn(Optional.empty());

        IgrpResponseStatusException exception =
                assertThrows(IgrpResponseStatusException.class, () -> policy.requireOpenFor(YEAR));

        assertEquals(400, exception.getBody().getStatus());
        // A mensagem é, literalmente, a que os três handlers já usavam -- GoalDrawer.tsx e o
        // PeriodGateAlert foram construídos contra este texto exato.
        assertEquals("Prazo não configurado para a submissão de objetivos estratégicos PAA/BSC",
                exception.getBody().getTitle());
    }

    /**
     * Prova a assinatura exata da consulta: {@code (UNIT_LEVEL, year, PAA_BSC_OBJECTIVES)} -- não
     * qualquer permutação destes três valores. É a contagem que cai a zero nos quatro handlers.
     */
    @Test
    void queriesWithExactlyUnitLevelYearAndBscObjectivesPurpose() {
        when(periodRepository.findActiveByTypeAndYearAndPurpose(
                PaaLevel.UNIT_LEVEL, YEAR, Purpose.PAA_BSC_OBJECTIVES))
                .thenReturn(Optional.empty());

        assertThrows(IgrpResponseStatusException.class, () -> policy.requireOpenFor(YEAR));

        ArgumentCaptor<PaaLevel> levelCaptor = ArgumentCaptor.forClass(PaaLevel.class);
        ArgumentCaptor<Integer> yearCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Purpose> purposeCaptor = ArgumentCaptor.forClass(Purpose.class);
        org.mockito.Mockito.verify(periodRepository)
                .findActiveByTypeAndYearAndPurpose(levelCaptor.capture(), yearCaptor.capture(), purposeCaptor.capture());

        assertEquals(PaaLevel.UNIT_LEVEL, levelCaptor.getValue());
        assertEquals(YEAR, yearCaptor.getValue());
        assertEquals(Purpose.PAA_BSC_OBJECTIVES, purposeCaptor.getValue());
    }
}
