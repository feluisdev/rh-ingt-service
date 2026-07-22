package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.application.dto.BudgetInfoDTO;
import cv.igrp.RH_Service.sigdi.application.port.EconomicClassifierPort;
import cv.igrp.RH_Service.sigdi.application.port.FuncionarioLookupPort;
import cv.igrp.RH_Service.sigdi.application.port.OrganicaLookupPort;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.StrategicGoalRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.TacticalActivity;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PaaSubmissionPeriodRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.TacticalActivityRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.Budget;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.DateRange;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.TacticalActivityId;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import cv.igrp.RH_Service.sigdi.application.dto.TacticalActivityResponseDTO;

@Component
public class UpdateTacticalActivityCommandHandler
    implements CommandHandler<UpdateTacticalActivityCommand, ResponseEntity<TacticalActivityResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(UpdateTacticalActivityCommandHandler.class);

  private final EconomicClassifierPort economicClassifierPort;
  private final StrategicGoalRepository goalRepository;
  private final TacticalActivityRepository activityRepository;
  private final OrganicaLookupPort organicaLookupPort;
  private final FuncionarioLookupPort funcionarioLookupPort;
  private final PaaSubmissionPeriodRepository periodRepository;

  public UpdateTacticalActivityCommandHandler(EconomicClassifierPort economicClassifierPort,
      StrategicGoalRepository goalRepository,
      TacticalActivityRepository activityRepository,
      OrganicaLookupPort organicaLookupPort,
      FuncionarioLookupPort funcionarioLookupPort,
      PaaSubmissionPeriodRepository periodRepository) {
    this.economicClassifierPort = economicClassifierPort;
    this.goalRepository = goalRepository;
    this.activityRepository = activityRepository;
    this.organicaLookupPort = organicaLookupPort;
    this.funcionarioLookupPort = funcionarioLookupPort;
    this.periodRepository = periodRepository;
  }

  @IgrpCommandHandler
  public ResponseEntity<TacticalActivityResponseDTO> handle(UpdateTacticalActivityCommand command) {
    LOGGER.debug("UpdateTacticalActivityCommand : {}", command);

    TacticalActivityId activityId = TacticalActivityId.from(command.getId());
    TacticalActivity activity = activityRepository.findById(activityId)
        .orElseThrow(() -> IgrpResponseStatusException.notFound("Atividade não encontrada"));

    // PRAZO-03: fail-closed deadline enforcement — sourced from the loaded entity's
    // paaLevel (never the request DTO, since update() does not accept/change it).
    periodRepository.findActiveByTypeAndYearAndPurpose(activity.getPaaLevel(), java.time.Year.now().getValue(), Purpose.PAA)
        .orElseThrow(() -> IgrpResponseStatusException.badRequest(
            "Prazo não configurado para a submissão de atividades do PAA"));

    var request = command.getTacticalActivity();

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

    TacticalActivity updatedActivity = activity.update(
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

    TacticalActivity saved = activityRepository.save(updatedActivity);

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

    if (saved.getOrganicUnitId() != null) {
      organicaLookupPort.findById(saved.getOrganicUnitId())
          .ifPresent(o -> response.setOrganicUnitName(o.getName()));
    }
    if (saved.getResponsibleWho() != null) {
      funcionarioLookupPort.findById(saved.getResponsibleWho())
          .ifPresent(f -> response.setResponsibleName(f.getNomeCompleto()));
    }

    return ResponseEntity.ok(response);
  }
}
