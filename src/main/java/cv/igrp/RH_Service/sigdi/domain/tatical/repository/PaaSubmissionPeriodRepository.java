package cv.igrp.RH_Service.sigdi.domain.tatical.repository;

import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
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
}
