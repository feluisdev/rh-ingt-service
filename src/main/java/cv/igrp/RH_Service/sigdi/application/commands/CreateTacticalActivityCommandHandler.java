package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cv.igrp.RH_Service.sigdi.application.dto.TacticalActivityResponseDTO;

@Component
public class CreateTacticalActivityCommandHandler implements CommandHandler<CreateTacticalActivityCommand, ResponseEntity<TacticalActivityResponseDTO>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(CreateTacticalActivityCommandHandler.class);

   public CreateTacticalActivityCommandHandler() {

   }

   @IgrpCommandHandler
   public ResponseEntity<TacticalActivityResponseDTO> handle(CreateTacticalActivityCommand command) {

      LOGGER.debug("CreateTacticalActivityCommand : {}", command);

      // TODO: Implement the command handling logic here
      return null;
   }

}