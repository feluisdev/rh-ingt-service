package cv.igrp.RH_Service.carreiras.infrastructure.persistence.repository;

import cv.igrp.RH_Service.carreiras.infrastructure.persistence.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CategoryEntityRepository
        extends JpaRepository<CategoryEntity, UUID>, JpaSpecificationExecutor<CategoryEntity> {

    boolean existsByCodeAndCareer_Id(String code, UUID careerId);

    boolean existsByCodeAndCareer_IdAndIdNot(String code, UUID careerId, UUID id);

    List<CategoryEntity> findByCareer_IdAndIsActiveTrue(UUID careerId);

    List<CategoryEntity> findByCareer_Id(UUID careerId);

    boolean existsByCareer_IdAndIsActiveTrue(UUID careerId);

    long countByCareer_Id(UUID careerId);
}
