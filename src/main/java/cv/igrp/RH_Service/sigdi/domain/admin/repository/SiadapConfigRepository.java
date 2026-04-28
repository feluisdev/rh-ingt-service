package cv.igrp.RH_Service.sigdi.domain.admin.repository;

import cv.igrp.RH_Service.sigdi.domain.admin.models.SiadapConfig;

import java.util.Optional;

public interface SiadapConfigRepository {

  SiadapConfig save(SiadapConfig config);

  Optional<SiadapConfig> findByFiscalYear(Integer fiscalYear);
}
