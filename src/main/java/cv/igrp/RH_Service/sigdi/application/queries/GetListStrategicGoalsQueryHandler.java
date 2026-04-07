package cv.igrp.RH_Service.sigdi.application.queries;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import cv.igrp.RH_Service.sigdi.application.dto.WrapperListStrategyGoalsDTO;

@Component
public class GetListStrategicGoalsQueryHandler implements QueryHandler<GetListStrategicGoalsQuery, ResponseEntity<WrapperListStrategyGoalsDTO>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(GetListStrategicGoalsQueryHandler.class);


  public GetListStrategicGoalsQueryHandler() {

  }

   @IgrpQueryHandler
  public ResponseEntity<WrapperListStrategyGoalsDTO> handle(GetListStrategicGoalsQuery query) {

    LOGGER.debug("GetListStrategicGoalsQuery: {}", query);

    // TODO: Implement the query handling logic here
    return null;
  }

}