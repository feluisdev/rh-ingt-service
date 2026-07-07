package cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository;

import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.CompetencyItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CompetencyItemEntityRepository extends
    JpaRepository<CompetencyItemEntity, UUID>,
    JpaSpecificationExecutor<CompetencyItemEntity>
{
    List<CompetencyItemEntity> findByEvaluationId(UUID evaluationId);
    void deleteByEvaluationId(UUID evaluationId);
}
