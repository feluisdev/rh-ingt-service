package cv.igrp.RH_Service.sigdi.domain.tatical.repository;

import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.PaaSubmissionPeriod;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaaSubmissionPeriodRepository {
    PaaSubmissionPeriod save(PaaSubmissionPeriod period);
    Optional<PaaSubmissionPeriod> findById(UUID id);
    Optional<PaaSubmissionPeriod> findActiveByType(PaaLevel type);
    Optional<PaaSubmissionPeriod> findByTypeAndYearAndStatus(PaaLevel type, Integer year, String status);
    List<PaaSubmissionPeriod> findAll(int page, int size);
    long countAll();

    // Purpose-aware finders (Phase 59 — see 59-RESEARCH.md Pitfalls 1 and 4)
    Optional<PaaSubmissionPeriod> findActiveByTypeAndPurpose(PaaLevel type, Purpose purpose);
    Optional<PaaSubmissionPeriod> findActiveByTypeAndYearAndPurpose(PaaLevel type, Integer year, Purpose purpose);
    Optional<PaaSubmissionPeriod> findByTypeAndYearAndStatusAndPurpose(PaaLevel type, Integer year, String status, Purpose purpose);
    List<PaaSubmissionPeriod> findAllByPurpose(int page, int size, Purpose purpose);
    long countAllByPurpose(Purpose purpose);
}
