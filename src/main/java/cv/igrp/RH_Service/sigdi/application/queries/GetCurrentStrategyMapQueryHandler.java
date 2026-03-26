package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.dto.StategicGoalResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.StrategyMapDataDTO;
import cv.igrp.RH_Service.sigdi.application.dto.StrategyMapLinkResponseDTO;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.InstitutionalIdentityRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.StrategicGoalRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.StrategyMapLinkRepository;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GetCurrentStrategyMapQueryHandler implements QueryHandler<GetCurrentStrategyMapQuery, ResponseEntity<StrategyMapDataDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GetCurrentStrategyMapQueryHandler.class);

  private final InstitutionalIdentityRepository identityRepository;
  private final StrategicGoalRepository goalRepository;
  private final StrategyMapLinkRepository linkRepository;

  public GetCurrentStrategyMapQueryHandler(InstitutionalIdentityRepository identityRepository,
                                          StrategicGoalRepository goalRepository,
                                          StrategyMapLinkRepository linkRepository) {
    this.identityRepository = identityRepository;
    this.goalRepository = goalRepository;
    this.linkRepository = linkRepository;
  }

  @IgrpQueryHandler
  public ResponseEntity<StrategyMapDataDTO> handle(GetCurrentStrategyMapQuery query) {
    LOGGER.debug("GetCurrentStrategyMapQuery: {}", query);

    var activeIdentity = identityRepository.findActive()
        .orElseThrow(() -> IgrpResponseStatusException.notFound("Identidade Institucional ativa não encontrada"));

    var goals = goalRepository.findByIdentityId(activeIdentity.getId());
    var links = linkRepository.findByIdentityId(activeIdentity.getId());

    StrategyMapDataDTO response = new StrategyMapDataDTO();
    response.setGoals(goals.stream().map(this::toGoalResponse).toList());
    response.setLinks(links.stream().map(this::toLinkResponse).toList());

    return ResponseEntity.ok(response);
  }

  private StategicGoalResponseDTO toGoalResponse(cv.igrp.RH_Service.sigdi.domain.strategy.models.StrategicGoal goal) {
    StategicGoalResponseDTO dto = new StategicGoalResponseDTO();
    dto.setId(goal.getId().getValor().getValor());
    dto.setIdentityId(goal.getIdentityId().getValor().getValor());
    dto.setPerspective(goal.getPerspective().getCode());
    dto.setPerspectiveDesc(goal.getPerspective().getDescription());
    dto.setWeight(goal.getWeight());
    dto.setTitle(goal.getTitle());
    dto.setDescription(goal.getDescription());
    dto.setStatus(goal.getStatus().getCode());
    dto.setStatusDesc(goal.getStatus().getDescription());
    return dto;
  }

  private StrategyMapLinkResponseDTO toLinkResponse(cv.igrp.RH_Service.sigdi.domain.strategy.models.StrategyMapLink link) {
    StrategyMapLinkResponseDTO dto = new StrategyMapLinkResponseDTO();
    dto.setId(link.getId().getValor().getValor());
    dto.setSourceGoalId(link.getSourceGoalId().getValor().getValor());
    dto.setTargetGoalId(link.getTargetGoalId().getValor().getValor());
    dto.setRelationshipType(link.getRelationshipType().getCode());
    return dto;
  }
}

