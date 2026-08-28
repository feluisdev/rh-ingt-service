package cv.igrp.RH_Service.sigdi.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.FormGenerationBatchStatus;
import cv.igrp.RH_Service.sigdi.application.constants.FormGenerationOutcome;
import cv.igrp.RH_Service.sigdi.application.constants.FormGenerationRevertSkipReason;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.application.dto.FormGenerationRevertResultDTO;
import cv.igrp.RH_Service.sigdi.application.service.FormGenerationBatchRevertService;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.FormGenerationBatch;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.FormGenerationBatchItem;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.FormGenerationBatchRepository;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Unit tests for {@link RevertFormGenerationBatchCommandHandler} (Fase 120, plano 03,
 * {@code PRZ-04}) -- os oito comportamentos do {@code <behavior>} da tarefa 2: as quatro
 * recusas (lote inexistente, lote de outro período, simulação, modo {@code READ_ONLY}, lote já
 * desfeito -- as duas primeiras partilham a mesma resposta {@code 404}, contando como uma única
 * recusa), o caminho válido com a ordem {@code revert} {@literal ->} {@code markReverted}, a
 * forma do resultado, e o lote sem itens {@code CREATED} que se desfaz sem ser recusado.
 *
 * <p>{@link FormGenerationBatchRevertService} é mock: a fronteira do apagável já está provada
 * por teste próprio em {@code FormGenerationBatchRevertServiceTest} (Fase 120, plano 02). O que
 * este teste prova é que o handler chama o serviço com o lote certo, marca a reversão com o
 * resultado do serviço, e nunca chama nenhum dos dois antes de passar as quatro guardas.
 */
@ExtendWith(MockitoExtension.class)
class RevertFormGenerationBatchCommandHandlerTest {

    private static final UUID PERIOD_ID = UUID.randomUUID();
    private static final UUID BATCH_ID = UUID.randomUUID();
    private static final LocalDateTime GENERATED_AT = LocalDateTime.of(2026, 8, 20, 10, 0);
    private static final LocalDateTime FINISHED_AT = LocalDateTime.of(2026, 8, 20, 10, 5);

    @Mock
    private FormGenerationBatchRepository batchRepository;

    @Mock
    private FormGenerationBatchRevertService revertService;

    @InjectMocks
    private RevertFormGenerationBatchCommandHandler handler;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void nonExistentBatchYields404WithoutTouchingRevertService() {
        when(batchRepository.findById(BATCH_ID)).thenReturn(Optional.empty());

        var command = new RevertFormGenerationBatchCommand(PERIOD_ID, BATCH_ID);
        var ex = assertThrows(IgrpResponseStatusException.class, () -> handler.handle(command));

        assertEquals(404, ex.getStatusCode().value());
        assertEquals("Lote de geração não encontrado", ex.getBody().getTitle());
        verify(revertService, never()).revert(any());
        verify(batchRepository, never()).markReverted(any(), any(), any(), any(), anyString());
    }

    @Test
    void batchFromAnotherPeriodYields404WithTheSameMessageAsNotFound() {
        UUID otherPeriodId = UUID.randomUUID();
        FormGenerationBatch batch = fixture(BATCH_ID, otherPeriodId, FormGenerationBatch.CREATES_FORMS,
                false, null, List.of());
        when(batchRepository.findById(BATCH_ID)).thenReturn(Optional.of(batch));

        var command = new RevertFormGenerationBatchCommand(PERIOD_ID, BATCH_ID);
        var ex = assertThrows(IgrpResponseStatusException.class, () -> handler.handle(command));

        assertEquals(404, ex.getStatusCode().value());
        assertEquals("Lote de geração não encontrado", ex.getBody().getTitle());
        verify(revertService, never()).revert(any());
        verify(batchRepository, never()).markReverted(any(), any(), any(), any(), anyString());
    }

    @Test
    void dryRunBatchYields409WithoutTouchingRevertService() {
        FormGenerationBatch batch = fixture(BATCH_ID, PERIOD_ID, FormGenerationBatch.CREATES_FORMS,
                true, null, List.of());
        when(batchRepository.findById(BATCH_ID)).thenReturn(Optional.of(batch));

        var command = new RevertFormGenerationBatchCommand(PERIOD_ID, BATCH_ID);
        var ex = assertThrows(IgrpResponseStatusException.class, () -> handler.handle(command));

        assertEquals(409, ex.getStatusCode().value());
        assertEquals("Esta geração é uma simulação: não criou formulários, não há nada a desfazer.",
                ex.getBody().getTitle());
        verify(revertService, never()).revert(any());
        verify(batchRepository, never()).markReverted(any(), any(), any(), any(), anyString());
    }

