package cv.igrp.RH_Service.sigdi.domain.strategy.repository;

import cv.igrp.RH_Service.sigdi.domain.strategy.models.StrategyMapLink;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.InstitutionalIdentityId;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategyMapLinkId;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;

import java.util.List;
import java.util.Optional;

public interface StrategyMapLinkRepository {

  public StrategyMapLink save(StrategyMapLink link);

  public Optional<StrategyMapLink> findById(StrategyMapLinkId id);

  public Optional<StrategyMapLink> findBySourceAndTarget(StrategicGoalId sourceGoalId, StrategicGoalId targetGoalId);

  public List<StrategyMapLink> findByIdentityId(InstitutionalIdentityId identityId);
}
