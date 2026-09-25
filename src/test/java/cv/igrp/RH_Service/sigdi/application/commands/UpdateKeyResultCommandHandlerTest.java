package cv.igrp.RH_Service.sigdi.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.KeyResultMetricUnit;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.dto.KeyResultRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.KeyResultResponseDTO;
import cv.igrp.RH_Service.sigdi.application.service.PaaActivityWindowPolicy;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.KeyResult;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.TacticalActivity;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.KeyResultRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.TacticalActivityRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.Budget;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.DateRange;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.KeyResultId;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.TacticalActivityId;

// A-132-112 (Fase 136-06): UpdateKeyResultCommandHandler passa a carregar a atividade a partir
// de keyResult.getActivityId() -- nunca do pedido -- e a consultar a janela antes de save().
// Verify prova, sobre save(), que a janela fechada nunca deixa gravar (D-56).
@ExtendWith(MockitoExtension.class)
class UpdateKeyResultCommandHandlerTest {

    @Mock
    private KeyResultRepository keyResultRepository;

    @Mock
    private TacticalActivityRepository activityRepository;

    @Mock
    private PaaActivityWindowPolicy windowPolicy;

    @InjectMocks
    private UpdateKeyResultCommandHandler handler;

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

    private KeyResult existingKeyResult(TacticalActivityId activityId) {
        return KeyResult.reconstruct(KeyResultId.gerarNovo(), UUID.randomUUID(), activityId,
                "Resultado-chave original", new BigDecimal("100"), BigDecimal.ZERO,
                KeyResultMetricUnit.NUMBER, new ArrayList<>(),
                null, null, null, null, null, null);
    }

    private KeyResultRequestDTO requestWithoutCurrentValueOrActivityId() {
        KeyResultRequestDTO dto = new KeyResultRequestDTO();
        dto.setTitle("Resultado-chave atualizado");
        dto.setTargetValue(new BigDecimal("150"));
        dto.setMetricUnit("NUMBER");
        return dto;
    }

    // T-136-18: janela fechada recusa e nunca chega a save().
    @Test
    void handleRejectsAndNeverSavesWhenWindowIsClosed() {
        TacticalActivity activity = activity(PaaLevel.UNIT_LEVEL);
        TacticalActivityId activityId = activity.getId();
        KeyResult keyResult = existingKeyResult(activityId);

        when(keyResultRepository.findByIdFull(keyResult.getId())).thenReturn(Optional.of(keyResult));
        when(activityRepository.findById(activityId)).thenReturn(Optional.of(activity));
        doThrow(IgrpResponseStatusException.badRequest("Prazo não configurado para a submissão de atividades do PAA"))
                .when(windowPolicy).requireOpenFor(any(PaaLevel.class));

        UpdateKeyResultCommand command = new UpdateKeyResultCommand(
                requestWithoutCurrentValueOrActivityId(), keyResult.getId().getStringValor());

        assertThrows(IgrpResponseStatusException.class, () -> handler.handle(command));
        verify(keyResultRepository, never()).save(any());
    }

    // Prova que o paaLevel consultado vem da atividade carregada a partir de keyResult.getActivityId().
    @Test
    void handleQueriesWindowForTheActivityOwningTheKeyResult() {
        TacticalActivity activity = activity(PaaLevel.INDIVIDUAL_LEVEL);
        TacticalActivityId activityId = activity.getId();
        KeyResult keyResult = existingKeyResult(activityId);

        when(keyResultRepository.findByIdFull(keyResult.getId())).thenReturn(Optional.of(keyResult));
        when(activityRepository.findById(activityId)).thenReturn(Optional.of(activity));
        when(keyResultRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateKeyResultCommand command = new UpdateKeyResultCommand(
                requestWithoutCurrentValueOrActivityId(), keyResult.getId().getStringValor());

        handler.handle(command);

        verify(windowPolicy).requireOpenFor(PaaLevel.INDIVIDUAL_LEVEL);
    }

    // Janela aberta grava normalmente -- o portão não quebra o caminho feliz.
    @Test
    void handleSavesWhenWindowIsOpen() {
        TacticalActivity activity = activity(PaaLevel.UNIT_LEVEL);
        TacticalActivityId activityId = activity.getId();
        KeyResult keyResult = existingKeyResult(activityId);

        when(keyResultRepository.findByIdFull(keyResult.getId())).thenReturn(Optional.of(keyResult));
        when(activityRepository.findById(activityId)).thenReturn(Optional.of(activity));
        when(keyResultRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateKeyResultCommand command = new UpdateKeyResultCommand(
                requestWithoutCurrentValueOrActivityId(), keyResult.getId().getStringValor());

        ResponseEntity<KeyResultResponseDTO> response = handler.handle(command);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        verify(keyResultRepository).save(any());
    }

    // Um resultado-chave órfão (sem activityId) é recusado com mensagem legível, e nunca chega
    // a consultar a janela nem a gravar -- um KeyResult órfão a escapar ao portão é o defeito
    // com outro nome.
    @Test
    void handleRejectsOrphanKeyResultWithoutActivityIdAndNeverQueriesWindowOrSaves() {
        KeyResult orphanKeyResult = existingKeyResult(null);

        when(keyResultRepository.findByIdFull(orphanKeyResult.getId()))
                .thenReturn(Optional.of(orphanKeyResult));

        UpdateKeyResultCommand command = new UpdateKeyResultCommand(
                requestWithoutCurrentValueOrActivityId(), orphanKeyResult.getId().getStringValor());

        IgrpResponseStatusException ex = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(command));

        assertEquals(400, ex.getBody().getStatus());
        verifyNoInteractions(windowPolicy);
        verify(keyResultRepository, never()).save(any());
    }
}
