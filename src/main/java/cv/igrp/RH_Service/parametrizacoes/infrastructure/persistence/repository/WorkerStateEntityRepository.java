package cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.repository;

import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.WorkerStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface WorkerStateEntityRepository extends JpaRepository<WorkerStateEntity, UUID>, JpaSpecificationExecutor<WorkerStateEntity> {
    boolean existsByCode(String code);
    java.util.Optional<WorkerStateEntity> findByCodeIgnoreCase(String code);
}
