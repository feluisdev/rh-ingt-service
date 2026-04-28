package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cv.igrp.RH_Service.sigdi.application.dto.CostDriverResponseDTO;

@Component
public class UpdateCostDriverCommandHandler implements CommandHandler<UpdateCostDriverCommand, ResponseEntity<CostDriverResponseDTO>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(UpdateCostDriverCommandHandler.class);

   public UpdateCostDriverCommandHandler() {

   }

   @IgrpCommandHandler
   public ResponseEntity<CostDriverResponseDTO> handle(UpdateCostDriverCommand command) {

      LOGGER.debug("UpdateCostDriverCommand : {}", command);

      // TODO: Implement the command handling logic here
      return null;
   }

}