package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.StrategyMapRelationshipType;
import cv.igrp.RH_Service.sigdi.application.dto.StrategyLinkDTO;
import cv.igrp.RH_Service.sigdi.application.dto.StrategyMapLinkResponseDTO;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.StrategyMapLink;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.InstitutionalIdentityRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.StrategicGoalRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.StrategyMapLinkRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CreateStrategyMapLinkCommandHandler implements CommandHandler<CreateStrategyMapLinkCommand, ResponseEntity<StrategyMapLinkResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateStrategyMapLinkCommandHandler.class);

  private final InstitutionalIdentityRepository identityRepository;
  private final StrategicGoalRepository goalRepository;
  private final StrategyMapLinkRepository linkRepository;

  public CreateStrategyMapLinkCommandHandler(InstitutionalIdentityRepository identityRepository,
                                             StrategicGoalRepository goalRepository,
                                             StrategyMapLinkRepository linkRepository) {
    this.identityRepository = identityRepository;
    this.goalRepository = goalRepository;
    this.linkRepository = linkRepository;
  }

  @IgrpCommandHandler
  @Transactional
  public ResponseEntity<StrategyMapLinkResponseDTO> handle(CreateStrategyMapLinkCommand command) {
    LOGGER.debug("CreateStrategyMapLinkCommand : {}", command);

    StrategyLinkDTO request = command.getStrategylink();
    if (request.getSourceGoalId().equals(request.getTargetGoalId())) {
      throw IgrpResponseStatusException.badRequest("sourceGoalId deve ser diferente de targetGoalId");
    }

    var activeIdentity = identityRepository.findActive()
        .orElseThrow(() -> IgrpResponseStatusException.badRequest("Identidade Institucional ativa não encontrada"));

    StrategicGoalId sourceId = StrategicGoalId.from(request.getSourceGoalId());
    StrategicGoalId targetId = StrategicGoalId.from(request.getTargetGoalId());
    StrategyMapRelationshipType type = StrategyMapRelationshipType.fromCodeOrThrow(request.getRelationshipType());

    var sourceGoal = goalRepository.findById(sourceId)
        .orElseThrow(() -> IgrpResponseStatusException.badRequest("sourceGoalId inválido"));
    var targetGoal = goalRepository.findById(targetId)
        .orElseThrow(() -> IgrpResponseStatusException.badRequest("targetGoalId inválido"));

    if (!activeIdentity.getId().equals(sourceGoal.getIdentityId()) || !activeIdentity.getId().equals(targetGoal.getIdentityId())) {
      throw IgrpResponseStatusException.badRequest("Os goals devem pertencer à identity ativa");
    }

    linkRepository.findBySourceTargetAndType(sourceId, targetId, type)
        .ifPresent(existing -> {
          throw IgrpResponseStatusException.badRequest("Já existe um link com os mesmos goals e relationshipType");
        });

    StrategyMapLink link = StrategyMapLink.create(sourceId, targetId, type);
    StrategyMapLink saved = linkRepository.save(link);

    StrategyMapLinkResponseDTO response = new StrategyMapLinkResponseDTO();
    response.setId(saved.getId().getValor().getValor());
    response.setSourceGoalId(saved.getSourceGoalId().getValor().getValor());
    response.setTargetGoalId(saved.getTargetGoalId().getValor().getValor());
    response.setRelationshipType(saved.getRelationshipType().getCode());

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}

