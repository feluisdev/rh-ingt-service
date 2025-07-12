package cv.igrp.RH_Service.funcionarios.application.commands;

import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@Component
public class CreateContratoCommandHandler implements CommandHandler<CreateContratoCommand, ResponseEntity<String>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(CreateContratoCommandHandler.class);

   public CreateContratoCommandHandler() {

   }

   @IgrpCommandHandler
   public ResponseEntity<String> handle(CreateContratoCommand command) {
      // TODO: Implement the command handling logic here
      return null;
   }

}