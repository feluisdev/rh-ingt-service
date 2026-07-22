package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.StrategyMapRelationshipType;
import cv.igrp.RH_Service.sigdi.application.dto.StrategyLinkDTO;
import cv.igrp.RH_Service.sigdi.application.dto.StrategyMapLinkResponseDTO;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.StrategyMapLink;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.BscPerspectiveConfigRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.InstitutionalIdentityRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.StrategicGoalRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.StrategyMapLinkRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CreateStrategyMapLinkCommandHandler
    implements CommandHandler<CreateStrategyMapLinkCommand, ResponseEntity<StrategyMapLinkResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateStrategyMapLinkCommandHandler.class);

  private final InstitutionalIdentityRepository identityRepository;
  private final StrategicGoalRepository goalRepository;
  private final StrategyMapLinkRepository linkRepository;
  private final BscPerspectiveConfigRepository perspectiveConfigRepository;

  public CreateStrategyMapLinkCommandHandler(InstitutionalIdentityRepository identityRepository,
      StrategicGoalRepository goalRepository,
      StrategyMapLinkRepository linkRepository,
      BscPerspectiveConfigRepository perspectiveConfigRepository) {
    this.identityRepository = identityRepository;
    this.goalRepository = goalRepository;
    this.linkRepository = linkRepository;
    this.perspectiveConfigRepository = perspectiveConfigRepository;
  }

  @IgrpCommandHandler
  @Transactional
  public ResponseEntity<StrategyMapLinkResponseDTO> handle(CreateStrategyMapLinkCommand command) {
    LOGGER.debug("CreateStrategyMapLinkCommand : {}", command);

    StrategyLinkDTO request = command.getStrategylink();
    if (request == null || request.getSourceGoalId() == null || request.getTargetGoalId() == null
        || request.getRelationshipType() == null) {
      throw IgrpResponseStatusException.badRequest("Dados do link em falta");
    }
    StrategicGoalId sourceId = StrategicGoalId.from(request.getSourceGoalId());
    StrategicGoalId targetId = StrategicGoalId.from(request.getTargetGoalId());
    if (sourceId.equals(targetId)) {
      throw IgrpResponseStatusException.badRequest("sourceGoalId deve ser diferente de targetGoalId");
    }

    var activeIdentity = identityRepository.findActive()
        .orElseThrow(() -> IgrpResponseStatusException.badRequest(
            "Identidade Institucional ativa não encontrada"));

    StrategyMapRelationshipType type =
        StrategyMapRelationshipType.fromCodeOrThrow(request.getRelationshipType());

    var sourceGoal = goalRepository.findById(sourceId)
        .orElseThrow(() -> IgrpResponseStatusException.badRequest("sourceGoalId inválido"));
    var targetGoal = goalRepository.findById(targetId)
        .orElseThrow(() -> IgrpResponseStatusException.badRequest("targetGoalId inválido"));

    if (!activeIdentity.getId().equals(sourceGoal.getIdentityId())
        || !activeIdentity.getId().equals(targetGoal.getIdentityId())) {
      throw IgrpResponseStatusException.badRequest("Os goals devem pertencer à identity ativa");
    }

    if (sourceGoal.getPerspective() == null || targetGoal.getPerspective() == null) {
      throw IgrpResponseStatusException.badRequest("Perspetiva do objetivo não definida");
    }

    int sourceOrder = perspectiveOrder(sourceGoal.getPerspective().getCode());
    int targetOrder = perspectiveOrder(targetGoal.getPerspective().getCode());
    if (sourceOrder < targetOrder) {
      throw IgrpResponseStatusException.badRequest(
          "Ligação inválida: a perspetiva de origem não pode estar numa ordem inferior à da perspetiva de destino no fluxo causa-efeito do BSC");
    }

    linkRepository.findBySourceAndTarget(sourceId, targetId)
        .ifPresent(existing -> {
          throw IgrpResponseStatusException.badRequest("Já existe um link com os mesmos goals");
        });

    StrategyMapLink link = StrategyMapLink.create(
        activeIdentity.getInstitutionId(), sourceId, targetId, type);
    StrategyMapLink saved;
    try {
      saved = linkRepository.save(link);
    } catch (DataIntegrityViolationException e) {
      LOGGER.warn("Concurrent duplicate link save rejected by DB constraint (source={}, target={})",
          sourceId, targetId, e);
      throw IgrpResponseStatusException.badRequest("Já existe um link com os mesmos goals");
    }

    StrategyMapLinkResponseDTO response = new StrategyMapLinkResponseDTO();
    response.setId(saved.getId().getValor().getValor());
    response.setSourceGoalId(saved.getSourceGoalId().getValor().getValor());
    response.setTargetGoalId(saved.getTargetGoalId().getValor().getValor());
    response.setRelationshipType(saved.getRelationshipType().getCode());

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  private int perspectiveOrder(String perspectiveCode) {
    return perspectiveConfigRepository.findByCode(perspectiveCode)
        .orElseThrow(() -> IgrpResponseStatusException.badRequest(
            "Perspetiva não configurada: " + perspectiveCode))
        .getDisplayOrder();
  }
}
