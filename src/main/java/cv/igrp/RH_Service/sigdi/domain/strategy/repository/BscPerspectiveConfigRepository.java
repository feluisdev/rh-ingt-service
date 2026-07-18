package cv.igrp.RH_Service.sigdi.domain.strategy.repository;

import cv.igrp.RH_Service.sigdi.domain.strategy.models.BscPerspectiveConfig;

import java.util.List;
import java.util.Optional;

public interface BscPerspectiveConfigRepository {

  /**
   * Returns all 4 configured perspectives, sorted ascending by displayOrder.
   */
  List<BscPerspectiveConfig> findAll();

  Optional<BscPerspectiveConfig> findByCode(String code);

  /**
   * Persists the given rows (label + displayOrder updates only -- rows always already exist,
   * seeded by V26). Returns the saved rows.
   */
  List<BscPerspectiveConfig> saveAll(List<BscPerspectiveConfig> configs);
}
