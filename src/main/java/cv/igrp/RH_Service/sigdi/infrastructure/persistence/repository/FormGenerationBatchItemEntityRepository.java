package cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository;

import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.FormGenerationBatchItemEntity;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FormGenerationBatchItemEntityRepository extends JpaRepository<FormGenerationBatchItemEntity, UUID> {

    List<FormGenerationBatchItemEntity> findByBatchId(UUID batchId);

    List<FormGenerationBatchItemEntity> findByBatchIdIn(Collection<UUID> batchIds);
}
