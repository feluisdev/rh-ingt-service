package cv.igrp.RH_Service.sigdi.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
import cv.igrp.RH_Service.sigdi.application.dto.KeyResultCheckinRequestDTO;
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

@ExtendWith(MockitoExtension.class)
class RegistraProcessoKrCommandHandlerTest {

    @Mock
    private KeyResultRepository keyResultRepository;

    @Mock
    private TacticalActivityRepository activityRepository;

    @Mock
    private PaaActivityWindowPolicy windowPolicy;

    @InjectMocks
    private RegistraProcessoKrCommandHandler handler;

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

    // OKR-01: proves the handler accepts a check-in without evidence -- the case that
    // was impossible while evidenceUrl carried @NotBlank in the DTO. Also proves the check-in
    // reaches save() only once the window is open (T-136-19, Fase 136-06).
    @Test
    void handle_appliesCheckinWithNullEvidenceAndSaves() {
        String keyResultIdString = UUID.randomUUID().toString();
        BigDecimal valueAdded = new BigDecimal("15");
        String comment = "Progresso registado sem evidência";

        KeyResultCheckinRequestDTO dto = new KeyResultCheckinRequestDTO();
        dto.setValueAdded(valueAdded);
        dto.setComment(comment);
        dto.setEvidenceUrl(null);

        RegistraProcessoKrCommand command = new RegistraProcessoKrCommand(dto, keyResultIdString);

        TacticalActivity activity = activity(PaaLevel.UNIT_LEVEL);
        TacticalActivityId activityId = activity.getId();

        KeyResult existingKeyResult = mock(KeyResult.class);
        when(existingKeyResult.getActivityId()).thenReturn(activityId);

        when(keyResultRepository.findByIdFull(KeyResultId.from(keyResultIdString)))
                .thenReturn(Optional.of(existingKeyResult));
        when(activityRepository.findById(activityId)).thenReturn(Optional.of(activity));
        when(existingKeyResult.applyCheckin(valueAdded, null, comment))
                .thenReturn(existingKeyResult);
        when(keyResultRepository.save(existingKeyResult)).thenReturn(existingKeyResult);

        ResponseEntity<String> response = handler.handle(command);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        verify(windowPolicy).requireOpenFor(PaaLevel.UNIT_LEVEL);
        verify(existingKeyResult).applyCheckin(valueAdded, null, comment);
        verify(keyResultRepository).save(existingKeyResult);
    }

    // T-136-19: janela fechada recusa o check-in, e o currentValue do resultado-chave não
    // avança -- applyCheckin nunca é chamado e save() também não (D-56: um 200 nunca prova uma
    // escrita, e aqui não há sequer o 200).
    @Test
    void handle_rejectsAndNeverAppliesCheckinWhenWindowIsClosed() {
        String keyResultIdString = UUID.randomUUID().toString();

        KeyResultCheckinRequestDTO dto = new KeyResultCheckinRequestDTO();
        dto.setValueAdded(new BigDecimal("15"));
        dto.setComment("Progresso registado fora de janela");

        RegistraProcessoKrCommand command = new RegistraProcessoKrCommand(dto, keyResultIdString);

        TacticalActivity activity = activity(PaaLevel.UNIT_LEVEL);
        TacticalActivityId activityId = activity.getId();

        KeyResult existingKeyResult = mock(KeyResult.class);
        when(existingKeyResult.getActivityId()).thenReturn(activityId);

        when(keyResultRepository.findByIdFull(KeyResultId.from(keyResultIdString)))
                .thenReturn(Optional.of(existingKeyResult));
        when(activityRepository.findById(activityId)).thenReturn(Optional.of(activity));
        doThrow(IgrpResponseStatusException.badRequest("Prazo não configurado para a submissão de atividades do PAA"))
                .when(windowPolicy).requireOpenFor(any(PaaLevel.class));

        assertThrows(IgrpResponseStatusException.class, () -> handler.handle(command));

        verify(existingKeyResult, never()).applyCheckin(any(), any(), any());
        verify(keyResultRepository, never()).save(any());
    }

    // Um resultado-chave órfão (sem activityId) é recusado com mensagem legível, e nunca chega
    // a consultar a janela nem a aplicar o check-in.
    @Test
    void handle_rejectsOrphanKeyResultWithoutActivityIdAndNeverQueriesWindowOrSaves() {
        String keyResultIdString = UUID.randomUUID().toString();

        KeyResultCheckinRequestDTO dto = new KeyResultCheckinRequestDTO();
        dto.setValueAdded(new BigDecimal("15"));
        dto.setComment("Progresso registado sem atividade associada");

        RegistraProcessoKrCommand command = new RegistraProcessoKrCommand(dto, keyResultIdString);

        KeyResult orphanKeyResult = mock(KeyResult.class);
        when(orphanKeyResult.getActivityId()).thenReturn(null);

        when(keyResultRepository.findByIdFull(KeyResultId.from(keyResultIdString)))
                .thenReturn(Optional.of(orphanKeyResult));

        IgrpResponseStatusException ex = assertThrows(IgrpResponseStatusException.class,
                () -> handler.handle(command));

        assertEquals(400, ex.getBody().getStatus());
        verifyNoInteractions(windowPolicy);
        verify(orphanKeyResult, never()).applyCheckin(any(), any(), any());
        verify(keyResultRepository, never()).save(any());
    }
}
