package cv.igrp.RH_Service.sigdi.application.queries;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import cv.igrp.RH_Service.sigdi.application.dto.WrapperKeyResultListDTO;

@Component
public class GetAllKeyResultsQueryHandler implements QueryHandler<GetAllKeyResultsQuery, ResponseEntity<WrapperKeyResultListDTO>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(GetAllKeyResultsQueryHandler.class);


  public GetAllKeyResultsQueryHandler() {

  }

   @IgrpQueryHandler
  public ResponseEntity<WrapperKeyResultListDTO> handle(GetAllKeyResultsQuery query) {

    LOGGER.debug("GetAllKeyResultsQuery: {}", query);

    // TODO: Implement the query handling logic here
    return null;
  }

}