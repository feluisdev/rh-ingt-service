package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.dto.BudgetInfoDTO;
import cv.igrp.RH_Service.sigdi.application.port.EconomicClassifierPort;
import cv.igrp.RH_Service.sigdi.application.port.FuncionarioLookupPort;
import cv.igrp.RH_Service.sigdi.application.port.OrganicaLookupPort;
import cv.igrp.RH_Service.sigdi.application.service.ActivityApprovalHistoryRecorder;
import cv.igrp.RH_Service.sigdi.application.service.PaaActivityWindowPolicy;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.StrategicGoalRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.TacticalActivity;
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
  private final PaaActivityWindowPolicy windowPolicy;
  private final ActivityApprovalHistoryRecorder historyRecorder;

  public UpdateTacticalActivityCommandHandler(EconomicClassifierPort economicClassifierPort,
      StrategicGoalRepository goalRepository,
      TacticalActivityRepository activityRepository,
      OrganicaLookupPort organicaLookupPort,
      FuncionarioLookupPort funcionarioLookupPort,
      PaaActivityWindowPolicy windowPolicy,
      ActivityApprovalHistoryRecorder historyRecorder) {
    this.economicClassifierPort = economicClassifierPort;
    this.goalRepository = goalRepository;
    this.activityRepository = activityRepository;
    this.organicaLookupPort = organicaLookupPort;
    this.funcionarioLookupPort = funcionarioLookupPort;
    this.windowPolicy = windowPolicy;
    this.historyRecorder = historyRecorder;
  }

  @IgrpCommandHandler
  public ResponseEntity<TacticalActivityResponseDTO> handle(UpdateTacticalActivityCommand command) {
    LOGGER.debug("UpdateTacticalActivityCommand : {}", command);

    TacticalActivityId activityId = TacticalActivityId.from(command.getId());
    TacticalActivity activity = activityRepository.findById(activityId)
        .orElseThrow(() -> IgrpResponseStatusException.notFound("Atividade não encontrada"));

    // PRAZO-03: fail-closed deadline enforcement — sourced from the loaded entity's
    // paaLevel (never the request DTO, since update() does not accept/change it). 136-11:
    // consulta movida para o dono único do critério (PaaActivityWindowPolicy), que já lê o
    // ano no fuso de Cabo Verde.
    windowPolicy.requireOpenFor(activity.getPaaLevel());

    // A-136-50 (Phase 136, plano 136-15): capturado antes de update(), que sempre reverte o
    // estado (TacticalActivity.java:398, "as per requirements") -- o 136-13 mediu que esta
    // reversão nunca chegava a t_activity_approval_history, oitava porta do defeito que o
    // 136-10 corrigiu nos outros sete handlers.
    String previousStatus = activity.getStatus().getCode();

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

    // A-136-50 (136-15): só grava quando a chamada produziu uma transição real -- uma DRAFT
    // com orçamento que se mantém DRAFT depois de gravar não é um ato de auditoria, e não
    // deixa linha. O colaborador não valida isto por si (ver o seu Javadoc), por isso a
    // guarda vive aqui.
    String newStatus = saved.getStatus().getCode();
    if (!previousStatus.equals(newStatus)) {
      historyRecorder.record(saved.getId(), previousStatus, newStatus, newStatus, null);
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
    // A-135-2Z (Phase 136, plano 136-11): a resposta do PUT trazia paaLevel/paaLevelDesc a
    // null, apesar de a coluna paa_level na base estar correta -- update() (linha 404, ver
    // TacticalActivity.java) devolve this.paaLevel inalterado, nunca aceita nem altera este
    // campo. A resposta passa a espelhar a base, como CreateTacticalActivityCommandHandler
    // já fazia.
    response.setPaaLevel(saved.getPaaLevel() != null ? saved.getPaaLevel().getCode() : PaaLevel.UNIT_LEVEL.getCode());
    response.setPaaLevelDesc(saved.getPaaLevel() != null ? saved.getPaaLevel().getDescription() : PaaLevel.UNIT_LEVEL.getDescription());

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
