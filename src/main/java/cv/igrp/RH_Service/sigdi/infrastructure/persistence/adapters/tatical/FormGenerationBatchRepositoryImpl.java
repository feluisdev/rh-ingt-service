package cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters.tatical;

import cv.igrp.RH_Service.sigdi.domain.tatical.models.FormGenerationBatch;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.FormGenerationBatchRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.tatical.FormGenerationBatchMapper;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.FormGenerationBatchEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.FormGenerationBatchItemEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.FormGenerationBatchEntityRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.FormGenerationBatchItemEntityRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class FormGenerationBatchRepositoryImpl implements FormGenerationBatchRepository {

    private final FormGenerationBatchEntityRepository jpaRepository;
    private final FormGenerationBatchItemEntityRepository itemJpaRepository;
    private final FormGenerationBatchMapper mapper;

    @Transactional
    @Override
    public FormGenerationBatch save(FormGenerationBatch batch) {
        FormGenerationBatchEntity entity = mapper.toEntity(batch);
        FormGenerationBatchEntity savedBatch = jpaRepository.save(entity);

        // O lote confirma na sua propria transaccao, item a item -- mesma mecanica que a
        // Fase 117 provou contra base real. Um lote sem itens nao chama o repositorio de
        // itens: nao ha nada para gravar, e uma chamada com lista vazia seria um round-trip
        // desnecessario.
        List<FormGenerationBatchItemEntity> savedItems;
        if (batch.getItems().isEmpty()) {
            savedItems = List.of();
        } else {
            List<FormGenerationBatchItemEntity> itemEntities = batch.getItems().stream()
                    .map(item -> mapper.toItemEntity(item, savedBatch.getId()))
                    .toList();
            savedItems = itemJpaRepository.saveAll(itemEntities);
        }

        return mapper.toDomain(savedBatch, savedItems);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<FormGenerationBatch> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(entity -> mapper.toDomain(entity, itemJpaRepository.findByBatchId(id)));
    }

    @Transactional(readOnly = true)
    @Override
    public List<FormGenerationBatch> findByPeriodId(UUID periodId) {
        // Leitura de sumario -- nao carrega os itens filhos. Os contadores ja vao no proprio
        // lote (createdCount/failedCount/skippedCount/pendingCount); quem precisar do detalhe
        // linha a linha usa findById. Evita um N+1 aqui, onde o consumidor tipico e uma lista.
        return jpaRepository.findByPeriodIdOrderByGeneratedAtDesc(periodId).stream()
                .map(entity -> mapper.toDomain(entity, List.of()))
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<FormGenerationBatch> findByPeriodIds(Collection<UUID> periodIds) {
        if (periodIds == null || periodIds.isEmpty()) return List.of();

        return jpaRepository.findByPeriodIdInOrderByGeneratedAtDesc(periodIds).stream()
                .map(entity -> mapper.toDomain(entity, List.of()))
                .toList();
    }

    @Transactional
    @Override
    public void deleteById(UUID id) {
        // ON DELETE CASCADE (V34) apaga as linhas filhas na base de dados; nao e preciso
        // apagar os itens aqui explicitamente.
        jpaRepository.deleteById(id);
    }
}
