package cv.igrp.RH_Service.sigdi.domain.tatical.repository;

import cv.igrp.RH_Service.sigdi.domain.tatical.models.ChangeRequest;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.ChangeRequestId;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.TacticalActivityId;

import java.util.List;
import java.util.Optional;

public interface ChangeRequestRepository {

  ChangeRequest save(ChangeRequest changeRequest);

  Optional<ChangeRequest> findById(ChangeRequestId id);

  List<ChangeRequest> findByActivityId(TacticalActivityId activityId);
}
