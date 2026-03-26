package cv.igrp.RH_Service.sigdi.application.queries;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import cv.igrp.RH_Service.sigdi.application.dto.IdentityResponseDTO;

@Component
public class GetCurrentIdentitieQueryHandler implements QueryHandler<GetCurrentIdentitieQuery, ResponseEntity<IdentityResponseDTO>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(GetCurrentIdentitieQueryHandler.class);


  public GetCurrentIdentitieQueryHandler() {

  }

   @IgrpQueryHandler
  public ResponseEntity<IdentityResponseDTO> handle(GetCurrentIdentitieQuery query) {

    LOGGER.debug("GetCurrentIdentitieQuery: {}", query);

    // TODO: Implement the query handling logic here
    return null;
  }

}