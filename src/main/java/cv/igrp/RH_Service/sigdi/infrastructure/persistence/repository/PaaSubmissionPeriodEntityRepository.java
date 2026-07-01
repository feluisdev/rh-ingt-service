package cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository;

import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.PaaSubmissionPeriodEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaaSubmissionPeriodEntityRepository extends JpaRepository<PaaSubmissionPeriodEntity, UUID>, JpaSpecificationExecutor<PaaSubmissionPeriodEntity> {

    @Query("SELECT p FROM PaaSubmissionPeriodEntity p WHERE p.type = :type AND p.status = 'OPEN' AND CURRENT_DATE BETWEEN p.startDate AND p.endDate")
    Optional<PaaSubmissionPeriodEntity> findActiveByType(@Param("type") String type);

    Optional<PaaSubmissionPeriodEntity> findByTypeAndYearAndStatus(String type, Integer year, String status);
}
