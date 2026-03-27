package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Component
public class ChangeStatusTacticalActivityCommandHandler implements CommandHandler<ChangeStatusTacticalActivityCommand, ResponseEntity<Map<String, ?>>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(ChangeStatusTacticalActivityCommandHandler.class);

   public ChangeStatusTacticalActivityCommandHandler() {

   }

   @IgrpCommandHandler
   public ResponseEntity<Map<String, ?>> handle(ChangeStatusTacticalActivityCommand command) {

      LOGGER.debug("ChangeStatusTacticalActivityCommand : {}", command);

      // TODO: Implement the command handling logic here
      return null;
   }

}