    @Test
    void readOnlyBatchYields409WithoutTouchingRevertService() {
        FormGenerationBatch batch = fixture(BATCH_ID, PERIOD_ID, FormGenerationBatch.READ_ONLY,
                false, null, List.of());
        when(batchRepository.findById(BATCH_ID)).thenReturn(Optional.of(batch));

        var command = new RevertFormGenerationBatchCommand(PERIOD_ID, BATCH_ID);
        var ex = assertThrows(IgrpResponseStatusException.class, () -> handler.handle(command));

        assertEquals(409, ex.getStatusCode().value());
        assertEquals("Esta finalidade não cria formulários; não há nada a desfazer.",
                ex.getBody().getTitle());
        verify(revertService, never()).revert(any());
        verify(batchRepository, never()).markReverted(any(), any(), any(), any(), anyString());
    }

    @Test
    void alreadyRevertedBatchYields409WithTheDateWithoutTouchingRevertService() {
        LocalDateTime revertedAt = LocalDateTime.of(2026, 8, 21, 9, 30);
        FormGenerationBatch batch = fixture(BATCH_ID, PERIOD_ID, FormGenerationBatch.CREATES_FORMS,
                false, revertedAt, List.of());
        when(batchRepository.findById(BATCH_ID)).thenReturn(Optional.of(batch));

        var command = new RevertFormGenerationBatchCommand(PERIOD_ID, BATCH_ID);
        var ex = assertThrows(IgrpResponseStatusException.class, () -> handler.handle(command));

        assertEquals(409, ex.getStatusCode().value());
        assertEquals("Esta geração já foi desfeita em " + revertedAt + ".", ex.getBody().getTitle());
        verify(revertService, never()).revert(any());
        verify(batchRepository, never()).markReverted(any(), any(), any(), any(), anyString());
    }

    @Test
    void validBatchCallsRevertThenMarkRevertedInOrderAndReturns200() {
        FormGenerationBatch batch = fixture(BATCH_ID, PERIOD_ID, FormGenerationBatch.CREATES_FORMS,
                false, null, List.of());
        when(batchRepository.findById(BATCH_ID)).thenReturn(Optional.of(batch));

        UUID revertedFormId = UUID.randomUUID();
        var outcome = new FormGenerationBatchRevertService.RevertOutcome(List.of(revertedFormId), Map.of());
        when(revertService.revert(batch)).thenReturn(outcome);

        LocalDateTime revertedAt = LocalDateTime.of(2026, 8, 28, 11, 0);
        FormGenerationBatch marked = fixture(BATCH_ID, PERIOD_ID, FormGenerationBatch.CREATES_FORMS,
                false, revertedAt, List.of());
        when(batchRepository.markReverted(eq(BATCH_ID), eq(List.of(revertedFormId)), eq(Map.of()),
                any(LocalDateTime.class), anyString())).thenReturn(marked);

        authenticateWith("quem-desfez@nosi.cv");

        ResponseEntity<FormGenerationRevertResultDTO> response =
                handler.handle(new RevertFormGenerationBatchCommand(PERIOD_ID, BATCH_ID));

        assertEquals(200, response.getStatusCode().value());
        assertEquals(BATCH_ID.toString(), response.getBody().getBatchId());
        assertEquals(PERIOD_ID.toString(), response.getBody().getPeriodId());

        InOrder order = inOrder(revertService, batchRepository);
        order.verify(revertService).revert(batch);
        order.verify(batchRepository).markReverted(eq(BATCH_ID), any(), any(), any(), any());

        ArgumentCaptor<String> authorCaptor = ArgumentCaptor.forClass(String.class);
        verify(batchRepository).markReverted(eq(BATCH_ID), any(), any(), any(), authorCaptor.capture());
        assertEquals("quem-desfez@nosi.cv", authorCaptor.getValue());
    }

