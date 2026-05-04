package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.ChangeRequestEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.TacticalActivitiesEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.TaticalActivityHistoryEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.TacticalActivitiesEntityRepository;
import cv.igrp.RH_Service.sigdi.application.constants.TacticalActivityStatus;
import cv.igrp.RH_Service.sigdi.application.dto.ChangeRequestResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.KeyResultResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.TacticalActivityDetailDTO;
import cv.igrp.RH_Service.sigdi.application.dto.WorkflowHistoryItemDTO;
import cv.igrp.RH_Service.sigdi.application.port.FuncionarioLookupPort;
import cv.igrp.RH_Service.sigdi.application.port.OrganicaLookupPort;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.KeyResultsEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
public class GetTacticalActivityByIdQueryHandler
    implements QueryHandler<GetTacticalActivityByIdQuery, ResponseEntity<TacticalActivityDetailDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GetTacticalActivityByIdQueryHandler.class);

  private final TacticalActivitiesEntityRepository repository;
  private final OrganicaLookupPort organicaLookupPort;
  private final FuncionarioLookupPort funcionarioLookupPort;

  public GetTacticalActivityByIdQueryHandler(TacticalActivitiesEntityRepository repository,
      OrganicaLookupPort organicaLookupPort,
      FuncionarioLookupPort funcionarioLookupPort) {
    this.repository = repository;
    this.organicaLookupPort = organicaLookupPort;
    this.funcionarioLookupPort = funcionarioLookupPort;
  }

  @IgrpQueryHandler
  @Transactional(readOnly = true)
  public ResponseEntity<TacticalActivityDetailDTO> handle(GetTacticalActivityByIdQuery query) {
    LOGGER.debug("GetTacticalActivityByIdQuery: {}", query);

    UUID id = UUID.fromString(query.getId());
    TacticalActivitiesEntity entity = repository.findByIdOrThrow(id);

    TacticalActivityDetailDTO dto = toDetailDTO(entity);

    return ResponseEntity.ok(dto);
  }

  private TacticalActivityDetailDTO toDetailDTO(TacticalActivitiesEntity entity) {
    TacticalActivityDetailDTO dto = new TacticalActivityDetailDTO();

    dto.setId(entity.getId().toString());
    dto.setStrategicGoalId(entity.getStrategicGoalId() != null ? entity.getStrategicGoalId().toString() : null);
    dto.setOrganicUnitId(entity.getOrganicUnitId() != null ? entity.getOrganicUnitId().toString() : null);
    if (entity.getOrganicUnitId() != null) {
      organicaLookupPort.findById(entity.getOrganicUnitId())
          .ifPresent(o -> dto.setOrganicUnitName(o.getName()));
    }
    dto.setTitle(entity.getTitle());
    dto.setDescriptionWhat(entity.getDescriptionWhat());
    dto.setJustificationWhy(entity.getJustificationWhy());
    dto.setResponsibleWho(entity.getResponsibleWho() != null ? entity.getResponsibleWho().toString() : null);
    if (entity.getResponsibleWho() != null) {
      funcionarioLookupPort.findById(entity.getResponsibleWho())
          .ifPresent(f -> dto.setResponsibleName(f.getNomeCompleto()));
    }
    dto.setLocationWhere(entity.getLocationWhere());
    dto.setMethodologyHow(entity.getMethodologyHow());
    dto.setStartDate(entity.getStartDate() != null ? entity.getStartDate().toString() : null);
    dto.setEndDate(entity.getEndDate() != null ? entity.getEndDate().toString() : null);
    dto.setFiscalYear(entity.getFiscalYear());
    dto.setEconomicClassifier(entity.getEconomicClassifier());
    dto.setBudgetEstimated(entity.getBudgetEstimated());
    dto.setBudgetCommitted(entity.getBudgetCommitted());
    dto.setBudgetLiquidated(entity.getBudgetLiquidated());
    dto.setBudgetPaid(entity.getBudgetPaid());
    dto.setStatus(entity.getStatus());
    dto.setVersion(entity.getVersion());
    dto.setCreatedAt(entity.getCreatedDate() != null ? entity.getCreatedDate().toString() : null);
    dto.setUpdatedAt(entity.getLastModifiedDate() != null ? entity.getLastModifiedDate().toString() : null);

    if (entity.getStatus() != null) {
      TacticalActivityStatus.fromCode(entity.getStatus())
          .ifPresent(s -> dto.setStatusDesc(s.getDescription()));
    }

    List<KeyResultResponseDTO> krs = entity.getKeyResults().stream()
        .map(this::toKeyResultDTO)
        .toList();
    dto.setKeyResults(krs);

    List<WorkflowHistoryItemDTO> history = entity.getHistoricals().stream()
        .map(this::toHistoryItem)
        .toList();
    dto.setWorkflowHistory(history);

    List<ChangeRequestResponseDTO> changeRequests = entity.getChangeRequests().stream()
        .map(this::toChangeRequest)
        .toList();
    dto.setChangeRequests(changeRequests);

    return dto;
  }

  private KeyResultResponseDTO toKeyResultDTO(KeyResultsEntity entity) {
    KeyResultResponseDTO dto = new KeyResultResponseDTO();
    dto.setId(entity.getId());
    dto.setTitle(entity.getTitle());
    dto.setTargetValue(entity.getTargetValue());
    dto.setCurrentValue(entity.getCurrentValue());
    dto.setMetricUnit(entity.getMetricUnit());
    dto.setWeight(entity.getWeight());
    dto.setCriteriaSuperado(entity.getCriteriaSuperado());
    dto.setCriteriaSeguranca(entity.getCriteriaSeguranca());
    dto.setCriteriaAlcancado(entity.getCriteriaAlcancado());
    dto.setCriteriaInsuficiente(entity.getCriteriaInsuficiente());
    dto.setActivityId(entity.getActivityId() != null ? entity.getActivityId().getId() : null);
    dto.setOkrId(entity.getOkrId() != null ? entity.getOkrId().getId() : null);
    return dto;
  }

  private WorkflowHistoryItemDTO toHistoryItem(TaticalActivityHistoryEntity h) {
    WorkflowHistoryItemDTO item = new WorkflowHistoryItemDTO();
    item.setAction(h.getAction());
    item.setActorId(h.getActorId() != null ? h.getActorId().toString() : null);
    item.setActorName(null);
    item.setFromStatus(h.getFromStatus());
    item.setToStatus(h.getToStatus());
    item.setComment(h.getComment());
    item.setDelegatedBy(h.getDelegatedBy() != null ? h.getDelegatedBy().toString() : null);
    item.setTimestamp(h.getCreatedDate() != null ? h.getCreatedDate().toString() : null);
    return item;
  }

  private ChangeRequestResponseDTO toChangeRequest(ChangeRequestEntity cr) {
    ChangeRequestResponseDTO dto = new ChangeRequestResponseDTO();
    dto.setId(cr.getId());
    dto.setActivityId(cr.getActivityId() != null ? cr.getActivityId().getId() : null);
    dto.setFieldName(cr.getFieldName());
    dto.setCurrentValue(cr.getCurrentValue());
    dto.setProposedValue(cr.getProposedValue());
    dto.setJustification(cr.getJustification());
    dto.setStatus(cr.getStatus());
    dto.setReviewerComment(cr.getReviewerComment());
    return dto;
  }
}