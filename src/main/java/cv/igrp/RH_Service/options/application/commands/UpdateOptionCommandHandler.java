package cv.igrp.RH_Service.options.application.commands;

import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cv.igrp.RH_Service.options.application.dto.OptionResponseDTO;

@Component
public class UpdateOptionCommandHandler implements CommandHandler<UpdateOptionCommand, ResponseEntity<OptionResponseDTO>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(UpdateOptionCommandHandler.class);

   public UpdateOptionCommandHandler() {

   }

   @IgrpCommandHandler
   public ResponseEntity<OptionResponseDTO> handle(UpdateOptionCommand command) {
      // TODO: Implement the command handling logic here
      return null;
   }

}