    @Test
    void resultDtoCarriesCountsStatusAndBlockedItemsWithReason() {
        UUID revertedFormId = UUID.randomUUID();
        UUID blockedFormId = UUID.randomUUID();
        UUID blockedEmployeeId = UUID.randomUUID();
        UUID blockedUnitId = UUID.randomUUID();

        FormGenerationBatchItem blockedItem = FormGenerationBatchItem.of(blockedEmployeeId,
                "Colaborador Bloqueado", blockedUnitId, "Unidade X", FormGenerationOutcome.CREATED,
                blockedFormId, UUID.randomUUID(), null, null, GENERATED_AT);

        FormGenerationBatch batch = fixture(BATCH_ID, PERIOD_ID, FormGenerationBatch.CREATES_FORMS,
                false, null, List.of(blockedItem));
        when(batchRepository.findById(BATCH_ID)).thenReturn(Optional.of(batch));

        Map<UUID, FormGenerationRevertSkipReason> blocked = new LinkedHashMap<>();
        blocked.put(blockedFormId, FormGenerationRevertSkipReason.PHASE_ADVANCED);
        var outcome = new FormGenerationBatchRevertService.RevertOutcome(List.of(revertedFormId), blocked);
        when(revertService.revert(batch)).thenReturn(outcome);

        LocalDateTime revertedAt = LocalDateTime.of(2026, 8, 28, 12, 0);
        FormGenerationBatch marked = FormGenerationBatch.reconstruct(BATCH_ID, PERIOD_ID, Purpose.SIADAP,
                PaaLevel.INDIVIDUAL_LEVEL, 2026, FormGenerationBatch.CREATES_FORMS, false,
                FormGenerationBatchStatus.PARTIALLY_REVERTED, 1, 0, 0, 0, GENERATED_AT, FINISHED_AT,
                "gerador@nosi.cv", List.of(blockedItem), revertedAt, "quem-desfez@nosi.cv", 1, 1);
        when(batchRepository.markReverted(eq(BATCH_ID), any(), any(), any(), anyString())).thenReturn(marked);

        authenticateWith("quem-desfez@nosi.cv");

        ResponseEntity<FormGenerationRevertResultDTO> response =
                handler.handle(new RevertFormGenerationBatchCommand(PERIOD_ID, BATCH_ID));

        FormGenerationRevertResultDTO dto = response.getBody();
        assertEquals("PARTIALLY_REVERTED", dto.getStatus());
        assertEquals(1, dto.getRevertedCount());
        assertEquals(1, dto.getBlockedCount());
        assertEquals(revertedAt, dto.getRevertedAt());
        assertEquals("quem-desfez@nosi.cv", dto.getRevertedBy());
        assertEquals(1, dto.getBlocked().size());

        var blockedDto = dto.getBlocked().get(0);
        assertEquals(blockedEmployeeId.toString(), blockedDto.getEmployeeId());
        assertEquals("Colaborador Bloqueado", blockedDto.getEmployeeName());
        assertEquals(blockedUnitId.toString(), blockedDto.getUnitId());
        assertEquals("Unidade X", blockedDto.getUnitName());
        assertEquals(blockedFormId.toString(), blockedDto.getGeneratedFormId());
        assertEquals("PHASE_ADVANCED", blockedDto.getReason());
        assertEquals(FormGenerationRevertSkipReason.PHASE_ADVANCED.getDescription(),
                blockedDto.getReasonDescription());
    }

    @Test
    void batchWithNoCreatedItemsRevertsWithZeroCountsInsteadOfBeingRefused() {
        FormGenerationBatchItem skippedItem = FormGenerationBatchItem.of(UUID.randomUUID(), "Alguém",
                UUID.randomUUID(), "Unidade Y", FormGenerationOutcome.SKIPPED, null, null,
                "ELIGIBILITY_REASON", null, GENERATED_AT);

        FormGenerationBatch batch = fixture(BATCH_ID, PERIOD_ID, FormGenerationBatch.CREATES_FORMS,
                false, null, List.of(skippedItem));
        when(batchRepository.findById(BATCH_ID)).thenReturn(Optional.of(batch));

        var emptyOutcome = new FormGenerationBatchRevertService.RevertOutcome(List.of(), Map.of());
        when(revertService.revert(batch)).thenReturn(emptyOutcome);

        LocalDateTime revertedAt = LocalDateTime.of(2026, 8, 28, 13, 0);
        FormGenerationBatch marked = fixture(BATCH_ID, PERIOD_ID, FormGenerationBatch.CREATES_FORMS,
                false, revertedAt, List.of(skippedItem));
        when(batchRepository.markReverted(eq(BATCH_ID), eq(List.of()), eq(Map.of()),
                any(LocalDateTime.class), anyString())).thenReturn(marked);

        authenticateWith("quem-desfez@nosi.cv");

        ResponseEntity<FormGenerationRevertResultDTO> response =
                handler.handle(new RevertFormGenerationBatchCommand(PERIOD_ID, BATCH_ID));

        assertEquals(200, response.getStatusCode().value());
        assertEquals(0, response.getBody().getRevertedCount());
        assertEquals(0, response.getBody().getBlockedCount());
        assertTrue(response.getBody().getBlocked().isEmpty());
        verify(revertService).revert(batch);
        verify(batchRepository).markReverted(eq(BATCH_ID), any(), any(), any(), anyString());
    }

    private FormGenerationBatch fixture(UUID id, UUID periodId, String generationMode, boolean dryRun,
                                         LocalDateTime revertedAt, List<FormGenerationBatchItem> items) {
        return FormGenerationBatch.reconstruct(id, periodId, Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL, 2026,
                generationMode, dryRun, FormGenerationBatchStatus.COMPLETED, items.size(), 0, 0, 0,
                GENERATED_AT, FINISHED_AT, "gerador@nosi.cv", items,
                revertedAt, revertedAt != null ? "anterior@nosi.cv" : null, 0, 0);
    }

    private void authenticateWith(String name) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(name, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
