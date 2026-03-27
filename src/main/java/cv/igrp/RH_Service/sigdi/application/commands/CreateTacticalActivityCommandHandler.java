package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.dto.BudgetInfoDTO;
import cv.igrp.RH_Service.sigdi.application.port.EconomicClassifierPort;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cv.igrp.RH_Service.sigdi.application.dto.TacticalActivityResponseDTO;

@Component
public class CreateTacticalActivityCommandHandler implements CommandHandler<CreateTacticalActivityCommand, ResponseEntity<TacticalActivityResponseDTO>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(CreateTacticalActivityCommandHandler.class);

   private final EconomicClassifierPort economicClassifierPort;

   public CreateTacticalActivityCommandHandler(EconomicClassifierPort economicClassifierPort) {

     this.economicClassifierPort = economicClassifierPort;
   }

   @IgrpCommandHandler
   public ResponseEntity<TacticalActivityResponseDTO> handle(CreateTacticalActivityCommand command) {

      LOGGER.debug("CreateTacticalActivityCommand : {}", command);

      var request = command.getCreatetacticalactivity();

      var economicClassifier = request.getEconomicClassifier();

     BudgetInfoDTO budgetInfo = economicClassifierPort.getBudget(economicClassifier);

     if (request.getBudgetEstimated().compareTo(budgetInfo.availableBudget()) > 0) {
       throw IgrpResponseStatusException.of(
           HttpStatus.UNPROCESSABLE_ENTITY,
           "Budget limit exceeded for this classifier. Available: " + budgetInfo.availableBudget()
       );
     }

      return null;
   }

}
