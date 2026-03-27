package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cv.igrp.RH_Service.sigdi.application.dto.CostDriverResponseDTO;

@Component
public class AddCostDriverCommandHandler implements CommandHandler<AddCostDriverCommand, ResponseEntity<CostDriverResponseDTO>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(AddCostDriverCommandHandler.class);

   public AddCostDriverCommandHandler() {

   }

   @IgrpCommandHandler
   public ResponseEntity<CostDriverResponseDTO> handle(AddCostDriverCommand command) {

      LOGGER.debug("AddCostDriverCommand : {}", command);

      // TODO: Implement the command handling logic here
      return null;
   }

}