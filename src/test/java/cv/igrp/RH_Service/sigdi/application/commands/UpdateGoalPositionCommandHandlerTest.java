package cv.igrp.RH_Service.sigdi.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.StrategicGoalsPerspective;
import cv.igrp.RH_Service.sigdi.application.dto.GoalPositionRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.GoalPositionResponseDTO;
import cv.igrp.RH_Service.sigdi.application.service.StrategicGoalWindowPolicy;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.StrategicGoal;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.StrategicGoalRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.InstitutionalIdentityId;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;

// T-136-20 / A-132-113 (Fase 136-06): mover um objetivo no mapa passa a exigir a janela
// PAA_BSC_OBJECTIVES aberta para o ano do próprio objetivo -- o mesmo critério que criar, alterar
// e cancelar já exigiam, agora consultado através do dono único StrategicGoalWindowPolicy.
// Verify prova, sobre goalRepository.save(), que a janela fechada nunca deixa gravar (D-56).
@ExtendWith(MockitoExtension.class)
class UpdateGoalPositionCommandHandlerTest {

    private static final Integer YEAR = 2026;

    @Mock
    private StrategicGoalRepository goalRepository;

    @Mock
    private StrategicGoalWindowPolicy windowPolicy;

    @InjectMocks
    private UpdateGoalPositionCommandHandler handler;

    private StrategicGoal goal(Integer year) {
        return StrategicGoal.create(
                UUID.randomUUID(), InstitutionalIdentityId.gerarNovo(), "Objetivo de teste",
                StrategicGoalsPerspective.CUSTOMER, BigDecimal.ONE, "Descrição de teste",
                year, new ArrayList<>());
    }

    private UpdateGoalPositionCommand commandFor(StrategicGoalId goalId) {
        GoalPositionRequestDTO dto = new GoalPositionRequestDTO();
        dto.setX(120.5);
        dto.setY(80.0);
        return new UpdateGoalPositionCommand(dto, goalId.getValor().getValor().toString());
    }

    // T-136-20: janela fechada recusa e nunca chega a save().
    @Test
    void handleRejectsAndNeverSavesWhenWindowIsClosed() {
        StrategicGoal goal = goal(YEAR);

        when(goalRepository.findById(goal.getId())).thenReturn(Optional.of(goal));
        doThrow(IgrpResponseStatusException.badRequest(
                "Prazo não configurado para a submissão de objetivos estratégicos PAA/BSC"))
                .when(windowPolicy).requireOpenFor(YEAR);

        UpdateGoalPositionCommand command = commandFor(goal.getId());

        assertThrows(IgrpResponseStatusException.class, () -> handler.handle(command));
        verify(goalRepository, never()).save(any(StrategicGoal.class));
    }

    // Prova que o ano consultado é o do próprio objetivo carregado, nunca outro.
    @Test
    void handleQueriesWindowForTheLoadedGoalsYear() {
        StrategicGoal goal = goal(YEAR);

        when(goalRepository.findById(goal.getId())).thenReturn(Optional.of(goal));
        when(goalRepository.save(any(StrategicGoal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UpdateGoalPositionCommand command = commandFor(goal.getId());

        handler.handle(command);

        verify(windowPolicy).requireOpenFor(YEAR);
    }

    // Janela aberta grava normalmente -- o portão não quebra o caminho feliz.
    @Test
    void handleSavesWhenWindowIsOpen() {
        StrategicGoal goal = goal(YEAR);

        when(goalRepository.findById(goal.getId())).thenReturn(Optional.of(goal));
        when(goalRepository.save(any(StrategicGoal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UpdateGoalPositionCommand command = commandFor(goal.getId());

        ResponseEntity<GoalPositionResponseDTO> response = handler.handle(command);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        verify(goalRepository).save(any(StrategicGoal.class));
    }

    // A exceção de notFound mantém-se como estava (ResponseStatusException do Spring) -- não é
    // o achado deste plano, e o portão nunca é sequer consultado para um objetivo inexistente.
    @Test
    void handleWithUnknownGoalThrowsNotFoundAndNeverConsultsWindow() {
        StrategicGoalId unknownId = StrategicGoalId.gerarNovo();
        when(goalRepository.findById(unknownId)).thenReturn(Optional.empty());

        UpdateGoalPositionCommand command = commandFor(unknownId);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> handler.handle(command));

        assertEquals(404, ex.getStatusCode().value());
        verify(windowPolicy, never()).requireOpenFor(any());
        verify(goalRepository, never()).save(any(StrategicGoal.class));
    }
}
