package cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters.tatical;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.sigdi.application.constants.FormGenerationBatchStatus;
import cv.igrp.RH_Service.sigdi.application.constants.FormGenerationOutcome;
import cv.igrp.RH_Service.sigdi.application.constants.FormGenerationRevertSkipReason;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.FormGenerationBatch;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.FormGenerationBatchItem;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.tatical.FormGenerationBatchMapper;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.FormGenerationBatchEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.FormGenerationBatchItemEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.FormGenerationBatchEntityRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.FormGenerationBatchItemEntityRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Fase 119, plano 01 (PRZ-06/PRZ-07). O mapper é real (não mockado) para que o
 * {@link ArgumentCaptor} das últimas duas afirmações prove a tradução de enum para código,
 * feita pelo {@link FormGenerationBatchMapper}, e não uma resposta encenada.
 */
@ExtendWith(MockitoExtension.class)
class FormGenerationBatchRepositoryImplTest {

    @Mock
    private FormGenerationBatchEntityRepository jpaRepository;

    @Mock
    private FormGenerationBatchItemEntityRepository itemJpaRepository;

    private FormGenerationBatchRepositoryImpl repository;

    private static final UUID PERIOD_ID = UUID.randomUUID();
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 27, 10, 0);

    @BeforeEach
    void setUp() {
        repository = new FormGenerationBatchRepositoryImpl(jpaRepository, itemJpaRepository, new FormGenerationBatchMapper());
    }

    private FormGenerationBatch finishedBatchWithOneCreatedItem() {
        FormGenerationBatch batch = FormGenerationBatch.start(PERIOD_ID, Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL,
                2026, FormGenerationBatch.CREATES_FORMS, false, "scheduler:period-opening", NOW);
        batch.addItem(FormGenerationBatchItem.of(UUID.randomUUID(), "Fulano Tal", null, null,
                FormGenerationOutcome.CREATED, UUID.randomUUID(), UUID.randomUUID(), null, null, NOW));
        batch.finish(NOW.plusMinutes(5));
        return batch;
    }

    @Test
    void saveWritesTheBatchThenTheItemsAndReturnsTheReconstructedBatch() {
        FormGenerationBatch batch = finishedBatchWithOneCreatedItem();

        FormGenerationBatchEntity savedBatchEntity = new FormGenerationBatchEntity();
        savedBatchEntity.setId(batch.getId());
        savedBatchEntity.setPeriodId(PERIOD_ID);
        savedBatchEntity.setPurpose(Purpose.SIADAP.getCode());
        savedBatchEntity.setType(PaaLevel.INDIVIDUAL_LEVEL.getCode());
        savedBatchEntity.setYear(2026);
        savedBatchEntity.setGenerationMode(FormGenerationBatch.CREATES_FORMS);
        savedBatchEntity.setStatus("COMPLETED");
        savedBatchEntity.setGeneratedAt(NOW);
        savedBatchEntity.setFinishedAt(NOW.plusMinutes(5));
        savedBatchEntity.setGeneratedBy("scheduler:period-opening");

        FormGenerationBatchItemEntity savedItemEntity = new FormGenerationBatchItemEntity();
        savedItemEntity.setId(UUID.randomUUID());
        savedItemEntity.setBatchId(batch.getId());
        savedItemEntity.setOutcome("CREATED");
        savedItemEntity.setCreatedAt(NOW);

        when(jpaRepository.save(any())).thenReturn(savedBatchEntity);
        when(itemJpaRepository.saveAll(any())).thenReturn(List.of(savedItemEntity));

        FormGenerationBatch result = repository.save(batch);

        verify(jpaRepository).save(any());
        verify(itemJpaRepository).saveAll(any());
        assertEquals(batch.getId(), result.getId());
        assertEquals(1, result.getItems().size());
    }

    @Test
    void saveOfABatchWithNoItemsOnlySavesTheBatchRowAndDoesNotCallTheItemRepository() {
        FormGenerationBatch batch = FormGenerationBatch.start(PERIOD_ID, Purpose.PAA, PaaLevel.UNIT_LEVEL,
                2026, FormGenerationBatch.READ_ONLY, false, "scheduler:period-opening", NOW);

        FormGenerationBatchEntity savedBatchEntity = new FormGenerationBatchEntity();
        savedBatchEntity.setId(batch.getId());
        savedBatchEntity.setPeriodId(PERIOD_ID);
        savedBatchEntity.setPurpose(Purpose.PAA.getCode());
        savedBatchEntity.setType(PaaLevel.UNIT_LEVEL.getCode());
        savedBatchEntity.setYear(2026);
        savedBatchEntity.setGenerationMode(FormGenerationBatch.READ_ONLY);
        savedBatchEntity.setGeneratedAt(NOW);
        savedBatchEntity.setGeneratedBy("scheduler:period-opening");

        when(jpaRepository.save(any())).thenReturn(savedBatchEntity);

        FormGenerationBatch result = repository.save(batch);

        verify(jpaRepository).save(any());
        verify(itemJpaRepository, never()).saveAll(any());
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void findByIdOfANonexistentIdReturnsEmptyWithoutThrowing() {
        UUID id = UUID.randomUUID();
        when(jpaRepository.findById(id)).thenReturn(Optional.empty());

        Optional<FormGenerationBatch> result = repository.findById(id);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByIdOfAnExistingBatchAlsoLoadsItsItemsByBatchId() {
        UUID id = UUID.randomUUID();
        FormGenerationBatchEntity entity = new FormGenerationBatchEntity();
        entity.setId(id);
        entity.setPeriodId(PERIOD_ID);
        entity.setPurpose(Purpose.SIADAP.getCode());
        entity.setType(PaaLevel.INDIVIDUAL_LEVEL.getCode());
        entity.setYear(2026);
        entity.setGenerationMode(FormGenerationBatch.CREATES_FORMS);
        entity.setStatus("COMPLETED");
        entity.setGeneratedAt(NOW);
        entity.setGeneratedBy("scheduler:period-opening");

        FormGenerationBatchItemEntity itemEntity = new FormGenerationBatchItemEntity();
        itemEntity.setId(UUID.randomUUID());
        itemEntity.setBatchId(id);
        itemEntity.setOutcome("CREATED");
        itemEntity.setCreatedAt(NOW);

        when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));
        when(itemJpaRepository.findByBatchId(id)).thenReturn(List.of(itemEntity));

        Optional<FormGenerationBatch> result = repository.findById(id);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getItems().size());
        verify(itemJpaRepository).findByBatchId(id);
    }

    @Test
    void findByPeriodIdReturnsTheBatchesInTheOrderTheJpaRepositoryGivesThem() {
        FormGenerationBatchEntity newer = new FormGenerationBatchEntity();
        newer.setId(UUID.randomUUID());
        newer.setPeriodId(PERIOD_ID);
        newer.setPurpose(Purpose.SIADAP.getCode());
        newer.setType(PaaLevel.INDIVIDUAL_LEVEL.getCode());
        newer.setYear(2026);
        newer.setGenerationMode(FormGenerationBatch.CREATES_FORMS);
        newer.setStatus("COMPLETED");
        newer.setGeneratedAt(NOW.plusDays(1));
        newer.setGeneratedBy("scheduler:period-opening");

        FormGenerationBatchEntity older = new FormGenerationBatchEntity();
        older.setId(UUID.randomUUID());
        older.setPeriodId(PERIOD_ID);
        older.setPurpose(Purpose.SIADAP.getCode());
        older.setType(PaaLevel.INDIVIDUAL_LEVEL.getCode());
        older.setYear(2026);
        older.setGenerationMode(FormGenerationBatch.CREATES_FORMS);
        older.setStatus("COMPLETED");
        older.setGeneratedAt(NOW);
        older.setGeneratedBy("scheduler:period-opening");

        when(jpaRepository.findByPeriodIdOrderByGeneratedAtDesc(PERIOD_ID)).thenReturn(List.of(newer, older));

        List<FormGenerationBatch> result = repository.findByPeriodId(PERIOD_ID);

        assertEquals(2, result.size());
        assertEquals(newer.getId(), result.get(0).getId());
        assertEquals(older.getId(), result.get(1).getId());
    }

    @Test
    void findByPeriodIdsWithAnEmptyOrNullCollectionReturnsAnEmptyListWithoutTouchingTheJpaRepository() {
        assertTrue(repository.findByPeriodIds(null).isEmpty());
        assertTrue(repository.findByPeriodIds(List.of()).isEmpty());

        verify(jpaRepository, never()).findByPeriodIdInOrderByGeneratedAtDesc(any());
    }

    @Test
    void saveSendsTheEnumCodesToTheJpaRepositoryAndNotTheEnumToString() {
        FormGenerationBatch batch = finishedBatchWithOneCreatedItem();

        FormGenerationBatchEntity savedBatchEntity = new FormGenerationBatchEntity();
        savedBatchEntity.setId(batch.getId());
        savedBatchEntity.setPeriodId(PERIOD_ID);
        savedBatchEntity.setPurpose(Purpose.SIADAP.getCode());
        savedBatchEntity.setType(PaaLevel.INDIVIDUAL_LEVEL.getCode());
        savedBatchEntity.setYear(2026);
        savedBatchEntity.setGenerationMode(FormGenerationBatch.CREATES_FORMS);
        savedBatchEntity.setStatus("COMPLETED");
        savedBatchEntity.setGeneratedAt(NOW);
        savedBatchEntity.setGeneratedBy("scheduler:period-opening");
        when(jpaRepository.save(any())).thenReturn(savedBatchEntity);
        when(itemJpaRepository.saveAll(any())).thenReturn(List.of());

        repository.save(batch);

        ArgumentCaptor<FormGenerationBatchEntity> batchCaptor = ArgumentCaptor.forClass(FormGenerationBatchEntity.class);
        verify(jpaRepository).save(batchCaptor.capture());
        assertEquals("COMPLETED", batchCaptor.getValue().getStatus());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<FormGenerationBatchItemEntity>> itemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(itemJpaRepository).saveAll(itemsCaptor.capture());
        assertEquals("CREATED", itemsCaptor.getValue().get(0).getOutcome());
    }

    /**
     * Fase 119, plano 08 (PRZ-07). Alinhar o esquema (Tasks 1-2) nao chega: uma mensagem de
     * excepcao muito longa continua a poder exceder o que a coluna aceite noutro ambiente, e o
     * modo de falha e o pior possivel -- perde-se o lote inteiro, em silencio (medido no
     * 119-07). Este teste prova o que importa: nao so que a mensagem fica curta, mas que a
     * gravacao do lote nao e impedida por uma mensagem desproporcionada, e que o item chega ao
     * repositorio JPA com a mensagem truncada e marcada -- nao apenas cortada em silencio.
     */
    @Test
    void saveOfAFailedItemWithAnOversizedErrorMessageStillReachesTheJpaRepositoryWithTheMessageTruncatedAndMarked() {
        String oversizedMessage = "x".repeat(10_000);

        FormGenerationBatch batch = FormGenerationBatch.start(PERIOD_ID, Purpose.SIADAP, PaaLevel.INDIVIDUAL_LEVEL,
                2026, FormGenerationBatch.CREATES_FORMS, false, "scheduler:period-opening", NOW);
        batch.addItem(FormGenerationBatchItem.of(UUID.randomUUID(), "Fulano Tal", null, null,
                FormGenerationOutcome.FAILED, null, null, null, oversizedMessage, NOW));
        batch.finish(NOW.plusMinutes(5));

        FormGenerationBatchEntity savedBatchEntity = new FormGenerationBatchEntity();
        savedBatchEntity.setId(batch.getId());
        savedBatchEntity.setPeriodId(PERIOD_ID);
        savedBatchEntity.setPurpose(Purpose.SIADAP.getCode());
        savedBatchEntity.setType(PaaLevel.INDIVIDUAL_LEVEL.getCode());
        savedBatchEntity.setYear(2026);
        savedBatchEntity.setGenerationMode(FormGenerationBatch.CREATES_FORMS);
        savedBatchEntity.setStatus("PARTIAL");
        savedBatchEntity.setGeneratedAt(NOW);
        savedBatchEntity.setFinishedAt(NOW.plusMinutes(5));
        savedBatchEntity.setGeneratedBy("scheduler:period-opening");

        when(jpaRepository.save(any())).thenReturn(savedBatchEntity);
        when(itemJpaRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // A propria chamada nao pode lancar -- e o modo de falha medido no 119-07: o
        // DataIntegrityViolationException do Postgres a rebentar o INSERT do item, e com ele o
        // lote inteiro (itens bem sucedidos incluidos).
        FormGenerationBatch result = repository.save(batch);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<FormGenerationBatchItemEntity>> itemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(itemJpaRepository).saveAll(itemsCaptor.capture());

        String savedMessage = itemsCaptor.getValue().get(0).getErrorMessage();
        assertTrue(savedMessage.length() < oversizedMessage.length(),
                "a mensagem gravada tem de ficar mais curta do que a original");
        assertTrue(savedMessage.endsWith("[...truncado]"),
                "a truncagem tem de ficar marcada no proprio texto, para quem le nao pensar que a mensagem acabou ali");
        assertEquals(1, result.getItems().size());
    }

    // --- markReverted (Fase 120, plano 01, PRZ-04) ---

    private FormGenerationBatchEntity closedCompletedEntity(UUID id) {
        FormGenerationBatchEntity entity = new FormGenerationBatchEntity();
        entity.setId(id);
        entity.setPeriodId(PERIOD_ID);
        entity.setPurpose(Purpose.SIADAP.getCode());
        entity.setType(PaaLevel.INDIVIDUAL_LEVEL.getCode());
        entity.setYear(2026);
        entity.setGenerationMode(FormGenerationBatch.CREATES_FORMS);
        entity.setStatus("COMPLETED");
        entity.setGeneratedAt(NOW);
        entity.setFinishedAt(NOW.plusMinutes(5));
        entity.setGeneratedBy("scheduler:period-opening");
        return entity;
    }

    @Test
    void markRevertedUpdatesItemsOnceAndDerivesRevertedStatusWhenNothingIsBlocked() {
        UUID batchId = UUID.randomUUID();
        UUID f1 = UUID.randomUUID();
        UUID f2 = UUID.randomUUID();
        LocalDateTime revertedAt = NOW.plusHours(1);

        FormGenerationBatchEntity entity = closedCompletedEntity(batchId);
        when(jpaRepository.findById(batchId)).thenReturn(Optional.of(entity));
        when(itemJpaRepository.findByBatchId(batchId)).thenReturn(List.of());

        FormGenerationBatch result = repository.markReverted(batchId, List.of(f1, f2), Map.of(), revertedAt, "user@x");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<java.util.Collection<UUID>> idsCaptor = ArgumentCaptor.forClass(java.util.Collection.class);
        verify(itemJpaRepository).markItemsReverted(eq(batchId), idsCaptor.capture(), eq(revertedAt));
        assertEquals(Set.of(f1, f2), Set.copyOf(idsCaptor.getValue()));
        verify(itemJpaRepository, never()).markItemsRevertBlocked(any(), any(), any());

        ArgumentCaptor<FormGenerationBatchEntity> batchCaptor = ArgumentCaptor.forClass(FormGenerationBatchEntity.class);
        verify(jpaRepository).save(batchCaptor.capture());
        assertEquals("REVERTED", batchCaptor.getValue().getStatus());
        assertEquals(revertedAt, batchCaptor.getValue().getRevertedAt());
        assertEquals("user@x", batchCaptor.getValue().getRevertedBy());
        assertEquals(2, batchCaptor.getValue().getRevertedCount());
        assertEquals(0, batchCaptor.getValue().getRevertBlockedCount());

        assertEquals(FormGenerationBatchStatus.REVERTED, result.getStatus());
    }

    @Test
    void markRevertedGroupsBlockedItemsByReasonWithOneUpdatePerDistinctReason() {
        UUID batchId = UUID.randomUUID();
        UUID f1 = UUID.randomUUID();
        UUID f2 = UUID.randomUUID();
        UUID f3 = UUID.randomUUID();
        LocalDateTime revertedAt = NOW.plusHours(1);

        FormGenerationBatchEntity entity = closedCompletedEntity(batchId);
        when(jpaRepository.findById(batchId)).thenReturn(Optional.of(entity));
        when(itemJpaRepository.findByBatchId(batchId)).thenReturn(List.of());

        Map<UUID, FormGenerationRevertSkipReason> blocked = Map.of(
                f2, FormGenerationRevertSkipReason.PHASE_ADVANCED,
                f3, FormGenerationRevertSkipReason.PHASE_ADVANCED);

        FormGenerationBatch result = repository.markReverted(batchId, List.of(f1), blocked, revertedAt, "user@x");

        verify(itemJpaRepository, times(1)).markItemsRevertBlocked(any(), any(), any());
        verify(itemJpaRepository).markItemsRevertBlocked(eq(batchId),
                argThat(ids -> Set.copyOf(ids).equals(Set.of(f2, f3))), eq("PHASE_ADVANCED"));

        assertEquals(FormGenerationBatchStatus.PARTIALLY_REVERTED, result.getStatus());
    }

    @Test
    void markRevertedNeverCallsSaveAllAndSavesTheSameLoadedEntityInstance() {
        UUID batchId = UUID.randomUUID();
        UUID f1 = UUID.randomUUID();
        FormGenerationBatchEntity entity = closedCompletedEntity(batchId);
        when(jpaRepository.findById(batchId)).thenReturn(Optional.of(entity));
        when(itemJpaRepository.findByBatchId(batchId)).thenReturn(List.of());

        repository.markReverted(batchId, List.of(f1), Map.of(), NOW.plusHours(1), "user@x");

        verify(itemJpaRepository, never()).saveAll(any());
        verify(jpaRepository).save(entity);
    }

    @Test
    void findByIdMapsRevertedAtAndRevertSkipReasonOnItems() {
        UUID id = UUID.randomUUID();
        FormGenerationBatchEntity entity = closedCompletedEntity(id);

        FormGenerationBatchItemEntity itemEntity = new FormGenerationBatchItemEntity();
        itemEntity.setId(UUID.randomUUID());
        itemEntity.setBatchId(id);
        itemEntity.setOutcome("CREATED");
        itemEntity.setCreatedAt(NOW);
        itemEntity.setRevertedAt(NOW.plusHours(1));
        itemEntity.setRevertSkipReason("PHASE_ADVANCED");

        when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));
        when(itemJpaRepository.findByBatchId(id)).thenReturn(List.of(itemEntity));

        FormGenerationBatch result = repository.findById(id).orElseThrow();

        FormGenerationBatchItem item = result.getItems().get(0);
        assertEquals(NOW.plusHours(1), item.getRevertedAt());
        assertEquals(FormGenerationRevertSkipReason.PHASE_ADVANCED, item.getRevertSkipReason());
    }

    @Test
    void mapperThrowsOnInvalidRevertSkipReasonCode() {
        UUID id = UUID.randomUUID();
        FormGenerationBatchEntity entity = closedCompletedEntity(id);

        FormGenerationBatchItemEntity itemEntity = new FormGenerationBatchItemEntity();
        itemEntity.setId(UUID.randomUUID());
        itemEntity.setBatchId(id);
        itemEntity.setOutcome("CREATED");
        itemEntity.setCreatedAt(NOW);
        itemEntity.setRevertSkipReason("NOT_A_REAL_CODE");

        when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));
        when(itemJpaRepository.findByBatchId(id)).thenReturn(List.of(itemEntity));

        assertThrows(IgrpResponseStatusException.class, () -> repository.findById(id));
    }
}
