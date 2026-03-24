package cv.igrp.RH_Service.sigdi.domain.repository;

import cv.igrp.RH_Service.sigdi.domain.models.StrategyMapLink;
import cv.igrp.RH_Service.sigdi.domain.valueobject.StrategyMapLinkId;

import java.util.Optional;

public interface StrategyMapLinkRepository {

  public StrategyMapLink save(StrategyMapLink link);

  public Optional<StrategyMapLink> findById(StrategyMapLinkId id);
}
