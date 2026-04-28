package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.RH_Service.sigdi.application.port.EconomicClassifierPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import cv.igrp.RH_Service.sigdi.application.dto.BudgetInfoDTO;

@Component
public class GetBudgetQueryHandler implements QueryHandler<GetBudgetQuery, ResponseEntity<BudgetInfoDTO>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(GetBudgetQueryHandler.class);

  private final EconomicClassifierPort economicClassifierPort;

  public GetBudgetQueryHandler(EconomicClassifierPort economicClassifierPort) {

    this.economicClassifierPort = economicClassifierPort;
  }

   @IgrpQueryHandler
  public ResponseEntity<BudgetInfoDTO> handle(GetBudgetQuery query) {

    LOGGER.debug("GetBudgetQuery: {}", query);

    return ResponseEntity.ok(economicClassifierPort.getBudget(query.getEconomicClassifier()));
  }

}
