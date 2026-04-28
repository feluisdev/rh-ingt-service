package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.dto.ImpactedActivityDTO;
import cv.igrp.RH_Service.sigdi.application.dto.ScenarioResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.ScenarioSummaryDTO;
import cv.igrp.RH_Service.sigdi.domain.intelligence.models.SimulationResult;
import cv.igrp.RH_Service.sigdi.domain.intelligence.models.SimulationScenario;
import cv.igrp.RH_Service.sigdi.domain.intelligence.repository.SimulationResultRepository;
import cv.igrp.RH_Service.sigdi.domain.intelligence.repository.SimulationScenarioRepository;
import cv.igrp.RH_Service.sigdi.domain.intelligence.valueobject.SimulationScenarioId;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class GetScenarioQueryHandler
    implements QueryHandler<GetScenarioQuery, ResponseEntity<ScenarioResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GetScenarioQueryHandler.class);

  private final SimulationScenarioRepository scenarioRepository;
  private final SimulationResultRepository resultRepository;

  public GetScenarioQueryHandler(SimulationScenarioRepository scenarioRepository,
                                  SimulationResultRepository resultRepository) {
    this.scenarioRepository = scenarioRepository;
    this.resultRepository = resultRepository;
  }

  @IgrpQueryHandler
  public ResponseEntity<ScenarioResponseDTO> handle(GetScenarioQuery query) {
    LOGGER.debug("GetScenarioQuery: {}", query);

    SimulationScenarioId id = SimulationScenarioId.from(UUID.fromString(query.getScenarioId()));

    SimulationScenario scenario = scenarioRepository.findById(id)
        .orElseThrow(() -> IgrpResponseStatusException.notFound("Scenario not found: " + query.getScenarioId()));

    List<SimulationResult> results = resultRepository.findByScenarioId(id);

    List<ImpactedActivityDTO> impacted = new ArrayList<>();
    int viable = 0, atRisk = 0, infeasible = 0;
    BigDecimal totalSaving = BigDecimal.ZERO;

    for (SimulationResult r : results) {
      String viability = r.getRecommendation() == null ? "VIABLE"
          : switch (r.getRecommendation()) {
            case CANCEL -> "INFEASIBLE";
            case REDUCE_SCOPE -> "AT_RISK";
            default -> "VIABLE";
          };

      if ("INFEASIBLE".equals(viability)) infeasible++;
      else if ("AT_RISK".equals(viability)) atRisk++;
      else viable++;

      ImpactedActivityDTO dto = new ImpactedActivityDTO();
      dto.setActivityId(r.getActivityId().toString());
      dto.setViability(viability);
      dto.setSuggestedAction(r.getRecommendation() != null ? r.getRecommendation().getCode() : null);
      dto.setActionReason(r.getDetails());
      dto.setPriorityScore(r.getImpactScore());
      impacted.add(dto);
    }

    ScenarioSummaryDTO summary = new ScenarioSummaryDTO();
    summary.setTotalActivities(results.size());
    summary.setActivitiesViable(viable);
    summary.setActivitiesAtRisk(atRisk);
    summary.setActivitiesInfeasible(infeasible);
    summary.setTotalSaving(totalSaving);

    ScenarioResponseDTO response = new ScenarioResponseDTO();
    response.setScenarioId(scenario.getId().getStringValor());
    response.setName(scenario.getName());
    response.setType(scenario.getParameters().getType().getCode());
    response.setStatus(scenario.getStatus().getCode());
    response.setSummary(summary);
    response.setImpactedActivities(impacted);

    return ResponseEntity.ok(response);
  }
}
