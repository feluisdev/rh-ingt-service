package cv.igrp.RH_Service.sigdi.application.commands;

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
public class CancelStrategicGoalCommandHandler
    implements CommandHandler<CancelStrategicGoalCommand, ResponseEntity<String>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CancelStrategicGoalCommandHandler.class);

  private final StrategicGoalRepository goalRepository;

  public CancelStrategicGoalCommandHandler(StrategicGoalRepository goalRepository) {
    this.goalRepository = goalRepository;
  }

  @IgrpCommandHandler
  public ResponseEntity<String> handle(CancelStrategicGoalCommand command) {
    LOGGER.debug("CancelStrategicGoalCommand : {}", command);

    StrategicGoalId goalId = StrategicGoalId.from(UUID.fromString(command.getId()));

    StrategicGoal goal = goalRepository.findById(goalId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "Objetivo estratégico não encontrado"));

    if (!goal.isActive()) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
          "Objetivo já se encontra inativo");
    }

    StrategicGoal cancelled = goal.cancel();
    goalRepository.save(cancelled);

    return ResponseEntity.noContent().build();
  }
}