package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.RH_Service.sigdi.application.dto.WrapperListStrategyGoalsDTO;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.InstitutionalIdentity;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.InstitutionalIdentityRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.StrategicGoalRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.strategy.StrategicGoalMapper;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Component
public class GetListStrategicGoalsQueryHandler implements QueryHandler<GetListStrategicGoalsQuery, ResponseEntity<WrapperListStrategyGoalsDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GetListStrategicGoalsQueryHandler.class);

  private final InstitutionalIdentityRepository identityRepository;
  private final StrategicGoalRepository goalRepository;
  private final StrategicGoalMapper goalMapper;

  public GetListStrategicGoalsQueryHandler(InstitutionalIdentityRepository identityRepository,
                                           StrategicGoalRepository goalRepository,
                                           StrategicGoalMapper goalMapper) {
    this.identityRepository = identityRepository;
    this.goalRepository = goalRepository;
    this.goalMapper = goalMapper;
  }

  @IgrpQueryHandler
  public ResponseEntity<WrapperListStrategyGoalsDTO> handle(GetListStrategicGoalsQuery query) {
    LOGGER.debug("GetListStrategicGoalsQuery: {}", query);

    InstitutionalIdentity activeIdentity = identityRepository.findActive()
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No active institutional identity found"));

    int page = query.getPageNumber() != null ? Integer.parseInt(query.getPageNumber()) : 0;
    int size = query.getPageSize() != null ? Integer.parseInt(query.getPageSize()) : 20;
    String perspective = (query.getPerspective() != null && !query.getPerspective().isBlank()) ? query.getPerspective() : null;
    String status = (query.getStatus() != null && !query.getStatus().isBlank()) ? query.getStatus() : null;
    String parentGoalId = (query.getParentGoalId() != null && !query.getParentGoalId().isBlank()) ? query.getParentGoalId() : null;

    var goals = goalRepository.findAll(activeIdentity.getId(), perspective, status, parentGoalId, page, size);
    long total = goalRepository.countAll(activeIdentity.getId(), perspective, status, parentGoalId);

    var data = goals.stream().map(goalMapper::toSummary).toList();

    WrapperListStrategyGoalsDTO response = new WrapperListStrategyGoalsDTO();
    response.setData(data);
    response.setPageNumber(page);
    response.setPageSize(size);
    response.setTotalElements(total);
    response.setTotalPages((int) Math.ceil((double) total / size));

    return ResponseEntity.ok(response);
  }
}