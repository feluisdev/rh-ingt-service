package cv.igrp.RH_Service.sigdi.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.shared.application.constants.Estado;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.StrategicGoalsPerspective;
import cv.igrp.RH_Service.sigdi.application.service.StrategicGoalWindowPolicy;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.StrategicGoal;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.StrategicGoalRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.InstitutionalIdentityId;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

/**
 * FIX-09 / A-124-04 (Media). This file did NOT exist before this wave: the handler it covers
 * had no test at all, which is precisely how the missing deadline guard survived three
 * milestones without ever turning a build red (docs/qa/130-PRECONDICOES.md, 4.2).
 *
 * <p>Two of the five cases exist to pin the ORDER of the guards and not the new guard itself:
 * a missing goal and an already inactive goal must never reach the period repository.
 * Inverting that order would answer 400 (deadline) where the service answers 404/422 (state).
 */
@ExtendWith(MockitoExtension.class)
public class CancelStrategicGoalCommandHandlerTest {

    private static final Integer YEAR = 2026;

    @Mock
    private StrategicGoalRepository goalRepository;

    @Mock
    private StrategicGoalWindowPolicy windowPolicy;

    @InjectMocks
    private CancelStrategicGoalCommandHandler cancelStrategicGoalCommandHandler;

    private static StrategicGoal activeGoal(Integer year) {
        return StrategicGoal.create(
                UUID.randomUUID(), InstitutionalIdentityId.gerarNovo(),
                "Objetivo ativo de teste", StrategicGoalsPerspective.CUSTOMER,
                BigDecimal.ONE, "Descrição de teste", year, new ArrayList<>());
    }

    private static StrategicGoal inactiveGoal() {
        return StrategicGoal.reconstruct(
                StrategicGoalId.gerarNovo(), UUID.randomUUID(), InstitutionalIdentityId.gerarNovo(),
                "Objetivo já cancelado", StrategicGoalsPerspective.CUSTOMER, BigDecimal.ONE,
                Estado.I, "Descrição de teste", null, null, YEAR, new ArrayList<>());
    }

    private static CancelStrategicGoalCommand commandFor(String id) {
        return new CancelStrategicGoalCommand(id);
    }

    /**
     * Case 1 -- an unknown id answers 404 and the period repository is NEVER consulted. The
     * deadline is irrelevant for a goal that does not exist.
     */
    @Test
    void handleWithUnknownGoalThrowsNotFoundAndNeverReadsThePeriod() {
        when(goalRepository.findById(any(StrategicGoalId.class))).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> cancelStrategicGoalCommandHandler.handle(
                        commandFor(UUID.randomUUID().toString())));

        assertEquals(404, ex.getStatusCode().value());
        verifyNoInteractions(windowPolicy);
        verify(goalRepository, never()).save(any(StrategicGoal.class));
    }

    /**
     * Case 2 -- an already inactive goal answers 422 and the period repository is NEVER
     * consulted. This is the case that fixes the ORDER of the guards: were the deadline guard
     * moved above the inactivity guard, this case would answer 400 instead of 422.
     */
    @Test
    void handleWithAlreadyInactiveGoalThrowsUnprocessableAndNeverReadsThePeriod() {
        when(goalRepository.findById(any(StrategicGoalId.class)))
                .thenReturn(Optional.of(inactiveGoal()));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> cancelStrategicGoalCommandHandler.handle(
                        commandFor(UUID.randomUUID().toString())));

        assertEquals(422, ex.getStatusCode().value());
        verifyNoInteractions(windowPolicy);
        verify(goalRepository, never()).save(any(StrategicGoal.class));
    }

    /**
     * Case 3 -- an active goal whose year has an OPEN window is cancelled, and what reaches
     * save() is a goal in the inactive state.
     */
    @Test
    void handleWithActiveGoalAndOpenWindowCancelsTheGoal() {
        when(goalRepository.findById(any(StrategicGoalId.class)))
                .thenReturn(Optional.of(activeGoal(YEAR)));
        when(goalRepository.save(any(StrategicGoal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<String> response = cancelStrategicGoalCommandHandler.handle(
                commandFor(UUID.randomUUID().toString()));

        ArgumentCaptor<StrategicGoal> savedCaptor = ArgumentCaptor.forClass(StrategicGoal.class);
        verify(goalRepository).save(savedCaptor.capture());
        assertFalse(savedCaptor.getValue().isActive());
        assertEquals(Estado.I, savedCaptor.getValue().getStatus());
        assertEquals(204, response.getStatusCode().value());
    }

    /**
     * Case 4 -- FIX-09 / A-124-04, the guard itself: an active goal with NO open window for its
     * year is refused with 400 and save() is never called. The exception asserted is
     * IgrpResponseStatusException and not the plain ResponseStatusException, because it is the
     * former that carries the RFC 7807 title all the way to the BFF (BLOQ-07).
     */
    @Test
    void handleWithActiveGoalAndClosedWindowIsRefusedWithBadRequestAndNeverSaves() {
        when(goalRepository.findById(any(StrategicGoalId.class)))
                .thenReturn(Optional.of(activeGoal(YEAR)));
        doThrow(IgrpResponseStatusException.badRequest(
                "Prazo não configurado para a submissão de objetivos estratégicos PAA/BSC"))
                .when(windowPolicy).requireOpenFor(YEAR);

        IgrpResponseStatusException ex = assertThrows(IgrpResponseStatusException.class,
                () -> cancelStrategicGoalCommandHandler.handle(
                        commandFor(UUID.randomUUID().toString())));

        assertEquals(400, ex.getStatusCode().value());
        assertEquals("Prazo não configurado para a submissão de objetivos estratégicos PAA/BSC",
                ex.getBody().getTitle());
        verify(goalRepository, never()).save(any(StrategicGoal.class));
    }

    /**
     * Case 5 -- the measured consequence of mirroring the update handler: a legacy goal with a
     * null year has no effective year, so it can no longer be cancelled. Refused with 400 by
     * the mandatory-year message, and save() is never called. The title travels on an
     * IgrpResponseStatusException for the same reason as case 4.
     */
    @Test
    void handleWithActiveGoalAndNullYearIsRefusedWithBadRequestAndNeverSaves() {
        when(goalRepository.findById(any(StrategicGoalId.class)))
                .thenReturn(Optional.of(activeGoal(null)));

        IgrpResponseStatusException ex = assertThrows(IgrpResponseStatusException.class,
                () -> cancelStrategicGoalCommandHandler.handle(
                        commandFor(UUID.randomUUID().toString())));

        assertEquals(400, ex.getStatusCode().value());
        assertEquals("O ano é obrigatório para a submissão de objetivos estratégicos PAA/BSC.",
                ex.getBody().getTitle());
        verifyNoInteractions(windowPolicy);
        verify(goalRepository, never()).save(any(StrategicGoal.class));
    }
}
