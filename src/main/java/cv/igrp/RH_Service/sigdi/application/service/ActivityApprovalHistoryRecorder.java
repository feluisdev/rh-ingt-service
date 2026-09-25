package cv.igrp.RH_Service.sigdi.application.service;

import cv.igrp.RH_Service.shared.security.SecurityContextHelper;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.TacticalActivityId;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.TacticalActivitiesEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.TaticalActivityHistoryEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.TacticalActivitiesEntityRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.TaticalActivityHistoryEntityRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Single owner of the write side of {@code t_activity_approval_history}. Before this class
 * existed ({@code A-135-2AB}), only two of the seven places that transition a
 * {@code TacticalActivity} wrote a history line at all --
 * {@code CreateTacticalActivityCommandHandler} and
 * {@code ChangeStatusTacticalActivityCommandHandler} -- and both built
 * {@link TaticalActivityHistoryEntity} by hand, with the same ten-line block, duplicated. The
 * other five transition handlers ({@code Submit}, {@code Approve}, {@code Reject},
 * {@code Accept}, {@code Negotiate}) did not write history at all: the base closed Phase 135 with
 * five rows, all {@code from_status = NEW}. This class exists so a transition either produces
 * exactly one history line or none, decided in one place, for every caller present and future.
 * <p>
 * <b>What it promises.</b> One row per successful call, with {@code fromStatus} exactly as the
 * caller passes it -- never re-read from the database after the save, because by the time this
 * method runs the persisted activity already holds the new status, and reading it back would
 * record a transition from the new status to itself.
 * <p>
 * <b>What it does not promise.</b> It does not validate that {@code fromStatus}/{@code toStatus}
 * form a coherent path, and it does not backfill or repair the gaps that predate this class
 * ({@code A-135-2AB}): the five rows already in {@code t_activity_approval_history}, all
 * {@code from_status = NEW}, stay exactly as they are. It also does not invent an actor:
 * {@code actorId} comes from {@link SecurityContextHelper#getCurrentUserId()} and is {@code null}
 * whenever that call does not resolve to a {@link UUID} -- which, in every environment this
 * project runs today, is always the case, because {@code SECURITY_ENABLED=false}
 * ({@code A-132-121}, accepted systemic debt). Callers must not treat a {@code null}
 * {@code actorId} as a defect of this class.
 */
@Component
public class ActivityApprovalHistoryRecorder {

  private static final Logger LOGGER = LoggerFactory.getLogger(ActivityApprovalHistoryRecorder.class);

  private final TaticalActivityHistoryEntityRepository historyRepository;
  private final TacticalActivitiesEntityRepository entityRepository;
  private final SecurityContextHelper securityContextHelper;

  public ActivityApprovalHistoryRecorder(TaticalActivityHistoryEntityRepository historyRepository,
      TacticalActivitiesEntityRepository entityRepository,
      SecurityContextHelper securityContextHelper) {
    this.historyRepository = historyRepository;
    this.entityRepository = entityRepository;
    this.securityContextHelper = securityContextHelper;
  }

  /**
   * Writes one row to {@code t_activity_approval_history} for a transition that already
   * happened -- callers must invoke this only after the {@code save} of the transitioned
   * activity succeeded, and inside the same {@code @Transactional} boundary, never before and
   * never outside it. Calling it before the {@code save} would record a transition that could
   * still fail; calling it outside the transaction would let the history row survive a rollback
   * of the transition it describes.
   * <p>
   * {@code fromStatus} is the caller's own record of the status immediately before the
   * transition -- it is never re-read from {@code activityId}, because by the time this method
   * runs the persisted status is already the new one.
   * <p>
   * When {@code activityId} does not resolve to a {@link TacticalActivitiesEntity}, no row is
   * written -- this mirrors the two handlers this class replaces -- but, unlike them, the gap is
   * now logged at {@code warn}, because a silent gap is exactly how {@code A-135-2AB} went
   * unnoticed for as long as it did.
   */
  public void record(TacticalActivityId activityId, String fromStatus, String toStatus, String action,
      String comment) {
    UUID rawId = activityId.getValor().getValor();
    TacticalActivitiesEntity actEntity = entityRepository.findById(rawId).orElse(null);
    if (actEntity == null) {
      LOGGER.warn("Não foi possível registar histórico de aprovação: atividade {} não encontrada "
          + "para a transição {} -> {} (ação {})", rawId, fromStatus, toStatus, action);
      return;
    }

    TaticalActivityHistoryEntity history = new TaticalActivityHistoryEntity();
    history.setId(UUID.randomUUID());
    history.setInstitutionId(actEntity.getInstitutionId());
    history.setActivityId(actEntity);
    history.setAction(action);
    try {
      history.setActorId(UUID.fromString(securityContextHelper.getCurrentUserId()));
    } catch (Exception e) {
      history.setActorId(null);
    }
    history.setFromStatus(fromStatus);
    history.setToStatus(toStatus);
    history.setComment(comment);

    historyRepository.save(history);
  }
}
