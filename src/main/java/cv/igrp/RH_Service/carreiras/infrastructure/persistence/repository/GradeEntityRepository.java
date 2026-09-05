package cv.igrp.RH_Service.carreiras.infrastructure.persistence.repository;

import cv.igrp.RH_Service.carreiras.infrastructure.persistence.entity.GradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface GradeEntityRepository
        extends JpaRepository<GradeEntity, UUID>, JpaSpecificationExecutor<GradeEntity> {

    boolean existsByGradeNumberAndCategory_Id(Integer gradeNumber, UUID categoryId);

    boolean existsByGradeNumberAndCategory_IdAndIdNot(Integer gradeNumber, UUID categoryId, UUID id);

    List<GradeEntity> findByCategory_IdOrderByGradeNumber(UUID categoryId);

    boolean existsByCategory_IdAndIsActiveTrue(UUID categoryId);

    long countByCategory_Id(UUID categoryId);
}
