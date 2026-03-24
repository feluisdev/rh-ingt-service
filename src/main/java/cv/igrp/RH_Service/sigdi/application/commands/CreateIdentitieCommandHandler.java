package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cv.igrp.RH_Service.sigdi.application.dto.IdentityResponseDTO;

@Component
public class CreateIdentitieCommandHandler implements CommandHandler<CreateIdentitieCommand, ResponseEntity<IdentityResponseDTO>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(CreateIdentitieCommandHandler.class);

   public CreateIdentitieCommandHandler() {

   }

   @IgrpCommandHandler
   public ResponseEntity<IdentityResponseDTO> handle(CreateIdentitieCommand command) {

      LOGGER.debug("CreateIdentitieCommand : {}", command);

      // TODO: Implement the command handling logic here
      return null;
   }

}