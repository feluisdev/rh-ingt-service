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

    boolean existsByCodeAndCareerId(String code, UUID careerId);

    boolean existsByCodeAndCareerIdAndIdNot(String code, UUID careerId, UUID id);

    List<CategoryEntity> findByCareerIdAndIsActiveTrue(UUID careerId);

    List<CategoryEntity> findByCareerId(UUID careerId);

    boolean existsByCareerIdAndIsActiveTrue(UUID careerId);
}
