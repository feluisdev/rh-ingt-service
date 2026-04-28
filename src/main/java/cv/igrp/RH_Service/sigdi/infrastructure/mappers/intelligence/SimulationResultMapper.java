package cv.igrp.RH_Service.sigdi.infrastructure.mappers.intelligence;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.SimulationResultsEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.SimulationScenariosEntity;
import cv.igrp.RH_Service.sigdi.application.constants.SimulationRecommendation;
import cv.igrp.RH_Service.sigdi.domain.intelligence.models.SimulationResult;
import cv.igrp.RH_Service.sigdi.domain.intelligence.valueobject.SimulationResultId;
import cv.igrp.RH_Service.sigdi.domain.intelligence.valueobject.SimulationScenarioId;
import org.springframework.stereotype.Component;

@Component
public class SimulationResultMapper {

  public SimulationResult toDomain(SimulationResultsEntity entity) {
    if (entity == null) return null;

    return SimulationResult.reconstruct(
        SimulationResultId.from(entity.getId()),
        SimulationScenarioId.from(entity.getScenarioId().getId()),
        entity.getActivityId(),
        entity.getRecommendation() != null ? SimulationRecommendation.fromCodeOrThrow(entity.getRecommendation()) : null,
        entity.getImpactScore(),
        entity.getDetails()
    );
  }

  public SimulationResultsEntity toEntity(SimulationResult domain) {
    if (domain == null) return null;

    SimulationResultsEntity entity = new SimulationResultsEntity();
    entity.setId(domain.getId().getValor().getValor());
    entity.setActivityId(domain.getActivityId());
    entity.setRecommendation(domain.getRecommendation() != null ? domain.getRecommendation().getCode() : null);
    entity.setImpactScore(domain.getImpactScore());
    entity.setDetails(domain.getDetails());

    SimulationScenariosEntity scenarioRef = new SimulationScenariosEntity();
    scenarioRef.setId(domain.getScenarioId().getValor().getValor());
    entity.setScenarioId(scenarioRef);

    return entity;
  }
}

