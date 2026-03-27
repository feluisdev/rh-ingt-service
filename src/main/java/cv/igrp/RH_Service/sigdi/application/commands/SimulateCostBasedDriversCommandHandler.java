package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@Component
public class SimulateCostBasedDriversCommandHandler implements CommandHandler<SimulateCostBasedDriversCommand, ResponseEntity<String>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(SimulateCostBasedDriversCommandHandler.class);

   public SimulateCostBasedDriversCommandHandler() {

   }

   @IgrpCommandHandler
   public ResponseEntity<String> handle(SimulateCostBasedDriversCommand command) {

      LOGGER.debug("SimulateCostBasedDriversCommand : {}", command);

      // TODO: Implement the command handling logic here
      return null;
   }

}