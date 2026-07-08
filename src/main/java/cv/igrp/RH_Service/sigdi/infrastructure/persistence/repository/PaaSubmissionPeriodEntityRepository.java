package cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository;

import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.PaaSubmissionPeriodEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaaSubmissionPeriodEntityRepository extends JpaRepository<PaaSubmissionPeriodEntity, UUID>, JpaSpecificationExecutor<PaaSubmissionPeriodEntity> {

    @Query("SELECT p FROM PaaSubmissionPeriodEntity p WHERE p.type = :type AND p.status = 'OPEN' AND CURRENT_DATE BETWEEN p.startDate AND p.endDate")
    Optional<PaaSubmissionPeriodEntity> findActiveByType(@Param("type") String type);

    Optional<PaaSubmissionPeriodEntity> findByTypeAndYearAndStatus(String type, Integer year, String status);

    // Defensive finders (see 59-REVIEW.md CR-02): the schema does not enforce uniqueness
    // on (type, year, purpose[, status]), so these return every match ordered newest-first;
    // callers take the first result instead of relying on Spring Data's single-result
    // Optional methods, which throw IncorrectResultSizeDataAccessException on 2+ rows.
    @Query("SELECT p FROM PaaSubmissionPeriodEntity p WHERE p.type = :type AND p.purpose = :purpose AND p.status = 'OPEN' AND CURRENT_DATE BETWEEN p.startDate AND p.endDate ORDER BY p.createdDate DESC")
    List<PaaSubmissionPeriodEntity> findAllActiveByTypeAndPurpose(@Param("type") String type, @Param("purpose") String purpose);

    @Query("SELECT p FROM PaaSubmissionPeriodEntity p WHERE p.type = :type AND p.year = :year AND p.purpose = :purpose AND p.status = 'OPEN' AND CURRENT_DATE BETWEEN p.startDate AND p.endDate ORDER BY p.createdDate DESC")
    List<PaaSubmissionPeriodEntity> findAllActiveByTypeAndYearAndPurpose(@Param("type") String type, @Param("year") Integer year, @Param("purpose") String purpose);

    @Query("SELECT p FROM PaaSubmissionPeriodEntity p WHERE p.type = :type AND p.year = :year AND p.status = :status AND p.purpose = :purpose ORDER BY p.createdDate DESC")
    List<PaaSubmissionPeriodEntity> findAllByTypeAndYearAndStatusAndPurpose(@Param("type") String type, @Param("year") Integer year, @Param("status") String status, @Param("purpose") String purpose);

    Page<PaaSubmissionPeriodEntity> findByPurpose(String purpose, Pageable pageable);

    long countByPurpose(String purpose);
}
