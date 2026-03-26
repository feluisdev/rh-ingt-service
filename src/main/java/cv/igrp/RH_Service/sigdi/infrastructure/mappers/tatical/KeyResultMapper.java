package cv.igrp.RH_Service.sigdi.infrastructure.mappers.tatical;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.KeyResultsEntity;
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
    if (entity == null)
      return null;

    return KeyResult.reconstruct(
        KeyResultId.from(entity.getId()),
        TacticalActivityId.from(entity.getActivityId().getId()),
        entity.getTitle(),
        entity.getTargetValue(),
        entity.getCurrentValue(),
        KeyResultMetricUnit.fromCodeOrThrow(entity.getMetricUnit()),
        new ArrayList<KeyResultCheckin>() // checkins carregados via toDomainFull
    );
  }

  public KeyResult toDomainFull(KeyResultsEntity entity) {
    if (entity == null)
      return null;

    List<KeyResultCheckin> checkins = entity.getKeyResultCheckins().stream()
        .map(checkinMapper::toDomain)
        .collect(Collectors.toList());

    return KeyResult.reconstruct(
        KeyResultId.from(entity.getId()),
        TacticalActivityId.from(entity.getActivityId().getId()),
        entity.getTitle(),
        entity.getTargetValue(),
        entity.getCurrentValue(),
        KeyResultMetricUnit.fromCodeOrThrow(entity.getMetricUnit()),
        checkins);
  }

  public KeyResultsEntity toEntity(KeyResult domain) {
    if (domain == null)
      return null;

    KeyResultsEntity entity = new KeyResultsEntity();
    entity.setId(domain.getId().getValor().getValor());
    entity.setTitle(domain.getTitle());
    entity.setTargetValue(domain.getTargetValue());
    entity.setCurrentValue(domain.getCurrentValue());
    entity.setMetricUnit(domain.getMetricUnit().getCode());

    // Referência leve — só o ID
    TacticalActivitiesEntity activityRef = new TacticalActivitiesEntity();
    activityRef.setId(domain.getActivityId().getValor().getValor());
    entity.setActivityId(activityRef);

    return entity;

  }
}
