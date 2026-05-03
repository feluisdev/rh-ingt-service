package cv.igrp.RH_Service.carreiras.infrastructure.persistence.repository;

import cv.igrp.RH_Service.carreiras.infrastructure.persistence.entity.GradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface GradeEntityRepository
        extends JpaRepository<GradeEntity, UUID>, JpaSpecificationExecutor<GradeEntity> {

    boolean existsByGradeNumberAndCategoryId(Integer gradeNumber, UUID categoryId);

    boolean existsByGradeNumberAndCategoryIdAndIdNot(Integer gradeNumber, UUID categoryId, UUID id);

    List<GradeEntity> findByCategoryIdOrderByGradeNumber(UUID categoryId);

    boolean existsByCategoryIdAndIsActiveTrue(UUID categoryId);
}
