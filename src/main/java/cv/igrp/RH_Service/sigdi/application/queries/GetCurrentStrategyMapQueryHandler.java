package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.dto.StrategyMapDataDTO;
import cv.igrp.RH_Service.sigdi.application.dto.StrategyMapLinkResponseDTO;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.BscPerspectiveConfig;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.BscPerspectiveConfigRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.InstitutionalIdentityRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.StrategicGoalRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.StrategyMapLinkRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.strategy.StrategicGoalMapper;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GetCurrentStrategyMapQueryHandler implements QueryHandler<GetCurrentStrategyMapQuery, ResponseEntity<StrategyMapDataDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GetCurrentStrategyMapQueryHandler.class);

  private final InstitutionalIdentityRepository identityRepository;
  private final StrategicGoalRepository goalRepository;
  private final StrategyMapLinkRepository linkRepository;
  private final BscPerspectiveConfigRepository bscPerspectiveConfigRepository;
  private final StrategicGoalMapper goalMapper;

  public GetCurrentStrategyMapQueryHandler(InstitutionalIdentityRepository identityRepository,
                                          StrategicGoalRepository goalRepository,
                                          StrategyMapLinkRepository linkRepository,
                                          BscPerspectiveConfigRepository bscPerspectiveConfigRepository,
                                          StrategicGoalMapper goalMapper) {
    this.identityRepository = identityRepository;
    this.goalRepository = goalRepository;
    this.linkRepository = linkRepository;
    this.bscPerspectiveConfigRepository = bscPerspectiveConfigRepository;
    this.goalMapper = goalMapper;
  }

  @IgrpQueryHandler
  public ResponseEntity<StrategyMapDataDTO> handle(GetCurrentStrategyMapQuery query) {
    LOGGER.debug("GetCurrentStrategyMapQuery: {}", query);

    var activeIdentity = identityRepository.findActive()
        .orElseThrow(() -> IgrpResponseStatusException.notFound("Identidade Institucional ativa não encontrada"));

    var goals = goalRepository.findByIdentityId(activeIdentity.getId());
    var links = linkRepository.findByIdentityId(activeIdentity.getId());
    Map<String, String> labelsByCode = bscPerspectiveConfigRepository.findAll().stream()
        .collect(Collectors.toMap(BscPerspectiveConfig::getCode, BscPerspectiveConfig::getLabel));

    StrategyMapDataDTO response = new StrategyMapDataDTO();
    response.setGoals(goals.stream().map(g -> goalMapper.toResponse(g, labelsByCode)).toList());
    response.setLinks(links.stream().map(this::toLinkResponse).toList());

    return ResponseEntity.ok(response);
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

