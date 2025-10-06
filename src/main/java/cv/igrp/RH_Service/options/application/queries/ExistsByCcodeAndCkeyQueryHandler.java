package cv.igrp.RH_Service.options.application.queries;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;



@Component
public class ExistsByCcodeAndCkeyQueryHandler implements QueryHandler<ExistsByCcodeAndCkeyQuery, ResponseEntity<Boolean>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(ExistsByCcodeAndCkeyQueryHandler.class);


  public ExistsByCcodeAndCkeyQueryHandler() {

  }

   @IgrpQueryHandler
  public ResponseEntity<Boolean> handle(ExistsByCcodeAndCkeyQuery query) {
    // TODO: Implement the query handling logic here
    return null;
  }

}