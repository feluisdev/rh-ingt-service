package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.ChangeRequestEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.TacticalActivitiesEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.TaticalActivityHistoryEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.TacticalActivitiesEntityRepository;
import cv.igrp.RH_Service.sigdi.application.constants.TacticalActivityStatus;
import cv.igrp.RH_Service.sigdi.application.dto.ChangeRequestResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.TacticalActivityDetailDTO;
import cv.igrp.RH_Service.sigdi.application.dto.WorkflowHistoryItemDTO;
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

  public GetTacticalActivityByIdQueryHandler(TacticalActivitiesEntityRepository repository) {
    this.repository = repository;
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
    dto.setTitle(entity.getTitle());
    dto.setJustificationWhy(entity.getJustificationWhy());
    dto.setResponsibleWho(entity.getResponsibleWho() != null ? entity.getResponsibleWho().toString() : null);
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