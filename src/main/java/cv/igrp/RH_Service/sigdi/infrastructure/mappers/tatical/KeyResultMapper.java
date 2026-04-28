package cv.igrp.RH_Service.sigdi.infrastructure.mappers.tatical;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.KeyResultsEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.KeyResultsCheckinEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.TacticalActivitiesEntity;
import cv.igrp.RH_Service.sigdi.application.constants.KeyResultMetricUnit;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.KeyResult;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.KeyResultCheckin;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.KeyResultId;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.TacticalActivityId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class KeyResultMapper {

  private final KeyResultCheckinMapper checkinMapper;

  public KeyResult toDomain(KeyResultsEntity entity) {
    if (entity == null) return null;

    TacticalActivityId activityId = entity.getActivityId() != null
        ? TacticalActivityId.from(entity.getActivityId().getId()) : null;

    return KeyResult.reconstruct(
        KeyResultId.from(entity.getId()),
        entity.getInstitutionId(),
        activityId,
        entity.getTitle(),
        entity.getTargetValue(),
        entity.getCurrentValue(),
        KeyResultMetricUnit.fromCodeOrThrow(entity.getMetricUnit()),
        new ArrayList<KeyResultCheckin>(),
        entity.getCriteriaSuperado(),
        entity.getCriteriaSeguranca(),
        entity.getCriteriaAlcancado(),
        entity.getCriteriaInsuficiente()
    );
  }

  public KeyResult toDomainFull(KeyResultsEntity entity) {
    if (entity == null) return null;

    List<KeyResultCheckin> checkins = entity.getKeyResultCheckins().stream()
        .map(checkinMapper::toDomain)
        .collect(Collectors.toList());

    TacticalActivityId activityId = entity.getActivityId() != null
        ? TacticalActivityId.from(entity.getActivityId().getId()) : null;

    return KeyResult.reconstruct(
        KeyResultId.from(entity.getId()),
        entity.getInstitutionId(),
        activityId,
        entity.getTitle(),
        entity.getTargetValue(),
        entity.getCurrentValue(),
        KeyResultMetricUnit.fromCodeOrThrow(entity.getMetricUnit()),
        checkins,
        entity.getCriteriaSuperado(),
        entity.getCriteriaSeguranca(),
        entity.getCriteriaAlcancado(),
        entity.getCriteriaInsuficiente());
  }

  public KeyResultsEntity toEntity(KeyResult domain) {
    if (domain == null) return null;

    KeyResultsEntity entity = new KeyResultsEntity();
    entity.setId(domain.getId().getValor().getValor());
    entity.setInstitutionId(domain.getInstitutionId());
    entity.setTitle(domain.getTitle());
    entity.setTargetValue(domain.getTargetValue());
    entity.setCurrentValue(domain.getCurrentValue());
    entity.setMetricUnit(domain.getMetricUnit().getCode());
    entity.setCriteriaSuperado(domain.getCriteriaSuperado());
    entity.setCriteriaSeguranca(domain.getCriteriaSeguranca());
    entity.setCriteriaAlcancado(domain.getCriteriaAlcancado());
    entity.setCriteriaInsuficiente(domain.getCriteriaInsuficiente());

    if (domain.getActivityId() != null) {
      TacticalActivitiesEntity activityRef = new TacticalActivitiesEntity();
      activityRef.setId(domain.getActivityId().getValor().getValor());
      entity.setActivityId(activityRef);
    }

    List<KeyResultsCheckinEntity> checkins = domain.getCheckins().stream()
        .map(checkinMapper::toEntity)
        .collect(Collectors.toList());

    checkins.forEach(c -> c.setKeyResultId(entity));
    entity.setKeyResultCheckins(checkins);

    return entity;
  }
}
