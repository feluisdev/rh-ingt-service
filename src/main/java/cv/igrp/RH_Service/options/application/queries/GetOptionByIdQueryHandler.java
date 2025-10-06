package cv.igrp.RH_Service.options.application.queries;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import cv.igrp.RH_Service.options.application.dto.OptionResponseDTO;

@Component
public class GetOptionByIdQueryHandler implements QueryHandler<GetOptionByIdQuery, ResponseEntity<OptionResponseDTO>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(GetOptionByIdQueryHandler.class);


  public GetOptionByIdQueryHandler() {

  }

   @IgrpQueryHandler
  public ResponseEntity<OptionResponseDTO> handle(GetOptionByIdQuery query) {
    // TODO: Implement the query handling logic here
    return null;
  }

}