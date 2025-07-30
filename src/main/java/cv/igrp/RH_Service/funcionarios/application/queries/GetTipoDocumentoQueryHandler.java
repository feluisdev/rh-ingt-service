package cv.igrp.RH_Service.funcionarios.application.queries;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;



@Component
public class GetTipoDocumentoQueryHandler implements QueryHandler<GetTipoDocumentoQuery, ResponseEntity<String>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(GetTipoDocumentoQueryHandler.class);


  public GetTipoDocumentoQueryHandler() {

  }

   @IgrpQueryHandler
  public ResponseEntity<String> handle(GetTipoDocumentoQuery query) {
    // TODO: Implement the query handling logic here
    return null;
  }

}