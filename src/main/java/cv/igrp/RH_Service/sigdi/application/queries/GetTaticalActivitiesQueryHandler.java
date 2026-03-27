package cv.igrp.RH_Service.sigdi.application.queries;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import cv.igrp.RH_Service.sigdi.application.dto.WrapperListTaticalActivityDTO;

@Component
public class GetTaticalActivitiesQueryHandler implements QueryHandler<GetTaticalActivitiesQuery, ResponseEntity<WrapperListTaticalActivityDTO>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(GetTaticalActivitiesQueryHandler.class);


  public GetTaticalActivitiesQueryHandler() {

  }

   @IgrpQueryHandler
  public ResponseEntity<WrapperListTaticalActivityDTO> handle(GetTaticalActivitiesQuery query) {

    LOGGER.debug("GetTaticalActivitiesQuery: {}", query);

    // TODO: Implement the query handling logic here
    return null;
  }

}