package cv.igrp.RH_Service.funcionarios.application.commands;

import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cv.igrp.RH_Service.funcionarios.application.dto.CargoResponseDTO;

@Component
public class UpdateCargoCommandHandler implements CommandHandler<UpdateCargoCommand, ResponseEntity<CargoResponseDTO>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(UpdateCargoCommandHandler.class);

   public UpdateCargoCommandHandler() {

   }

   @IgrpCommandHandler
   public ResponseEntity<CargoResponseDTO> handle(UpdateCargoCommand command) {
      // TODO: Implement the command handling logic here
      return null;
   }

}