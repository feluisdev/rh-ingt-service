package cv.igrp.RH_Service.funcionarios.application.commands;

import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cv.igrp.RH_Service.funcionarios.application.dto.DepartamentoResponseDTO;

@Component
public class UpdateDepartamentoCommandHandler implements CommandHandler<UpdateDepartamentoCommand, ResponseEntity<DepartamentoResponseDTO>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(UpdateDepartamentoCommandHandler.class);

   public UpdateDepartamentoCommandHandler() {

   }

   @IgrpCommandHandler
   public ResponseEntity<DepartamentoResponseDTO> handle(UpdateDepartamentoCommand command) {
      // TODO: Implement the command handling logic here
      return null;
   }

}