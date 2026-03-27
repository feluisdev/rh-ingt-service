package cv.igrp.RH_Service.sigdi.application.queries;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import cv.igrp.RH_Service.sigdi.application.dto.CostDriverResponseDTO;

@Component
public class GetCostDriverQueryHandler implements QueryHandler<GetCostDriverQuery, ResponseEntity<CostDriverResponseDTO>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(GetCostDriverQueryHandler.class);


  public GetCostDriverQueryHandler() {

  }

   @IgrpQueryHandler
  public ResponseEntity<CostDriverResponseDTO> handle(GetCostDriverQuery query) {

    LOGGER.debug("GetCostDriverQuery: {}", query);

    // TODO: Implement the query handling logic here
    return null;
  }

}