package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.TacticalActivitiesEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.TacticalActivitiesEntityRepository;
import cv.igrp.RH_Service.sigdi.application.constants.SimulationRecommendation;
import cv.igrp.RH_Service.sigdi.application.constants.SimulationScenarioScope;
import cv.igrp.RH_Service.sigdi.application.constants.SimulationScenarioType;
import cv.igrp.RH_Service.sigdi.application.dto.AsyncScenarioResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.CreateScenarioRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.ImpactedActivityDTO;
import cv.igrp.RH_Service.sigdi.application.dto.ScenarioResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.ScenarioSummaryDTO;
import cv.igrp.RH_Service.sigdi.domain.intelligence.models.SimulationResult;
import cv.igrp.RH_Service.sigdi.domain.intelligence.models.SimulationScenario;
import cv.igrp.RH_Service.sigdi.domain.intelligence.repository.SimulationResultRepository;
import cv.igrp.RH_Service.sigdi.domain.intelligence.repository.SimulationScenarioRepository;
import cv.igrp.RH_Service.sigdi.domain.intelligence.valueobject.SimulationScenarioParameters;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class CreateScenarioCommandHandler
    implements CommandHandler<CreateScenarioCommand, ResponseEntity<?>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateScenarioCommandHandler.class);
  private static final int ASYNC_THRESHOLD = 100;

  private final SimulationScenarioRepository scenarioRepository;
  private final SimulationResultRepository resultRepository;
  private final TacticalActivitiesEntityRepository activitiesRepository;

  public CreateScenarioCommandHandler(SimulationScenarioRepository scenarioRepository,
                                      SimulationResultRepository resultRepository,
                                      TacticalActivitiesEntityRepository activitiesRepository) {
    this.scenarioRepository = scenarioRepository;
    this.resultRepository = resultRepository;
    this.activitiesRepository = activitiesRepository;
  }

  @IgrpCommandHandler
  @Transactional
  public ResponseEntity<?> handle(CreateScenarioCommand command) {
    LOGGER.debug("CreateScenarioCommand: {}", command);

    long startMs = System.currentTimeMillis();
    CreateScenarioRequestDTO req = command.getBody();

    SimulationScenarioType type = SimulationScenarioType.fromCodeOrThrow(req.getType());
    SimulationScenarioScope scope = resolveScope(req.getScope());

    // CR-01 fix: guard here -- before SimulationScenarioParameters.of() below -- rather than
    // relying solely on loadActivities()'s own guard, because SimulationScenarioParameters's
    // constructor ALSO validates "departmentId required when scope=DEPARTMENT_ID" and throws its
    // own raw, uncaught IllegalArgumentException for a null/blank targetId. That internal check
    // fires before loadActivities() is ever reached, so it must be intercepted here to surface
    // the intended structured 400 instead of letting it propagate unhandled.
    if (SimulationScenarioScope.DEPARTMENT_ID.equals(scope)
        && (req.getTargetId() == null || req.getTargetId().isBlank())) {
      throw IgrpResponseStatusException.badRequest("targetId is required when scope is not 'GLOBAL'");
    }

    // NOTE (known-remaining gap, ERRO-03 Open Question #2): SimulationScenarioScope only
    // distinguishes GLOBAL/DEPARTMENT_ID (2 values), while the frontend offers 3 scopes
    // (GLOBAL/UNIT_SPECIFIC/GOAL_SPECIFIC), and loadActivities() below only queries by
    // organicUnitId (findAllByFiscalYearAndOrganicUnitId) -- there is no goal-scoped finder.
    // A UNIT_SPECIFIC targetId (organic-unit UUID) resolves correctly. A GOAL_SPECIFIC scope is
    // rejected by resolveScope() as an invalid scope (400 "Invalid scope: GOAL_SPECIFIC") rather
    // than silently treated as DEPARTMENT_ID -- this fails loudly instead of returning wrong
    // data, but does not add goal-scoped simulation support. Adding a goal-scoped finder plus a
    // third SimulationScenarioScope value is a new feature, deliberately out of scope here.
    SimulationScenarioParameters parameters = SimulationScenarioParameters.of(
        type, req.getPercentage(), scope,
        SimulationScenarioScope.DEPARTMENT_ID.equals(scope) ? req.getTargetId() : null,
        req.getPriorityCriteria());

    SimulationScenario scenario = SimulationScenario.create(req.getName(), parameters);
    SimulationScenario saved = scenarioRepository.save(scenario);

    // Load activities for the fiscal year
    List<TacticalActivitiesEntity> activities = loadActivities(req.getFiscalYear(), scope, req.getTargetId());

    if (activities.size() >= ASYNC_THRESHOLD) {
      // Return async response — processing continues in background (stub)
      AsyncScenarioResponseDTO response = new AsyncScenarioResponseDTO();
      response.setScenarioId(saved.getId().getStringValor());
      response.setStatus("PROCESSING");
      response.setEstimatedCompletionSeconds(45);
      response.setPollUrl("/api/v1/intelligence/scenarios/" + saved.getId().getStringValor());
      return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    // Synchronous simulation
    List<SimulationResult> results = simulate(saved, activities, req.getPercentage(), req.getExcludeObligatory());

    // Save results
    for (SimulationResult result : results) {
      resultRepository.save(result);
    }

    // Mark scenario as completed
    SimulationScenario completed = saved.complete();
    scenarioRepository.save(completed);

    // Build summary
    int viable = 0, atRisk = 0, infeasible = 0;
    BigDecimal totalBefore = BigDecimal.ZERO;
    BigDecimal totalAfter = BigDecimal.ZERO;
    List<ImpactedActivityDTO> impacted = new ArrayList<>();

    for (int i = 0; i < activities.size(); i++) {
      TacticalActivitiesEntity act = activities.get(i);
      SimulationResult res = results.get(i);

      BigDecimal currentBudget = act.getBudgetEstimated() != null ? act.getBudgetEstimated() : BigDecimal.ZERO;
      BigDecimal reduction = currentBudget.multiply(req.getPercentage()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
      BigDecimal simulated = currentBudget.subtract(reduction);

      totalBefore = totalBefore.add(currentBudget);
      totalAfter = totalAfter.add(simulated.max(BigDecimal.ZERO));

      String viability = res.getRecommendation() == SimulationRecommendation.CANCEL ? "INFEASIBLE"
          : res.getRecommendation() == SimulationRecommendation.REDUCE_SCOPE ? "AT_RISK"
          : "VIABLE";

      if ("INFEASIBLE".equals(viability)) infeasible++;
      else if ("AT_RISK".equals(viability)) atRisk++;
      else viable++;

      ImpactedActivityDTO dto = new ImpactedActivityDTO();
      dto.setActivityId(act.getId().toString());
      dto.setActivityTitle(act.getTitle());
      dto.setOrganicUnitName(act.getOrganicUnitId() != null ? act.getOrganicUnitId().toString() : null);
      dto.setStrategicGoalTitle(act.getStrategicGoalId() != null ? act.getStrategicGoalId().toString() : null);
      dto.setCurrentBudget(currentBudget);
      dto.setSimulatedBudget(simulated.max(BigDecimal.ZERO));
      dto.setReduction(reduction);
      dto.setViability(viability);
      dto.setSuggestedAction(res.getRecommendation() != null ? res.getRecommendation().getCode() : null);
      dto.setActionReason(res.getDetails());
      dto.setPriorityScore(res.getImpactScore());
      impacted.add(dto);
    }

    ScenarioSummaryDTO summary = new ScenarioSummaryDTO();
    summary.setTotalActivities(activities.size());
    summary.setActivitiesViable(viable);
    summary.setActivitiesAtRisk(atRisk);
    summary.setActivitiesInfeasible(infeasible);
    summary.setTotalBudgetBefore(totalBefore);
    summary.setTotalBudgetAfter(totalAfter);
    summary.setTotalSaving(totalBefore.subtract(totalAfter));

    ScenarioResponseDTO response = new ScenarioResponseDTO();
    response.setScenarioId(completed.getId().getStringValor());
    response.setName(completed.getName());
    response.setType(type.getCode());
    response.setStatus(completed.getStatus().getCode());
    response.setSummary(summary);
    response.setImpactedActivities(impacted);
    response.setCreatedAt(LocalDateTime.now().toString());
    response.setProcessingTimeMs(System.currentTimeMillis() - startMs);

    return ResponseEntity.ok(response);
  }

  private SimulationScenarioScope resolveScope(String scope) {
    if ("GLOBAL".equalsIgnoreCase(scope)) {
      return SimulationScenarioScope.GLOBAL;
    }
    if ("UNIT_SPECIFIC".equalsIgnoreCase(scope) || "DEPARTMENT_ID".equalsIgnoreCase(scope)) {
      return SimulationScenarioScope.DEPARTMENT_ID;
    }
    throw IgrpResponseStatusException.badRequest("Invalid scope: " + scope);
  }

  private List<TacticalActivitiesEntity> loadActivities(Integer fiscalYear,
                                                         SimulationScenarioScope scope,
                                                         String scopeValue) {
    if (SimulationScenarioScope.DEPARTMENT_ID.equals(scope)) {
      if (scopeValue == null || scopeValue.isBlank()) {
        // Defensive backstop: handle() already rejects a null/blank targetId earlier (before
        // this method is reached), but this guard stays local to the UUID.fromString() call it
        // directly protects, per CR-01's originally-requested fix location.
        throw IgrpResponseStatusException.badRequest("targetId is required when scope is not 'GLOBAL'");
      }
      try {
        UUID organicUnitId = UUID.fromString(scopeValue);
        return activitiesRepository.findAllByFiscalYearAndOrganicUnitId(fiscalYear, organicUnitId);
      } catch (IllegalArgumentException e) {
        throw IgrpResponseStatusException.badRequest("scope must be 'GLOBAL' or a valid UUID for organicUnitId");
      }
    }
    return activitiesRepository.findAllByFiscalYear(fiscalYear);
  }

  private List<SimulationResult> simulate(SimulationScenario scenario,
                                           List<TacticalActivitiesEntity> activities,
                                           BigDecimal cutPercentage,
                                           Boolean excludeObligatory) {
    List<SimulationResult> results = new ArrayList<>();
    for (TacticalActivitiesEntity act : activities) {
      BigDecimal estimated = act.getBudgetEstimated() != null ? act.getBudgetEstimated() : BigDecimal.ZERO;
      BigDecimal committed = act.getBudgetCommitted() != null ? act.getBudgetCommitted() : BigDecimal.ZERO;

      BigDecimal reduction = estimated.multiply(cutPercentage).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
      BigDecimal simulated = estimated.subtract(reduction);

      SimulationRecommendation recommendation;
      String details;
      BigDecimal impactScore;

      if (simulated.compareTo(BigDecimal.ZERO) <= 0) {
        recommendation = SimulationRecommendation.CANCEL;
        details = "Orçamento insuficiente para execução mínima viável após corte de " + cutPercentage + "%.";
        impactScore = new BigDecimal("10.0");
      } else if (simulated.compareTo(committed) < 0) {
        recommendation = SimulationRecommendation.REDUCE_SCOPE;
        details = "Orçamento simulado (" + simulated + ") inferior ao comprometido (" + committed + "). Redução de âmbito necessária.";
        impactScore = committed.compareTo(BigDecimal.ZERO) > 0
            ? committed.subtract(simulated).divide(committed, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("10"))
            : new BigDecimal("5.0");
      } else {
        recommendation = null;
        details = "Atividade mantém viabilidade após corte.";
        impactScore = reduction.compareTo(BigDecimal.ZERO) > 0 && estimated.compareTo(BigDecimal.ZERO) > 0
            ? reduction.divide(estimated, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("10"))
            : BigDecimal.ZERO;
      }

      results.add(SimulationResult.create(scenario.getId(), act.getId(), recommendation, impactScore, details));
    }
    return results;
  }
}
