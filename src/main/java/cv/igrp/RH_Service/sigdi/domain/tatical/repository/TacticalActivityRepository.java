package cv.igrp.RH_Service.sigdi.domain.tatical.repository;

import cv.igrp.RH_Service.sigdi.domain.tatical.models.TacticalActivity;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.TacticalActivityId;

import java.util.Optional;

public interface TacticalActivityRepository {

  TacticalActivity save(TacticalActivity activity);

  Optional<TacticalActivity> findById(TacticalActivityId id);

  Optional<TacticalActivity> findByIdFull(TacticalActivityId id);
}
