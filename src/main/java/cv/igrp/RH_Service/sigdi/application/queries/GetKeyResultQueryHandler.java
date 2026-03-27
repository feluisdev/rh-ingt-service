package cv.igrp.RH_Service.sigdi.application.queries;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import cv.igrp.RH_Service.sigdi.application.dto.KeyResultResponseDTO;

@Component
public class GetKeyResultQueryHandler implements QueryHandler<GetKeyResultQuery, ResponseEntity<KeyResultResponseDTO>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(GetKeyResultQueryHandler.class);


  public GetKeyResultQueryHandler() {

  }

   @IgrpQueryHandler
  public ResponseEntity<KeyResultResponseDTO> handle(GetKeyResultQuery query) {

    LOGGER.debug("GetKeyResultQuery: {}", query);

    // TODO: Implement the query handling logic here
    return null;
  }

}