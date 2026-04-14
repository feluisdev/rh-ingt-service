package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.sigdi.application.dto.StategicGoalResponseDTO;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.StrategicGoal;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.StrategicGoalRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.strategy.StrategicGoalMapper;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Component
public class UpdateStrategicGoalsCommandHandler implements CommandHandler<UpdateStrategicGoalsCommand, ResponseEntity<StategicGoalResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(UpdateStrategicGoalsCommandHandler.class);

  private final StrategicGoalRepository goalRepository;
  private final StrategicGoalMapper goalMapper;

  public UpdateStrategicGoalsCommandHandler(StrategicGoalRepository goalRepository,
                                            StrategicGoalMapper goalMapper) {
    this.goalRepository = goalRepository;
    this.goalMapper = goalMapper;
  }

  @IgrpCommandHandler
  public ResponseEntity<StategicGoalResponseDTO> handle(UpdateStrategicGoalsCommand command) {
    LOGGER.debug("UpdateStrategicGoalsCommand : {}", command);

    StrategicGoalId goalId = StrategicGoalId.from(UUID.fromString(command.getId()));

    StrategicGoal goal = goalRepository.findById(goalId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Strategic goal not found"));

    var dto = command.getUpdatestategicgoal();
    StrategicGoal updated = goal.update(dto.getTitle(), dto.getDescription(), dto.getWeight());
    StrategicGoal saved = goalRepository.save(updated);

    return ResponseEntity.ok(goalMapper.toResponse(saved));
  }
}