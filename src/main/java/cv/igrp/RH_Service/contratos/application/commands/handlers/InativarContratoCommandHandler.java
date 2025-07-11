package cv.igrp.RH_Service.contratos.application.commands.handlers;

import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.RH_Service.contratos.application.commands.commands.InativarContratoCommand;



@Service
public class InativarContratoCommandHandler implements CommandHandler<InativarContratoCommand, ResponseEntity<String>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(InativarContratoCommandHandler.class);

   public InativarContratoCommandHandler() {

   }

   @IgrpCommandHandler
   public ResponseEntity<String> handle(InativarContratoCommand command) {
      // TODO: Implement the command handling logic here
      return null;
   }

}