package cv.igrp.RH_Service.sigdi.domain.budget.repository;

import cv.igrp.RH_Service.sigdi.application.constants.CostDriverType;
import cv.igrp.RH_Service.sigdi.domain.budget.models.CostDriver;
import cv.igrp.RH_Service.sigdi.domain.budget.valueobject.CostDriverId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CostDriverRepository {

    CostDriver save(CostDriver costDriver);

    CostDriver update(CostDriver costDriver);

    Optional<CostDriver> findById(CostDriverId id);

    List<CostDriver> findAll();

    List<CostDriver> findByType(CostDriverType type);

    Optional<CostDriver> findActiveByType(CostDriverType type, LocalDate referenceDate);
}
