package cv.igrp.RH_Service.sigdi.domain.tatical.models;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.ChangeRequestStatus;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.ChangeRequestId;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.TacticalActivityId;
import lombok.Getter;

import java.util.UUID;

@Getter
public class ChangeRequest {

  private final ChangeRequestId id;
  private final TacticalActivityId activityId;
  private final String fieldName;
  private final String currentValue;
  private final String proposedValue;
  private final String justification;
  private final ChangeRequestStatus status;
  private final UUID reviewerId;
  private final String reviewerComment;

  private ChangeRequest(ChangeRequestId id, TacticalActivityId activityId, String fieldName,
                        String currentValue, String proposedValue, String justification,
                        ChangeRequestStatus status, UUID reviewerId, String reviewerComment) {
    if (activityId == null) throw new IllegalArgumentException("activityId é obrigatório");
    if (fieldName == null || fieldName.isBlank()) throw new IllegalArgumentException("fieldName é obrigatório");
    if (justification == null || justification.isBlank()) throw new IllegalArgumentException("justification é obrigatória");

    this.id = id;
    this.activityId = activityId;
    this.fieldName = fieldName;
    this.currentValue = currentValue;
    this.proposedValue = proposedValue;
    this.justification = justification;
    this.status = (status != null) ? status : ChangeRequestStatus.PENDING;
    this.reviewerId = reviewerId;
    this.reviewerComment = reviewerComment;
  }

  public static ChangeRequest create(TacticalActivityId activityId, String fieldName,
                                     String currentValue, String proposedValue, String justification) {
    return new ChangeRequest(ChangeRequestId.gerarNovo(), activityId, fieldName,
        currentValue, proposedValue, justification, ChangeRequestStatus.PENDING, null, null);
  }

  public static ChangeRequest reconstruct(ChangeRequestId id, TacticalActivityId activityId,
                                          String fieldName, String currentValue, String proposedValue,
                                          String justification, ChangeRequestStatus status,
                                          UUID reviewerId, String reviewerComment) {
    return new ChangeRequest(id, activityId, fieldName, currentValue, proposedValue,
        justification, status, reviewerId, reviewerComment);
  }

  public ChangeRequest approve(String comment) {
    if (!ChangeRequestStatus.PENDING.equals(this.status))
      throw IgrpResponseStatusException.badRequest("Change Request não está pendente");
    return new ChangeRequest(this.id, this.activityId, this.fieldName, this.currentValue,
        this.proposedValue, this.justification, ChangeRequestStatus.APPROVED, null, comment);
  }

  public ChangeRequest reject(String comment) {
    if (!ChangeRequestStatus.PENDING.equals(this.status))
      throw IgrpResponseStatusException.badRequest("Change Request não está pendente");
    if (comment == null || comment.isBlank())
      throw IgrpResponseStatusException.badRequest("Comentário é obrigatório para rejeitar");
    return new ChangeRequest(this.id, this.activityId, this.fieldName, this.currentValue,
        this.proposedValue, this.justification, ChangeRequestStatus.REJECTED, null, comment);
  }

  public boolean isPending() {
    return ChangeRequestStatus.PENDING.equals(this.status);
  }
}
