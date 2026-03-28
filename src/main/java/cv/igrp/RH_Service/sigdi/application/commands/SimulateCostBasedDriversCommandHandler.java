package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.CostDriverType;
import cv.igrp.RH_Service.sigdi.application.dto.CostDriverSimulateResponseDTO;
import cv.igrp.RH_Service.sigdi.domain.budget.models.CostDriver;
import cv.igrp.RH_Service.sigdi.domain.budget.repository.CostDriverRepository;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;


@Component
public class SimulateCostBasedDriversCommandHandler implements CommandHandler<SimulateCostBasedDriversCommand, ResponseEntity<CostDriverSimulateResponseDTO>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(SimulateCostBasedDriversCommandHandler.class);

  private final CostDriverRepository repository;


  public SimulateCostBasedDriversCommandHandler(CostDriverRepository repository) {

    this.repository = repository;
  }

   @IgrpCommandHandler
   public ResponseEntity<CostDriverSimulateResponseDTO> handle(SimulateCostBasedDriversCommand command) {

      LOGGER.debug("SimulateCostBasedDriversCommand : {}", command);

      var costDriverType = CostDriverType.fromCodeOrThrow(command.getCostdriversimulatereq().getDriverType());

     CostDriver driver = repository.findActiveByType(costDriverType, LocalDate.now())
         .orElseThrow(() -> IgrpResponseStatusException.notFound("No active CostDriver found for type " + costDriverType.getDescription()));
      // TODO: Implement the command handling logic here
      return null;
   }

}
