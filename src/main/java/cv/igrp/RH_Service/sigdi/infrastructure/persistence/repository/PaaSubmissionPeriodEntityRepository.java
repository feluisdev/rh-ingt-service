package cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository;

import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.PaaSubmissionPeriodEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("SELECT p FROM PaaSubmissionPeriodEntity p WHERE p.type = :type AND p.purpose = :purpose AND p.status = 'OPEN' AND CURRENT_DATE BETWEEN p.startDate AND p.endDate")
    Optional<PaaSubmissionPeriodEntity> findActiveByTypeAndPurpose(@Param("type") String type, @Param("purpose") String purpose);

    @Query("SELECT p FROM PaaSubmissionPeriodEntity p WHERE p.type = :type AND p.year = :year AND p.purpose = :purpose AND p.status = 'OPEN' AND CURRENT_DATE BETWEEN p.startDate AND p.endDate")
    Optional<PaaSubmissionPeriodEntity> findActiveByTypeAndYearAndPurpose(@Param("type") String type, @Param("year") Integer year, @Param("purpose") String purpose);

    Optional<PaaSubmissionPeriodEntity> findByTypeAndYearAndStatusAndPurpose(String type, Integer year, String status, String purpose);

    Page<PaaSubmissionPeriodEntity> findByPurpose(String purpose, Pageable pageable);

    long countByPurpose(String purpose);
}
