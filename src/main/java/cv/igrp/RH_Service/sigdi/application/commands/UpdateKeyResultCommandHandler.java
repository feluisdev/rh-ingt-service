package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cv.igrp.RH_Service.sigdi.application.dto.KeyResultResponseDTO;

@Component
public class UpdateKeyResultCommandHandler implements CommandHandler<UpdateKeyResultCommand, ResponseEntity<KeyResultResponseDTO>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(UpdateKeyResultCommandHandler.class);

   public UpdateKeyResultCommandHandler() {

   }

   @IgrpCommandHandler
   public ResponseEntity<KeyResultResponseDTO> handle(UpdateKeyResultCommand command) {

      LOGGER.debug("UpdateKeyResultCommand : {}", command);

      // TODO: Implement the command handling logic here
      return null;
   }

}