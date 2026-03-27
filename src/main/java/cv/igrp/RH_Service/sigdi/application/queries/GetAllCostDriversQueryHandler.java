package cv.igrp.RH_Service.sigdi.application.queries;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import cv.igrp.RH_Service.sigdi.application.dto.WrapperCostDriverListDTO;

@Component
public class GetAllCostDriversQueryHandler implements QueryHandler<GetAllCostDriversQuery, ResponseEntity<WrapperCostDriverListDTO>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(GetAllCostDriversQueryHandler.class);


  public GetAllCostDriversQueryHandler() {

  }

   @IgrpQueryHandler
  public ResponseEntity<WrapperCostDriverListDTO> handle(GetAllCostDriversQuery query) {

    LOGGER.debug("GetAllCostDriversQuery: {}", query);

    // TODO: Implement the query handling logic here
    return null;
  }

}