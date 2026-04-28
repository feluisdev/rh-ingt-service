package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.sigdi.application.dto.GoalPositionRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.GoalPositionResponseDTO;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.StrategicGoal;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.StrategicGoalRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
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
public class UpdateGoalPositionCommandHandler
    implements CommandHandler<UpdateGoalPositionCommand, ResponseEntity<GoalPositionResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(UpdateGoalPositionCommandHandler.class);

  private final StrategicGoalRepository goalRepository;

  public UpdateGoalPositionCommandHandler(StrategicGoalRepository goalRepository) {
    this.goalRepository = goalRepository;
  }

  @IgrpCommandHandler
  public ResponseEntity<GoalPositionResponseDTO> handle(UpdateGoalPositionCommand command) {
    LOGGER.debug("UpdateGoalPositionCommand : {}", command);

    StrategicGoalId goalId = StrategicGoalId.from(UUID.fromString(command.getGoalId()));

    StrategicGoal goal = goalRepository.findById(goalId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "Objetivo estratégico não encontrado"));

    GoalPositionRequestDTO req = command.getUpdategoalposition();
    StrategicGoal updated = goal.updatePosition(req.getX(), req.getY());
    goalRepository.save(updated);

    GoalPositionResponseDTO response = new GoalPositionResponseDTO();
    response.setGoalId(command.getGoalId());
    response.setX(req.getX());
    response.setY(req.getY());

    return ResponseEntity.ok(response);
  }
}