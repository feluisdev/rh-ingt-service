package cv.igrp.RH_Service.options.application.queries;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import cv.igrp.RH_Service.options.application.dto.OptionCodeResponseDTO;

@Component
public class FindByCcodeQueryHandler implements QueryHandler<FindByCcodeQuery, ResponseEntity<OptionCodeResponseDTO>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(FindByCcodeQueryHandler.class);


  public FindByCcodeQueryHandler() {

  }

   @IgrpQueryHandler
  public ResponseEntity<OptionCodeResponseDTO> handle(FindByCcodeQuery query) {
    // TODO: Implement the query handling logic here
    return null;
  }

}