package cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.repository;

import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.LeaveTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface LeaveTypeEntityRepository extends JpaRepository<LeaveTypeEntity, UUID>, JpaSpecificationExecutor<LeaveTypeEntity> {
    boolean existsByCode(String code);
}
