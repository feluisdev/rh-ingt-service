package cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository;

import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.PaaSubmissionPeriodEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaaSubmissionPeriodEntityRepository extends JpaRepository<PaaSubmissionPeriodEntity, UUID>, JpaSpecificationExecutor<PaaSubmissionPeriodEntity> {

    // Defensive finders (see 59-REVIEW.md CR-02): the schema does not enforce uniqueness
    // on (type, year, purpose[, status]), so these return every match ordered newest-first;
    // callers take the first result instead of relying on Spring Data's single-result
    // Optional methods, which throw IncorrectResultSizeDataAccessException on 2+ rows.
    @Query("SELECT p FROM PaaSubmissionPeriodEntity p WHERE p.type = :type AND p.purpose = :purpose AND p.status = 'OPEN' AND :today BETWEEN p.startDate AND p.endDate ORDER BY p.createdDate DESC")
    List<PaaSubmissionPeriodEntity> findAllActiveByTypeAndPurpose(@Param("type") String type, @Param("purpose") String purpose, @Param("today") LocalDate today);

    @Query("SELECT p FROM PaaSubmissionPeriodEntity p WHERE p.type = :type AND p.year = :year AND p.purpose = :purpose AND p.status = 'OPEN' AND :today BETWEEN p.startDate AND p.endDate ORDER BY p.createdDate DESC")
    List<PaaSubmissionPeriodEntity> findAllActiveByTypeAndYearAndPurpose(@Param("type") String type, @Param("year") Integer year, @Param("purpose") String purpose, @Param("today") LocalDate today);

    @Query("SELECT p FROM PaaSubmissionPeriodEntity p WHERE p.type = :type AND p.year = :year AND p.status = :status AND p.purpose = :purpose ORDER BY p.createdDate DESC")
    List<PaaSubmissionPeriodEntity> findAllByTypeAndYearAndStatusAndPurpose(@Param("type") String type, @Param("year") Integer year, @Param("status") String status, @Param("purpose") String purpose);

    // Fase 116 (AUT-06): a fonte de elegibilidade tem de responder também sobre um período
    // já fechado -- ver quem *era* elegível é metade da utilidade da consulta -- por isso
    // este finder, ao contrário dos vizinhos acima, não filtra por status nem por data.
    // Devolve List, não Optional, pela mesma razão defensiva do comentário no topo deste
    // ficheiro: a ausência de unicidade em (type, year, purpose).
    @Query("SELECT p FROM PaaSubmissionPeriodEntity p WHERE p.type = :type AND p.year = :year AND p.purpose = :purpose ORDER BY p.createdDate DESC")
    List<PaaSubmissionPeriodEntity> findAllByTypeAndYearAndPurpose(@Param("type") String type, @Param("year") Integer year, @Param("purpose") String purpose);

    // Rule 3 (Phase 70 — SOBREP-01/02/03): unscoped by type/status/purpose on purpose — cross-type
    // overlap validation must see every period for the year, including CLOSED/reopened records.
    @Query("SELECT p FROM PaaSubmissionPeriodEntity p WHERE p.year = :year ORDER BY p.createdDate DESC")
    List<PaaSubmissionPeriodEntity> findAllByYear(@Param("year") Integer year);

    Page<PaaSubmissionPeriodEntity> findByPurpose(String purpose, Pageable pageable);

    long countByPurpose(String purpose);
}
