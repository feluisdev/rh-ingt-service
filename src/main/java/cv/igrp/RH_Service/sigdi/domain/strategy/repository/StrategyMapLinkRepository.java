package cv.igrp.RH_Service.sigdi.domain.strategy.repository;

import cv.igrp.RH_Service.sigdi.domain.strategy.models.StrategyMapLink;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategyMapLinkId;

import java.util.Optional;

public interface StrategyMapLinkRepository {

  public StrategyMapLink save(StrategyMapLink link);

  public Optional<StrategyMapLink> findById(StrategyMapLinkId id);
}
