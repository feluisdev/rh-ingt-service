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
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.dto.KeyResultRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.KeyResultResponseDTO;
import cv.igrp.RH_Service.sigdi.application.service.PaaActivityWindowPolicy;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.TacticalActivity;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.KeyResultRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.TacticalActivityRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.Budget;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.DateRange;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.TacticalActivityId;

// A-132-112 (Fase 136-06): CreateKeyResultCommandHandler já carregava a atividade -- o portão
// entra logo a seguir a essa carga. Verify prova, sobre save(), que a janela fechada nunca
// deixa gravar (D-56).
@ExtendWith(MockitoExtension.class)
class CreateKeyResultCommandHandlerTest {

    @Mock
    private KeyResultRepository keyResultRepository;

    @Mock
    private TacticalActivityRepository activityRepository;

    @Mock
    private PaaActivityWindowPolicy windowPolicy;

    @InjectMocks
    private CreateKeyResultCommandHandler handler;

    private TacticalActivity activity(PaaLevel paaLevel) {
        return TacticalActivity.create(
                UUID.randomUUID(),
                StrategicGoalId.from(UUID.randomUUID()),
                UUID.randomUUID(),
                "Atividade de teste",
                null,
                null,
                null,
                UUID.randomUUID(), // responsibleWho -- obrigatório quando paaLevel é INDIVIDUAL_LEVEL
                null,
                DateRange.of(LocalDate.now(), LocalDate.now().plusDays(10)),
                Budget.of(new BigDecimal("1000"), "02.02.01"),
                paaLevel);
    }

    private KeyResultRequestDTO requestFor(UUID activityId) {
        KeyResultRequestDTO dto = new KeyResultRequestDTO();
        dto.setTitle("Resultado-chave de teste");
        dto.setTargetValue(new BigDecimal("100"));
        dto.setMetricUnit("NUMBER");
        dto.setActivityId(activityId);
        return dto;
    }

    // T-136-18: janela fechada recusa e nunca chega a save().
    @Test
    void handleRejectsAndNeverSavesWhenWindowIsClosed() {
        TacticalActivity activity = activity(PaaLevel.UNIT_LEVEL);
        UUID activityUuid = activity.getId().getValor().getValor();

        when(activityRepository.findById(TacticalActivityId.from(activityUuid)))
                .thenReturn(Optional.of(activity));
        doThrow(IgrpResponseStatusException.badRequest("Prazo não configurado para a submissão de atividades do PAA"))
                .when(windowPolicy).requireOpenFor(any(PaaLevel.class));

        CreateKeyResultCommand command = new CreateKeyResultCommand(requestFor(activityUuid));

        assertThrows(IgrpResponseStatusException.class, () -> handler.handle(command));
        verify(keyResultRepository, never()).save(any());
    }

    // Prova que o paaLevel consultado vem da atividade carregada a partir do pedido.
    @Test
    void handleQueriesWindowForTheLoadedActivitysPaaLevel() {
        TacticalActivity activity = activity(PaaLevel.INDIVIDUAL_LEVEL);
        UUID activityUuid = activity.getId().getValor().getValor();

        when(activityRepository.findById(TacticalActivityId.from(activityUuid)))
                .thenReturn(Optional.of(activity));
        when(keyResultRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateKeyResultCommand command = new CreateKeyResultCommand(requestFor(activityUuid));

        handler.handle(command);

        verify(windowPolicy).requireOpenFor(PaaLevel.INDIVIDUAL_LEVEL);
    }

    // Janela aberta grava normalmente -- o portão não quebra o caminho feliz.
    @Test
    void handleSavesWhenWindowIsOpen() {
        TacticalActivity activity = activity(PaaLevel.UNIT_LEVEL);
        UUID activityUuid = activity.getId().getValor().getValor();

        when(activityRepository.findById(TacticalActivityId.from(activityUuid)))
                .thenReturn(Optional.of(activity));
        when(keyResultRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateKeyResultCommand command = new CreateKeyResultCommand(requestFor(activityUuid));

        ResponseEntity<KeyResultResponseDTO> response = handler.handle(command);

        assertNotNull(response);
        assertEquals(201, response.getStatusCode().value());
        verify(keyResultRepository).save(any());
    }
}
