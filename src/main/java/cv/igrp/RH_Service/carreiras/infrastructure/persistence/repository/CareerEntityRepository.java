package cv.igrp.RH_Service.carreiras.infrastructure.persistence.repository;

import cv.igrp.RH_Service.carreiras.infrastructure.persistence.entity.CareerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface CareerEntityRepository
        extends JpaRepository<CareerEntity, UUID>, JpaSpecificationExecutor<CareerEntity> {

    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, UUID id);
    Optional<CareerEntity> findByCode(String code);
}
