package cv.igrp.RH_Service.sigdi.domain.strategy.repository;

import cv.igrp.RH_Service.sigdi.domain.strategy.models.StrategicGoal;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.InstitutionalIdentityId;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;

import java.util.List;
import java.util.Optional;

public interface StrategicGoalRepository {

  public StrategicGoal save(StrategicGoal goal);

  public Optional<StrategicGoal> findById(StrategicGoalId id);

  public List<StrategicGoal> findByIdentityId(InstitutionalIdentityId identityId);
}
