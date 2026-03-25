package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cv.igrp.RH_Service.sigdi.application.dto.StategicGoalResponseDTO;

@Component
public class CreateStrategicGoalCommandHandler implements CommandHandler<CreateStrategicGoalCommand, ResponseEntity<StategicGoalResponseDTO>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(CreateStrategicGoalCommandHandler.class);

   public CreateStrategicGoalCommandHandler() {

   }

   @IgrpCommandHandler
   public ResponseEntity<StategicGoalResponseDTO> handle(CreateStrategicGoalCommand command) {

      LOGGER.debug("CreateStrategicGoalCommand : {}", command);

      // TODO: Implement the command handling logic here
      return null;
   }

}