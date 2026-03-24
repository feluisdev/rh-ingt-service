package cv.igrp.RH_Service.sigdi.domain.repository;

import cv.igrp.RH_Service.sigdi.domain.models.StrategicGoal;
import cv.igrp.RH_Service.sigdi.domain.valueobject.StrategicGoalId;

import java.util.Optional;

public interface StrategicGoalRepository {

  public StrategicGoal save(StrategicGoal goal);

  public Optional<StrategicGoal> findById(StrategicGoalId id);
}
