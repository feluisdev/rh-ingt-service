package cv.igrp.RH_Service.sigdi.domain.tatical.repository;

import cv.igrp.RH_Service.shared.domain.pagination.PageResult;
import cv.igrp.RH_Service.sigdi.domain.tatical.filter.TaticalActivityFilter;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.TacticalActivity;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.TacticalActivityId;

import java.util.List;
import java.util.Optional;

public interface TacticalActivityRepository {

  TacticalActivity save(TacticalActivity activity);

  Optional<TacticalActivity> findById(TacticalActivityId id);

  Optional<TacticalActivity> findByIdFull(TacticalActivityId id);

  PageResult<TacticalActivity> findAll(TaticalActivityFilter filter);

  List<TacticalActivity> findByStatuses(List<String> statuses, int page, int size);

  long countByStatuses(List<String> statuses);
}
