package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.dto.BudgetInfoDTO;
import cv.igrp.RH_Service.sigdi.application.dto.TacticalActivityResponseDTO;
import cv.igrp.RH_Service.sigdi.application.port.EconomicClassifierPort;
import cv.igrp.RH_Service.sigdi.application.service.PaaActivityWindowPolicy;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.TacticalActivity;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.TacticalActivityRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.Budget;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.TacticalActivityId;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class AssignTacticalActivityBudgetCommandHandler
    implements CommandHandler<AssignTacticalActivityBudgetCommand, ResponseEntity<TacticalActivityResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AssignTacticalActivityBudgetCommandHandler.class);

  private final EconomicClassifierPort economicClassifierPort;
  private final TacticalActivityRepository activityRepository;
  private final PaaActivityWindowPolicy windowPolicy;

  public AssignTacticalActivityBudgetCommandHandler(EconomicClassifierPort economicClassifierPort,
      TacticalActivityRepository activityRepository, PaaActivityWindowPolicy windowPolicy) {
    this.economicClassifierPort = economicClassifierPort;
    this.activityRepository = activityRepository;
    this.windowPolicy = windowPolicy;
  }

  @IgrpCommandHandler
  public ResponseEntity<TacticalActivityResponseDTO> handle(AssignTacticalActivityBudgetCommand command) {
    LOGGER.debug("AssignTacticalActivityBudgetCommand : {}", command);

    var request = command.getAssignBudget();
    
    TacticalActivity activity = activityRepository.findById(TacticalActivityId.from(request.getActivityId()))
        .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.NOT_FOUND, "Atividade não encontrada"));

    // A-132-110 / COR-01 (Phase 136, D-47 reverts T-139's 2026-09-05 exclusion of this handler).
    // Checked before the EconomicClassifierPort call below on purpose, not after: the deadline
    // refusal is the cheapest and most structural one, and refusing here avoids an external call
    // for an act that could never have gone through anyway. paaLevel comes from the loaded
    // activity, never from the request (D-27, Phase 134).
    windowPolicy.requireOpenFor(activity.getPaaLevel());

    // Validate budget availability
    BudgetInfoDTO budgetInfo = economicClassifierPort.getBudget(request.getEconomicClassifier());

    if (request.getBudgetEstimated().compareTo(budgetInfo.availableBudget()) > 0) {
      throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
          "Budget limit exceeded for this classifier. Available: " + budgetInfo.availableBudget());
    }

    Budget budget = Budget.of(request.getBudgetEstimated(), request.getEconomicClassifier());
    
    TacticalActivity updated = activity.assignBudget(budget);
    TacticalActivity saved = activityRepository.save(updated);

    TacticalActivityResponseDTO response = new TacticalActivityResponseDTO();
    response.setId(saved.getId().getValor().getValor());
    response.setStrategicGoalId(saved.getStrategicGoalId().getValor().getValor());
    response.setOrganicUnitId(saved.getOrganicUnitId());
    response.setTitle(saved.getTitle());
    response.setBudgetEstimated(saved.getBudget().getEstimatedAmount());
    response.setEconomicClassifier(saved.getBudget().getClassifier().getCode());
    response.setStatus(saved.getStatus().getCode());
    response.setStatusDesc(saved.getStatus().getDescription());

    return ResponseEntity.ok(response);
  }
}
