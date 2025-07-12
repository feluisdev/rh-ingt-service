package cv.igrp.RH_Service.funcionarios.application.commands;

import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Component
public class AtivarContratoCommandHandler implements CommandHandler<AtivarContratoCommand, ResponseEntity<Map<String, ?>>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(AtivarContratoCommandHandler.class);

   public AtivarContratoCommandHandler() {

   }

   @IgrpCommandHandler
   public ResponseEntity<Map<String, ?>> handle(AtivarContratoCommand command) {
      // TODO: Implement the command handling logic here
      return null;
   }

}