package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.TacticalActivityStatus;
import cv.igrp.RH_Service.sigdi.application.dto.WrapperWorkflowInboxDTO;
import cv.igrp.RH_Service.sigdi.application.dto.WorkflowInboxItemDTO;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.TacticalActivity;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.TacticalActivityRepository;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GetWorkflowInboxQueryHandler
    implements QueryHandler<GetWorkflowInboxQuery, ResponseEntity<WrapperWorkflowInboxDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GetWorkflowInboxQueryHandler.class);

  private final TacticalActivityRepository repository;

  public GetWorkflowInboxQueryHandler(TacticalActivityRepository repository) {
    this.repository = repository;
  }

  @IgrpQueryHandler
  public ResponseEntity<WrapperWorkflowInboxDTO> handle(GetWorkflowInboxQuery query) {
    LOGGER.debug("GetWorkflowInboxQuery: {}", query);

    int page = parseOrDefault(query.getPageNumber(), 0);
    int size = parseOrDefault(query.getPageSize(), 20);

    List<String> pendingStatuses = List.of(
        TacticalActivityStatus.PENDING_TACTICAL.getCode(),
        TacticalActivityStatus.PENDING_STRATEGIC.getCode()
    );

    var activities = repository.findByStatuses(pendingStatuses, page, size);
    long total = repository.countByStatuses(pendingStatuses);

    var data = activities.stream().map(this::toInboxItem).toList();

    WrapperWorkflowInboxDTO response = new WrapperWorkflowInboxDTO();
    response.setData(data);
    response.setPageNumber(page);
    response.setPageSize(size);
    response.setTotalElements(total);
    response.setTotalPages((int) Math.ceil((double) total / size));

    return ResponseEntity.ok(response);
  }

  private WorkflowInboxItemDTO toInboxItem(TacticalActivity activity) {
    WorkflowInboxItemDTO dto = new WorkflowInboxItemDTO();
    dto.setId(activity.getId().getValor().getValor());
    dto.setTitle(activity.getTitle());
    dto.setCurrentStatus(activity.getStatus().getCode());
    dto.setBudgetEstimated(activity.getBudget().getEstimatedAmount());
    dto.setEconomicClassifier(activity.getBudget().getClassifier().getCode());
    return dto;
  }

  private int parseOrDefault(String value, int defaultValue) {
    try {
      return (value != null && !value.isBlank()) ? Integer.parseInt(value) : defaultValue;
    } catch (NumberFormatException e) {
      throw IgrpResponseStatusException.badRequest("Parâmetro de paginação inválido: " + value);
    }
  }
}
