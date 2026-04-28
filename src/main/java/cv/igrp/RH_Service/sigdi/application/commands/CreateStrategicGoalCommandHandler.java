package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.StrategicGoalsPerspective;
import cv.igrp.RH_Service.sigdi.application.dto.CreateStategicGoalDTO;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.StrategicGoal;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.InstitutionalIdentityRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.StrategicGoalRepository;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cv.igrp.RH_Service.sigdi.application.dto.StategicGoalResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.StrategicIndicatorDTO;

@Component
public class CreateStrategicGoalCommandHandler
    implements CommandHandler<CreateStrategicGoalCommand, ResponseEntity<StategicGoalResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateStrategicGoalCommandHandler.class);

  private final InstitutionalIdentityRepository identityRepository;
  private final StrategicGoalRepository goalRepository;

  public CreateStrategicGoalCommandHandler(InstitutionalIdentityRepository identityRepository,
      StrategicGoalRepository goalRepository) {
    this.identityRepository = identityRepository;
    this.goalRepository = goalRepository;
  }

  @IgrpCommandHandler
  public ResponseEntity<StategicGoalResponseDTO> handle(CreateStrategicGoalCommand command) {
    LOGGER.debug("CreateStrategicGoalCommand : {}", command);

    CreateStategicGoalDTO request = command.getCreatestategicgoal();

    var activeIdentity = identityRepository.findActive()
        .orElseThrow(() -> IgrpResponseStatusException.notFound(
            "Identidade Institucional ativa não encontrada"));

    StrategicGoalsPerspective perspective =
        StrategicGoalsPerspective.fromCodeOrThrow(request.getPerspective());

    java.util.List<cv.igrp.RH_Service.sigdi.domain.strategy.models.StrategicIndicator> domainIndicators = new java.util.ArrayList<>();
    if (request.getIndicators() != null && !request.getIndicators().isEmpty()) {
        domainIndicators = request.getIndicators().stream().map(dto -> 
            cv.igrp.RH_Service.sigdi.domain.strategy.models.StrategicIndicator.create(
                dto.getTitle(),
                dto.getFormula(),
                dto.getTarget(),
                dto.getEvaluationCriteria(),
                dto.getInfoSource(),
                dto.getWeight(),
                dto.getCriteriaSuperado(),
                dto.getCriteriaSeguranca(),
                dto.getCriteriaAlcancado(),
                dto.getCriteriaInsuficiente()
            )
        ).collect(java.util.stream.Collectors.toList());
    }

    StrategicGoal goal = StrategicGoal.create(
        activeIdentity.getInstitutionId(),
        activeIdentity.getId(),
        request.getTitle(),
        perspective,
        request.getWeight(),
        request.getDescription(),
        domainIndicators);

    StrategicGoal saved = goalRepository.save(goal);

    cv.igrp.RH_Service.sigdi.infrastructure.mappers.strategy.StrategicGoalMapper mapper = new cv.igrp.RH_Service.sigdi.infrastructure.mappers.strategy.StrategicGoalMapper();
    StategicGoalResponseDTO response = mapper.toResponse(saved);

    return ResponseEntity.ok(response);
  }
}
