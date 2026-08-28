package cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository;

import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.FormGenerationBatchEntity;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FormGenerationBatchEntityRepository extends JpaRepository<FormGenerationBatchEntity, UUID> {

    List<FormGenerationBatchEntity> findByPeriodIdOrderByGeneratedAtDesc(UUID periodId);

    List<FormGenerationBatchEntity> findByPeriodIdInOrderByGeneratedAtDesc(Collection<UUID> periodIds);
}
