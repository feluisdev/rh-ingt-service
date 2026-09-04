package cv.igrp.RH_Service.sigdi.domain.tatical.repository;

import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.PaaSubmissionPeriod;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaaSubmissionPeriodRepository {
    PaaSubmissionPeriod save(PaaSubmissionPeriod period);
    Optional<PaaSubmissionPeriod> findById(UUID id);
    List<PaaSubmissionPeriod> findAll(int page, int size);
    long countAll();
    List<PaaSubmissionPeriod> findAllByYear(Integer year);

    // Purpose-aware finders (Phase 59 — see 59-RESEARCH.md Pitfalls 1 and 4)
    Optional<PaaSubmissionPeriod> findActiveByTypeAndPurpose(PaaLevel type, Purpose purpose);
    Optional<PaaSubmissionPeriod> findActiveByTypeAndYearAndPurpose(PaaLevel type, Integer year, Purpose purpose);
    Optional<PaaSubmissionPeriod> findByTypeAndYearAndStatusAndPurpose(PaaLevel type, Integer year, String status, Purpose purpose);
    List<PaaSubmissionPeriod> findAllByPurpose(int page, int size, Purpose purpose);
    long countAllByPurpose(Purpose purpose);

    // Fase 116 (AUT-06): finder sem filtro de estado -- a fonte de elegibilidade responde
    // também sobre um período já fechado.
    Optional<PaaSubmissionPeriod> findByTypeAndYearAndPurpose(PaaLevel type, Integer year, Purpose purpose);

    // Fase 117 (PRZ-02): leitura para o fecho automático do período expirado. A fronteira é
    // estrita -- endDate < today -- por isso um período que acaba hoje ainda não entra nesta
    // lista. O varrimento em lotes é responsabilidade de quem chama; esta porta devolve sempre
    // no máximo `limit` resultados, sem noção de página além da primeira.
    List<PaaSubmissionPeriod> findOpenExpired(LocalDate today, int limit);

    /**
     * Fase 119 (PRZ-01): leitura para o agendador de abertura
     * ({@code PaaSubmissionPeriodOpeningScheduler}) -- quais os períodos {@code OPEN} a decorrer
     * hoje. Ver o comentário sobre as duas fronteiras inclusivas em
     * {@code PaaSubmissionPeriodEntityRepository.findOpenActiveOn}. Como em
     * {@link #findOpenExpired(LocalDate, int)}, o varrimento em lotes é responsabilidade de quem
     * chama; esta porta devolve sempre no máximo {@code limit} resultados, sem noção de página
     * além da primeira.
     */
    List<PaaSubmissionPeriod> findOpenActiveOn(LocalDate today, int limit);
}
