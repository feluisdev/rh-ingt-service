package cv.igrp.RH_Service.funcionarios.application.commands;

import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cv.igrp.RH_Service.funcionarios.application.dto.ContratoResponseDTO;

@Component
public class UpdateContratoCommandHandler implements CommandHandler<UpdateContratoCommand, ResponseEntity<ContratoResponseDTO>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(UpdateContratoCommandHandler.class);

   public UpdateContratoCommandHandler() {

   }

   @IgrpCommandHandler
   public ResponseEntity<ContratoResponseDTO> handle(UpdateContratoCommand command) {
      // TODO: Implement the command handling logic here
      return null;
   }

}