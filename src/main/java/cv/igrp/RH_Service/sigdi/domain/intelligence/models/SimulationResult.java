package cv.igrp.RH_Service.sigdi.domain.intelligence.models;

import cv.igrp.RH_Service.sigdi.application.constants.SimulationRecommendation;
import cv.igrp.RH_Service.sigdi.domain.intelligence.valueobject.SimulationResultId;
import cv.igrp.RH_Service.sigdi.domain.intelligence.valueobject.SimulationScenarioId;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class SimulationResult {

  private final SimulationResultId id;
  private final SimulationScenarioId scenarioId;
  private final UUID activityId;
  private final SimulationRecommendation recommendation;
  private final BigDecimal impactScore;
  private final String details;

  private SimulationResult(SimulationResultId id, SimulationScenarioId scenarioId,
                           UUID activityId, SimulationRecommendation recommendation,
                           BigDecimal impactScore, String details) {
    if (id == null) throw new IllegalArgumentException("id é obrigatório");
    if (scenarioId == null) throw new IllegalArgumentException("scenarioId é obrigatório");
    if (activityId == null) throw new IllegalArgumentException("activityId é obrigatório");
    this.id = id;
    this.scenarioId = scenarioId;
    this.activityId = activityId;
    this.recommendation = recommendation;
    this.impactScore = impactScore;
    this.details = details;
  }

  public static SimulationResult create(SimulationScenarioId scenarioId, UUID activityId,
                                        SimulationRecommendation recommendation,
                                        BigDecimal impactScore, String details) {
    return new SimulationResult(SimulationResultId.gerarNovo(), scenarioId, activityId,
        recommendation, impactScore, details);
  }

  public static SimulationResult reconstruct(SimulationResultId id, SimulationScenarioId scenarioId,
                                             UUID activityId, SimulationRecommendation recommendation,
                                             BigDecimal impactScore, String details) {
    return new SimulationResult(id, scenarioId, activityId, recommendation, impactScore, details);
  }
}

