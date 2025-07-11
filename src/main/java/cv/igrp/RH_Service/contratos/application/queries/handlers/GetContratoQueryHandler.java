package cv.igrp.RH_Service.contratos.application.queries.handlers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import cv.igrp.RH_Service.contratos.application.queries.queries.GetContratoQuery;


@Service
public class GetContratoQueryHandler implements QueryHandler<GetContratoQuery, ResponseEntity<String>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(GetContratoQueryHandler.class);


   public GetContratoQueryHandler() {

   }

   @IgrpQueryHandler
   public ResponseEntity<String> handle(GetContratoQuery query) {
      // TODO: Implement the query handling logic here
      return null;
   }

}