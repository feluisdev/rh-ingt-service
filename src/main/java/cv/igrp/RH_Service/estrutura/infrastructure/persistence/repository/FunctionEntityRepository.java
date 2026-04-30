package cv.igrp.RH_Service.estrutura.infrastructure.persistence.repository;

import cv.igrp.RH_Service.estrutura.infrastructure.persistence.entity.FunctionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface FunctionEntityRepository
        extends JpaRepository<FunctionEntity, UUID>, JpaSpecificationExecutor<FunctionEntity> {

    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, UUID id);
    Optional<FunctionEntity> findByCode(String code);
}
