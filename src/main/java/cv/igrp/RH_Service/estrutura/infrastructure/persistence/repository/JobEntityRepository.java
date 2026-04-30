package cv.igrp.RH_Service.estrutura.infrastructure.persistence.repository;

import cv.igrp.RH_Service.estrutura.infrastructure.persistence.entity.JobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface JobEntityRepository
        extends JpaRepository<JobEntity, UUID>, JpaSpecificationExecutor<JobEntity> {

    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, UUID id);
    Optional<JobEntity> findByCode(String code);
}
