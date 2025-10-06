package cv.igrp.RH_Service.options.application.queries;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import cv.igrp.RH_Service.options.application.dto.WrapperListOptionsDTO;

@Component
public class GetListOptionsQueryHandler implements QueryHandler<GetListOptionsQuery, ResponseEntity<WrapperListOptionsDTO>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(GetListOptionsQueryHandler.class);


  public GetListOptionsQueryHandler() {

  }

   @IgrpQueryHandler
  public ResponseEntity<WrapperListOptionsDTO> handle(GetListOptionsQuery query) {
    // TODO: Implement the query handling logic here
    return null;
  }

}