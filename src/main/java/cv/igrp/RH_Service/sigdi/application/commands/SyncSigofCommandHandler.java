package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Component
public class SyncSigofCommandHandler implements CommandHandler<SyncSigofCommand, ResponseEntity<Map<String, ?>>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(SyncSigofCommandHandler.class);

   public SyncSigofCommandHandler() {

   }

   @IgrpCommandHandler
   public ResponseEntity<Map<String, ?>> handle(SyncSigofCommand command) {

      LOGGER.debug("SyncSigofCommand : {}", command);

      // TODO: Implement the command handling logic here
      return null;
   }

}