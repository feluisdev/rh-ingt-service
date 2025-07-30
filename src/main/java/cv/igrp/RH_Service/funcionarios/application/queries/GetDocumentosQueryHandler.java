package cv.igrp.RH_Service.funcionarios.application.queries;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;



@Component
public class GetDocumentosQueryHandler implements QueryHandler<GetDocumentosQuery, ResponseEntity<String>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(GetDocumentosQueryHandler.class);


  public GetDocumentosQueryHandler() {

  }

   @IgrpQueryHandler
  public ResponseEntity<String> handle(GetDocumentosQuery query) {
    // TODO: Implement the query handling logic here
    return null;
  }

}