package cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository;

import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.SiadapInterimImprovementActionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SiadapInterimImprovementActionEntityRepository extends JpaRepository<SiadapInterimImprovementActionEntity, UUID> {
    List<SiadapInterimImprovementActionEntity> findByEvaluationId(UUID evaluationId);
    void deleteByEvaluationId(UUID evaluationId);
}
