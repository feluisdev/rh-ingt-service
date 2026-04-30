package cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.repository;

import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.LeaveMobilitySubtypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface LeaveMobilitySubtypeEntityRepository extends JpaRepository<LeaveMobilitySubtypeEntity, UUID>, JpaSpecificationExecutor<LeaveMobilitySubtypeEntity> {
    boolean existsByCode(String code);
}
