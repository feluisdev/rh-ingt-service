package cv.igrp.RH_Service.sigdi.domain.strategy.repository;

import cv.igrp.RH_Service.sigdi.domain.strategy.models.StrategicGoal;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.InstitutionalIdentityId;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;

import java.util.List;
import java.util.Optional;

public interface StrategicGoalRepository {

  StrategicGoal save(StrategicGoal goal);

  Optional<StrategicGoal> findById(StrategicGoalId id);

  List<StrategicGoal> findByIdentityId(InstitutionalIdentityId identityId);

  List<StrategicGoal> findAll(InstitutionalIdentityId identityId, String perspective,
      String status, String parentGoalId, int page, int size);

  long countAll(InstitutionalIdentityId identityId, String perspective,
      String status, String parentGoalId);
}
