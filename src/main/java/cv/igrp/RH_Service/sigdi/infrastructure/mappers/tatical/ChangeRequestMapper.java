package cv.igrp.RH_Service.sigdi.infrastructure.mappers.tatical;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.ChangeRequestEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.TacticalActivitiesEntity;
import cv.igrp.RH_Service.sigdi.application.constants.ChangeRequestStatus;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.ChangeRequest;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.ChangeRequestId;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.TacticalActivityId;
import org.springframework.stereotype.Component;

@Component
public class ChangeRequestMapper {

  public ChangeRequest toDomain(ChangeRequestEntity entity) {
    if (entity == null) return null;

    return ChangeRequest.reconstruct(
        ChangeRequestId.from(entity.getId()),
        entity.getInstitutionId(),
        TacticalActivityId.from(entity.getActivityId().getId()),
        entity.getFieldName(),
        entity.getCurrentValue(),
        entity.getProposedValue(),
        entity.getJustification(),
        ChangeRequestStatus.fromCodeOrThrow(entity.getStatus()),
        entity.getReviewerId(),
        entity.getReviewerComment()
    );
  }

  public ChangeRequestEntity toEntity(ChangeRequest domain) {
    if (domain == null) return null;

    ChangeRequestEntity entity = new ChangeRequestEntity();
    entity.setId(domain.getId().getValor().getValor());
    entity.setInstitutionId(domain.getInstitutionId());
    entity.setFieldName(domain.getFieldName());
    entity.setCurrentValue(domain.getCurrentValue());
    entity.setProposedValue(domain.getProposedValue());
    entity.setJustification(domain.getJustification());
    entity.setStatus(domain.getStatus().getCode());
    entity.setReviewerId(domain.getReviewerId());
    entity.setReviewerComment(domain.getReviewerComment());

    TacticalActivitiesEntity activityRef = new TacticalActivitiesEntity();
    activityRef.setId(domain.getActivityId().getValor().getValor());
    entity.setActivityId(activityRef);

    return entity;
  }
}
