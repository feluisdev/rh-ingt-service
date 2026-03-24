package cv.igrp.RH_Service.sigdi.infrastructure.mappers;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.StrategicGoalEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.StrategyMapLinkEntity;
import cv.igrp.RH_Service.sigdi.application.constants.StrategyMapRelationshipType;
import cv.igrp.RH_Service.sigdi.domain.models.StrategicGoal;
import cv.igrp.RH_Service.sigdi.domain.models.StrategyMapLink;
import cv.igrp.RH_Service.sigdi.domain.valueobject.StrategyMapLinkId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StrategyMapLinkMapper {


  public StrategyMapLink toDomain(StrategyMapLinkEntity entity,
                                  StrategicGoal sourceGoal,
                                  StrategicGoal targetGoal) {
    if (entity == null) return null;

    return StrategyMapLink.reconstruct(
        StrategyMapLinkId.from(entity.getId()),
        sourceGoal,
        targetGoal,
        StrategyMapRelationshipType.fromCodeOrThrow(entity.getRelationshipType())
    );
  }

  public StrategyMapLinkEntity toEntity(StrategyMapLink domain) {
    if (domain == null) return null;

    StrategyMapLinkEntity entity = new StrategyMapLinkEntity();
    entity.setId(domain.getId().getValor().getValor());
    entity.setRelationshipType(domain.getRelationshipType().getCode());

    StrategicGoalEntity sourceRef = new StrategicGoalEntity();
    sourceRef.setId(domain.getSourceGoalId().getId().getValor().getValor());
    entity.setSourceGoalId(sourceRef);

    StrategicGoalEntity targetRef = new StrategicGoalEntity();
    targetRef.setId(domain.getTargetGoalId().getId().getValor().getValor());
    entity.setTargetGoalId(targetRef);

    return entity;
  }
}
