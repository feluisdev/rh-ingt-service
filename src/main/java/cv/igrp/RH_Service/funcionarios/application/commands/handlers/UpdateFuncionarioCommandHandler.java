package cv.igrp.RH_Service.funcionarios.application.commands.handlers;

import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.RH_Service.funcionarios.application.commands.commands.UpdateFuncionarioCommand;

import cv.igrp.RH_Service.funcionarios.application.dto.FuncionarioResponseDTO;

@Service
public class UpdateFuncionarioCommandHandler implements CommandHandler<UpdateFuncionarioCommand, ResponseEntity<FuncionarioResponseDTO>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(UpdateFuncionarioCommandHandler.class);

   public UpdateFuncionarioCommandHandler() {

   }

   @IgrpCommandHandler
   public ResponseEntity<FuncionarioResponseDTO> handle(UpdateFuncionarioCommand command) {
      // TODO: Implement the command handling logic here
      return null;
   }

}