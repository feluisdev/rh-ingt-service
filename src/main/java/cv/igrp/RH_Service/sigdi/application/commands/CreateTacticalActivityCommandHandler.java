package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.security.SecurityContextHelper;
import cv.igrp.RH_Service.sigdi.application.dto.BudgetInfoDTO;
import cv.igrp.RH_Service.sigdi.application.port.EconomicClassifierPort;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.StrategicGoalRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.TacticalActivity;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.TacticalActivityRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.Budget;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.DepartamentoEntityRepository;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.FuncionarioEntityRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.DateRange;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cv.igrp.RH_Service.sigdi.application.dto.TacticalActivityResponseDTO;

@Component
public class CreateTacticalActivityCommandHandler
    implements CommandHandler<CreateTacticalActivityCommand, ResponseEntity<TacticalActivityResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateTacticalActivityCommandHandler.class);

  private final EconomicClassifierPort economicClassifierPort;
  private final StrategicGoalRepository goalRepository;
  private final TacticalActivityRepository activityRepository;
  private final SecurityContextHelper securityContextHelper;
  private final DepartamentoEntityRepository departamentoRepository;
  private final FuncionarioEntityRepository funcionarioRepository;

  public CreateTacticalActivityCommandHandler(EconomicClassifierPort economicClassifierPort,
      StrategicGoalRepository goalRepository,
      TacticalActivityRepository activityRepository,
      SecurityContextHelper securityContextHelper,
      DepartamentoEntityRepository departamentoRepository,
      FuncionarioEntityRepository funcionarioRepository) {
    this.economicClassifierPort = economicClassifierPort;
    this.goalRepository = goalRepository;
    this.activityRepository = activityRepository;
    this.securityContextHelper = securityContextHelper;
    this.departamentoRepository = departamentoRepository;
    this.funcionarioRepository = funcionarioRepository;
  }

  @IgrpCommandHandler
  public ResponseEntity<TacticalActivityResponseDTO> handle(CreateTacticalActivityCommand command) {
    LOGGER.debug("CreateTacticalActivityCommand : {}", command);

    var request = command.getCreatetacticalactivity();
    StrategicGoalId strategicGoalId = StrategicGoalId.from(request.getStrategicGoalId());
    goalRepository.findById(strategicGoalId)
        .orElseThrow(() -> IgrpResponseStatusException.badRequest("strategicGoalId inválido"));

    if (!departamentoRepository.existsById(request.getOrganicUnitId())) {
      throw IgrpResponseStatusException.badRequest("organicUnitId (Departamento) inválido ou não encontrado");
    }

    if (!funcionarioRepository.existsById(request.getResponsibleWho())) {
      throw IgrpResponseStatusException.badRequest("responsibleWho (Funcionário) inválido ou não encontrado");
    }

    DateRange dateRange = DateRange.of(request.getStartDate(), request.getEndDate());
    
    Budget budget = null;
    if (request.getEconomicClassifier() != null && !request.getEconomicClassifier().isBlank() 
        && request.getBudgetEstimated() != null) {
      
      BudgetInfoDTO budgetInfo = economicClassifierPort.getBudget(request.getEconomicClassifier());

      if (request.getBudgetEstimated().compareTo(budgetInfo.availableBudget()) > 0) {
        throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
            "Budget limit exceeded for this classifier. Available: " + budgetInfo.availableBudget());
      }
      
      budget = Budget.of(request.getBudgetEstimated(), request.getEconomicClassifier());
    }

    TacticalActivity activity = TacticalActivity.create(
        securityContextHelper.getCurrentInstitutionId(),
        strategicGoalId,
        request.getOrganicUnitId(),
        request.getTitle(),
        request.getDescriptionWhat(),
        request.getJustificationWhy(),
        request.getLocationWhere(),
        request.getResponsibleWho(),
        request.getMethodologyHow(),
        dateRange,
        budget);

    TacticalActivity saved = activityRepository.save(activity);

    TacticalActivityResponseDTO response = new TacticalActivityResponseDTO();
    response.setId(saved.getId().getValor().getValor());
    response.setStrategicGoalId(saved.getStrategicGoalId().getValor().getValor());
    response.setOrganicUnitId(saved.getOrganicUnitId());
    response.setTitle(saved.getTitle());
    response.setDescriptionWhat(saved.getDescriptionWhat());
    response.setJustificationWhy(saved.getJustificationWhy());
    response.setLocationWhere(saved.getLocationWhere());
    response.setResponsibleWho(saved.getResponsibleWho());
    response.setMethodologyHow(saved.getMethodologyHow());
    response.setStartDate(saved.getDateRange().getStartDate());
    response.setEndDate(saved.getDateRange().getEndDate());
    
    if (saved.getBudget() != null) {
      response.setBudgetEstimated(saved.getBudget().getEstimatedAmount());
      response.setEconomicClassifier(saved.getBudget().getClassifier().getCode());
    }
    response.setVersion(saved.getVersion());
    response.setStatus(saved.getStatus().getCode());
    response.setStatusDesc(saved.getStatus().getDescription());
    response.setOrganicUnitName(saved.getOrganicUnitName());
    response.setResponsibleName(saved.getResponsibleName());

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}
