package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.security.SecurityContextHelper;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.dto.BudgetInfoDTO;
import cv.igrp.RH_Service.sigdi.application.port.EconomicClassifierPort;
import cv.igrp.RH_Service.sigdi.application.port.FuncionarioLookupPort;
import cv.igrp.RH_Service.sigdi.application.port.OrganicaLookupPort;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.StrategicGoalRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.TacticalActivity;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.TacticalActivityRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.Budget;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.DateRange;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.TacticalActivitiesEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.TaticalActivityHistoryEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.TacticalActivitiesEntityRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.TaticalActivityHistoryEntityRepository;
import java.util.UUID;
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
  private final OrganicaLookupPort organicaLookupPort;
  private final FuncionarioLookupPort funcionarioLookupPort;
  private final TaticalActivityHistoryEntityRepository historyRepository;
  private final TacticalActivitiesEntityRepository entityRepository;

  public CreateTacticalActivityCommandHandler(EconomicClassifierPort economicClassifierPort,
      StrategicGoalRepository goalRepository,
      TacticalActivityRepository activityRepository,
      SecurityContextHelper securityContextHelper,
      OrganicaLookupPort organicaLookupPort,
      FuncionarioLookupPort funcionarioLookupPort,
      TaticalActivityHistoryEntityRepository historyRepository,
      TacticalActivitiesEntityRepository entityRepository) {
    this.economicClassifierPort = economicClassifierPort;
    this.goalRepository = goalRepository;
    this.activityRepository = activityRepository;
    this.securityContextHelper = securityContextHelper;
    this.organicaLookupPort = organicaLookupPort;
    this.funcionarioLookupPort = funcionarioLookupPort;
    this.historyRepository = historyRepository;
    this.entityRepository = entityRepository;
  }

  @IgrpCommandHandler
  public ResponseEntity<TacticalActivityResponseDTO> handle(CreateTacticalActivityCommand command) {
    LOGGER.debug("CreateTacticalActivityCommand : {}", command);

    var request = command.getCreatetacticalactivity();

    StrategicGoalId strategicGoalId = StrategicGoalId.from(request.getStrategicGoalId());
    goalRepository.findById(strategicGoalId)
        .orElseThrow(() -> IgrpResponseStatusException.badRequest("strategicGoalId inválido"));

    if (request.getOrganicUnitId() != null) {
      organicaLookupPort.findById(request.getOrganicUnitId())
          .orElseThrow(() -> IgrpResponseStatusException.badRequest(
              "organicUnitId inválido ou não encontrado: " + request.getOrganicUnitId()));
    }

    if (request.getResponsibleWho() != null) {
      funcionarioLookupPort.findById(request.getResponsibleWho())
          .orElseThrow(() -> IgrpResponseStatusException.badRequest(
              "responsibleWho inválido ou não encontrado: " + request.getResponsibleWho()));
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

    // Resolve PaaLevel
    PaaLevel paaLevel = (request.getPaaLevel() != null && !request.getPaaLevel().isBlank())
        ? PaaLevel.fromCodeOrThrow(request.getPaaLevel())
        : PaaLevel.UNIT_LEVEL;

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
        budget,
        paaLevel);

    TacticalActivity saved = activityRepository.save(activity);

    // Save initial history
    TacticalActivitiesEntity actEntity = entityRepository.findById(saved.getId().getValor().getValor()).orElse(null);
    if (actEntity != null) {
      TaticalActivityHistoryEntity history = new TaticalActivityHistoryEntity();
      history.setInstitutionId(actEntity.getInstitutionId());
      history.setActivityId(actEntity);
      history.setAction(saved.getStatus().getCode());
      try {
        history.setActorId(UUID.fromString(securityContextHelper.getCurrentUserId()));
      } catch (Exception e) {
        history.setActorId(null);
      }
      history.setFromStatus("NEW");
      history.setToStatus(saved.getStatus().getCode());
      historyRepository.save(history);
    }

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
    response.setPaaLevel(saved.getPaaLevel() != null ? saved.getPaaLevel().getCode() : PaaLevel.UNIT_LEVEL.getCode());
    response.setPaaLevelDesc(saved.getPaaLevel() != null ? saved.getPaaLevel().getDescription() : PaaLevel.UNIT_LEVEL.getDescription());
    if (saved.getAcceptanceStatus() != null) {
      response.setAcceptanceStatus(saved.getAcceptanceStatus().getCode());
      response.setAcceptanceStatusDesc(saved.getAcceptanceStatus().getDescription());
    }

    if (saved.getOrganicUnitId() != null) {
      organicaLookupPort.findById(saved.getOrganicUnitId())
          .ifPresent(o -> response.setOrganicUnitName(o.getName()));
    }
    if (saved.getResponsibleWho() != null) {
      funcionarioLookupPort.findById(saved.getResponsibleWho())
          .ifPresent(f -> response.setResponsibleName(f.getNomeCompleto()));
    }

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}
