package cv.igrp.RH_Service.sigdi.infrastructure.mappers.strategy;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.StrategicGoalEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.StrategyMapLinkEntity;
import cv.igrp.RH_Service.sigdi.application.constants.StrategyMapRelationshipType;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.StrategyMapLink;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategyMapLinkId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StrategyMapLinkMapper {

  public StrategyMapLink toDomain(StrategyMapLinkEntity entity) {
    if (entity == null) return null;

    return StrategyMapLink.reconstruct(
        StrategyMapLinkId.from(entity.getId()),
        entity.getInstitutionId(),
        StrategicGoalId.from(entity.getSourceGoalId().getId()),
        StrategicGoalId.from(entity.getTargetGoalId().getId()),
        StrategyMapRelationshipType.fromCodeOrThrow(entity.getRelationshipType())
    );
  }

  public StrategyMapLinkEntity toEntity(StrategyMapLink domain) {
    if (domain == null) return null;

    StrategyMapLinkEntity entity = new StrategyMapLinkEntity();
    entity.setId(domain.getId().getValor().getValor());
    entity.setInstitutionId(domain.getInstitutionId());
    entity.setRelationshipType(domain.getRelationshipType().getCode());

    StrategicGoalEntity sourceRef = new StrategicGoalEntity();
    sourceRef.setId(domain.getSourceGoalId().getValor().getValor());
    entity.setSourceGoalId(sourceRef);

    StrategicGoalEntity targetRef = new StrategicGoalEntity();
    targetRef.setId(domain.getTargetGoalId().getValor().getValor());
    entity.setTargetGoalId(targetRef);

    return entity;
  }
}
