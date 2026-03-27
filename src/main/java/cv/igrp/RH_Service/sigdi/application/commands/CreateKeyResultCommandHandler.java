package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cv.igrp.RH_Service.sigdi.application.dto.KeyResultResponseDTO;

@Component
public class CreateKeyResultCommandHandler implements CommandHandler<CreateKeyResultCommand, ResponseEntity<KeyResultResponseDTO>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(CreateKeyResultCommandHandler.class);

   public CreateKeyResultCommandHandler() {

   }

   @IgrpCommandHandler
   public ResponseEntity<KeyResultResponseDTO> handle(CreateKeyResultCommand command) {

      LOGGER.debug("CreateKeyResultCommand : {}", command);

      // TODO: Implement the command handling logic here
      return null;
   }

}