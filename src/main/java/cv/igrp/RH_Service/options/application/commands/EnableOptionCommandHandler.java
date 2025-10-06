package cv.igrp.RH_Service.options.application.commands;

import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Component
public class EnableOptionCommandHandler implements CommandHandler<EnableOptionCommand, ResponseEntity<Map<String, ?>>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(EnableOptionCommandHandler.class);

   public EnableOptionCommandHandler() {

   }

   @IgrpCommandHandler
   public ResponseEntity<Map<String, ?>> handle(EnableOptionCommand command) {
      // TODO: Implement the command handling logic here
      return null;
   }

}