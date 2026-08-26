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

  /**
   * Read projection of pending change requests for the workflow inbox, ordered oldest-first
   * by {@code createdDate} (D-S, {@code 110-01-PLAN.md}). No status parameter: the inbox only
   * ever asks for {@code ChangeRequestStatus.PENDING}, and a parameter with a single legal
   * value invites misuse.
   */
  List<PendingChangeRequestRow> findPendingRows(int page, int size);

  /**
   * Count of pending change requests, for the same reason {@link #findPendingRows} takes no
   * status parameter.
   */
  long countPending();
}
