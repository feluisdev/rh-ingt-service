package cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository;

import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.SiadapInterimObjectiveRevisionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SiadapInterimObjectiveRevisionEntityRepository extends JpaRepository<SiadapInterimObjectiveRevisionEntity, UUID> {
    List<SiadapInterimObjectiveRevisionEntity> findByEvaluationId(UUID evaluationId);
    void deleteByEvaluationId(UUID evaluationId);
}
