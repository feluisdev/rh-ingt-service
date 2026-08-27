package cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters.tatical;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.sigdi.application.constants.FormGenerationOutcome;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.FormGenerationBatch;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.FormGenerationBatchItem;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.tatical.FormGenerationBatchMapper;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.FormGenerationBatchEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.FormGenerationBatchItemEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.FormGenerationBatchEntityRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.FormGenerationBatchItemEntityRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
}
