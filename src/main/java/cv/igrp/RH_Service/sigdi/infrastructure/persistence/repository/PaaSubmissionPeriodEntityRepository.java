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

    // Fase 117 (PRZ-02): leitura para o fecho automático do período expirado. Fronteira
    // estrita -- endDate < :today -- um período que acaba hoje não entra. Ordenado por
    // endDate ascendente e depois por id ascendente: ordem determinista, ao contrário dos
    // vizinhos acima que ordenam por createdDate DESC, porque aqui interessa esgotar os
    // períodos mais antigos primeiro e ter uma ordem estável entre passagens do varrimento.
    @Query("SELECT p FROM PaaSubmissionPeriodEntity p WHERE p.status = 'OPEN' AND p.endDate < :today ORDER BY p.endDate ASC, p.id ASC")
    List<PaaSubmissionPeriodEntity> findOpenWithEndDateBefore(@Param("today") LocalDate today, Pageable pageable);

    // Fase 119 (PRZ-01): leitura para o agendador de abertura -- quais os períodos OPEN a
    // decorrer hoje. Ao contrário de findOpenWithEndDateBefore acima, cuja fronteira superior é
    // estrita, aqui as DUAS fronteiras são inclusivas: um período que começa hoje conta como a
    // decorrer (fronteira inferior inclusiva), e um período que acaba hoje ainda conta como a
    // decorrer (fronteira superior inclusiva) -- ainda não passou pelo fecho automático da Fase
    // 117, que só corre às 00:30, antes deste agendador (00:45). O BETWEEN reproduz o mesmo
    // predicado já usado por findAllActiveByTypeAndPurpose acima, para que "está a decorrer"
    // tenha uma única definição em todo o módulo. Ordem determinista entre passagens do
    // varrimento -- por startDate ascendente e depois por id ascendente -- porque o varrimento
    // repete sempre a primeira página (ver PaaSubmissionPeriodOpeningScheduler).
    @Query("SELECT p FROM PaaSubmissionPeriodEntity p WHERE p.status = 'OPEN' AND :today BETWEEN p.startDate AND p.endDate ORDER BY p.startDate ASC, p.id ASC")
    List<PaaSubmissionPeriodEntity> findOpenActiveOn(@Param("today") LocalDate today, Pageable pageable);
}
