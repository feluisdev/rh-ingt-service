package cv.igrp.RH_Service.options.application.commands;

import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cv.igrp.RH_Service.options.application.dto.OptionResponseDTO;

@Component
public class CreateOptionsCommandHandler implements CommandHandler<CreateOptionsCommand, ResponseEntity<OptionResponseDTO>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(CreateOptionsCommandHandler.class);

   public CreateOptionsCommandHandler() {

   }

   @IgrpCommandHandler
   public ResponseEntity<OptionResponseDTO> handle(CreateOptionsCommand command) {
      // TODO: Implement the command handling logic here
      return null;
   }

}