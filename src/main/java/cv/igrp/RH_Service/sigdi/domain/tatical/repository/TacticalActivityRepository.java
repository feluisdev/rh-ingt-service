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

  long countByStatuses(List<String> statuses);

  /**
   * Read projection of pending activities for the workflow inbox, ordered oldest-first
   * by {@code createdDate} (D-S, {@code 110-01-PLAN.md}).
   */
  List<PendingActivityRow> findPendingRows(List<String> statuses, int page, int size);
}
