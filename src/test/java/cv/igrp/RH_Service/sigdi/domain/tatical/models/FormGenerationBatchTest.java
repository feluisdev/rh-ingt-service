package cv.igrp.RH_Service.sigdi.domain.tatical.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cv.igrp.RH_Service.sigdi.application.constants.FormGenerationBatchStatus;
import cv.igrp.RH_Service.sigdi.application.constants.FormGenerationOutcome;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Fase 119, plano 01 (PRZ-06/PRZ-07). Cobertura do agregado do lote de geração de
 * formulários e do seu item filho.
 *
 * <p>Assinatura de {@link FormGenerationBatchItem#of}, por esta ordem: employeeId,
 * employeeName, unitId, unitName, outcome, generatedFormId, evaluatorId, skipReason,
 * errorMessage, createdAt.
 */
class FormGenerationBatchTest {

    private static final UUID PERIOD_ID = UUID.randomUUID();
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 27, 10, 0);

    private FormGenerationBatch startDefault() {
        return FormGenerationBatch.start(PERIOD_ID, Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL, 2026,
                FormGenerationBatch.CREATES_FORMS, false, "scheduler:period-opening", NOW);
    }

    // --- start ---

    @Test
    void startCreatesABatchWithZeroedCountersAndNoFinishTime() {
        FormGenerationBatch batch = startDefault();

        assertNotNull(batch.getId());
        assertEquals(0, batch.getCreatedCount());
        assertEquals(0, batch.getFailedCount());
        assertEquals(0, batch.getSkippedCount());
        assertEquals(0, batch.getPendingCount());
        assertNull(batch.getFinishedAt());
        assertTrue(batch.getItems().isEmpty());
    }

    @Test
    void startRejectsNullPeriodId() {
        assertThrows(IllegalArgumentException.class, () ->
                FormGenerationBatch.start(null, Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL, 2026,
                        FormGenerationBatch.CREATES_FORMS, false, "scheduler:period-opening", NOW));
    }

    @Test
    void startRejectsNullPurpose() {
        assertThrows(IllegalArgumentException.class, () ->
                FormGenerationBatch.start(PERIOD_ID, null, PaaLevel.INDIVIDUAL_LEVEL, 2026,
                        FormGenerationBatch.CREATES_FORMS, false, "scheduler:period-opening", NOW));
    }

    @Test
    void startRejectsNullType() {
        assertThrows(IllegalArgumentException.class, () ->
                FormGenerationBatch.start(PERIOD_ID, Purpose.SIADAP, null, 2026,
                        FormGenerationBatch.CREATES_FORMS, false, "scheduler:period-opening", NOW));
    }

    @Test
    void startRejectsNullYear() {
        assertThrows(IllegalArgumentException.class, () ->
                FormGenerationBatch.start(PERIOD_ID, Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL, null,
                        FormGenerationBatch.CREATES_FORMS, false, "scheduler:period-opening", NOW));
    }

    @Test
    void startRejectsNullGeneratedBy() {
        assertThrows(IllegalArgumentException.class, () ->
                FormGenerationBatch.start(PERIOD_ID, Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL, 2026,
                        FormGenerationBatch.CREATES_FORMS, false, null, NOW));
    }

    @Test
    void startRejectsBlankGeneratedBy() {
        assertThrows(IllegalArgumentException.class, () ->
                FormGenerationBatch.start(PERIOD_ID, Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL, 2026,
                        FormGenerationBatch.CREATES_FORMS, false, "   ", NOW));
    }

    @Test
    void startRejectsNullGeneratedAt() {
        assertThrows(IllegalArgumentException.class, () ->
                FormGenerationBatch.start(PERIOD_ID, Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL, 2026,
                        FormGenerationBatch.CREATES_FORMS, false, "scheduler:period-opening", null));
    }

    // --- addItem ---

    @Test
    void addItemIncrementsCreatedCountForCreatedOutcome() {
        FormGenerationBatch batch = startDefault();
        batch.addItem(item(FormGenerationOutcome.CREATED));

        assertEquals(1, batch.getCreatedCount());
        assertEquals(1, batch.getItems().size());
    }

    @Test
    void addItemIncrementsCreatedCountForWouldCreateOutcome() {
        FormGenerationBatch batch = startDefault();
        batch.addItem(item(FormGenerationOutcome.WOULD_CREATE));

        assertEquals(1, batch.getCreatedCount());
    }

    @Test
    void addItemIncrementsFailedCountForFailedOutcome() {
        FormGenerationBatch batch = startDefault();
        batch.addItem(failedItem());

        assertEquals(1, batch.getFailedCount());
    }

    @Test
    void addItemIncrementsSkippedCountForSkippedOutcome() {
        FormGenerationBatch batch = startDefault();
        batch.addItem(item(FormGenerationOutcome.SKIPPED));

        assertEquals(1, batch.getSkippedCount());
    }

    @Test
    void addItemIncrementsPendingCountForPendingOutcome() {
        FormGenerationBatch batch = startDefault();
        batch.addItem(item(FormGenerationOutcome.PENDING));

        assertEquals(1, batch.getPendingCount());
    }

    @Test
    void addItemIncrementsPendingCountForAlreadyExistedOutcome() {
        FormGenerationBatch batch = startDefault();
        batch.addItem(item(FormGenerationOutcome.ALREADY_EXISTED));

        assertEquals(1, batch.getPendingCount());
    }

    // --- finish ---

    @Test
    void finishDerivesPartialStatusWhenSomeCreatedAndSomeFailed() {
        FormGenerationBatch batch = startDefault();
        batch.addItem(item(FormGenerationOutcome.CREATED));
        batch.addItem(failedItem());

        batch.finish(NOW.plusMinutes(5));

        assertEquals(FormGenerationBatchStatus.PARTIAL, batch.getStatus());
        assertEquals(NOW.plusMinutes(5), batch.getFinishedAt());
    }

    @Test
    void finishDerivesFailedStatusWhenNothingWasCreated() {
        FormGenerationBatch batch = startDefault();
        batch.addItem(failedItem());

        batch.finish(NOW.plusMinutes(5));

        assertEquals(FormGenerationBatchStatus.FAILED, batch.getStatus());
    }

    @Test
    void finishDerivesNothingToGenerateForReadOnlyMode() {
        FormGenerationBatch batch = FormGenerationBatch.start(PERIOD_ID, Purpose.PAA, PaaLevel.UNIT_LEVEL, 2026,
                FormGenerationBatch.READ_ONLY, false, "scheduler:period-opening", NOW);

        batch.finish(NOW.plusMinutes(5));

        assertEquals(FormGenerationBatchStatus.NOTHING_TO_GENERATE, batch.getStatus());
    }

    @Test
    void finishDerivesDryRunStatusWhenSimulated() {
        FormGenerationBatch batch = FormGenerationBatch.start(PERIOD_ID, Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL, 2026,
                FormGenerationBatch.CREATES_FORMS, true, "scheduler:period-opening", NOW);
        batch.addItem(item(FormGenerationOutcome.WOULD_CREATE));

        batch.finish(NOW.plusMinutes(5));

        assertEquals(FormGenerationBatchStatus.DRY_RUN, batch.getStatus());
    }

    @Test
    void finishDerivesCompletedStatusOtherwise() {
        FormGenerationBatch batch = startDefault();
        batch.addItem(item(FormGenerationOutcome.CREATED));

        batch.finish(NOW.plusMinutes(5));

        assertEquals(FormGenerationBatchStatus.COMPLETED, batch.getStatus());
    }

    @Test
    void finishCalledTwiceThrows() {
        FormGenerationBatch batch = startDefault();
        batch.finish(NOW.plusMinutes(5));

        assertThrows(IllegalStateException.class, () -> batch.finish(NOW.plusMinutes(10)));
    }

    // --- reconstruct ---

    @Test
    void reconstructReturnsTheBatchWithCountersAndStatusAsGivenWithoutRecalculating() {
        UUID id = UUID.randomUUID();
        FormGenerationBatchItem item = failedItem();

        FormGenerationBatch batch = FormGenerationBatch.reconstruct(id, PERIOD_ID, Purpose.SIADAP,
                PaaLevel.INDIVIDUAL_LEVEL, 2026, FormGenerationBatch.CREATES_FORMS, false,
                FormGenerationBatchStatus.COMPLETED, 3, 0, 0, 0, NOW, NOW.plusMinutes(5),
                "scheduler:period-opening", List.of(item));

        // Estado tal como veio da persistencia, mesmo sendo inconsistente com o item FAILED
        // que traz -- reconstruct nao recalcula.
        assertEquals(id, batch.getId());
        assertEquals(FormGenerationBatchStatus.COMPLETED, batch.getStatus());
        assertEquals(3, batch.getCreatedCount());
        assertEquals(0, batch.getFailedCount());
        assertEquals(1, batch.getItems().size());
    }

    // --- FormGenerationBatchItem.of ---
    // Ordem: employeeId, employeeName, unitId, unitName, outcome, generatedFormId,
    // evaluatorId, skipReason, errorMessage, createdAt.

    @Test
    void itemOfRejectsNullOutcome() {
        assertThrows(IllegalArgumentException.class, () ->
                FormGenerationBatchItem.of(UUID.randomUUID(), "Fulano Tal", null, null, null,
                        null, null, null, null, NOW));
    }

    @Test
    void itemOfRejectsNullCreatedAt() {
        assertThrows(IllegalArgumentException.class, () ->
                FormGenerationBatchItem.of(UUID.randomUUID(), "Fulano Tal", null, null,
                        FormGenerationOutcome.SKIPPED, null, null, null, null, null));
    }

    @Test
    void itemOfAcceptsNullEmployeeIdForASkippedUnitRow() {
        FormGenerationBatchItem item = FormGenerationBatchItem.of(null, null, UUID.randomUUID(), "Unidade X",
                FormGenerationOutcome.SKIPPED, null, null, "UNIT_WITHOUT_RESPONSIBLE", null, NOW);

        assertNull(item.getEmployeeId());
    }

    @Test
    void itemOfAcceptsNullErrorMessageWhenOutcomeIsNotFailed() {
        FormGenerationBatchItem item = item(FormGenerationOutcome.SKIPPED);

        assertNull(item.getErrorMessage());
    }

    @Test
    void itemOfRejectsFailedOutcomeWithNullErrorMessage() {
        assertThrows(IllegalArgumentException.class, () -> item(FormGenerationOutcome.FAILED));
    }

    @Test
    void itemOfRejectsFailedOutcomeWithBlankErrorMessage() {
        assertThrows(IllegalArgumentException.class, () ->
                FormGenerationBatchItem.of(UUID.randomUUID(), "Fulano Tal", null, null,
                        FormGenerationOutcome.FAILED, null, null, null, "   ", NOW));
    }

    @Test
    void itemOfAcceptsFailedOutcomeWithErrorMessage() {
        FormGenerationBatchItem item = failedItem();

        assertFalse(item.getErrorMessage().isBlank());
    }

    private FormGenerationBatchItem item(FormGenerationOutcome outcome) {
        return FormGenerationBatchItem.of(UUID.randomUUID(), "Fulano Tal", null, null, outcome,
                null, null, null, null, NOW);
    }

    private FormGenerationBatchItem failedItem() {
        return FormGenerationBatchItem.of(UUID.randomUUID(), "Fulano Tal", null, null,
                FormGenerationOutcome.FAILED, null, null, null, "Falha ao criar avaliacao", NOW);
    }
}
