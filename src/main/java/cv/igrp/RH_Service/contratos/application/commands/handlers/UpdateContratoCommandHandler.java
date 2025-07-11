package cv.igrp.RH_Service.contratos.application.commands.handlers;

import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.RH_Service.contratos.application.commands.commands.UpdateContratoCommand;



@Service
public class UpdateContratoCommandHandler implements CommandHandler<UpdateContratoCommand, ResponseEntity<String>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(UpdateContratoCommandHandler.class);

   public UpdateContratoCommandHandler() {

   }

   @IgrpCommandHandler
   public ResponseEntity<String> handle(UpdateContratoCommand command) {
      // TODO: Implement the command handling logic here
      return null;
   }

}