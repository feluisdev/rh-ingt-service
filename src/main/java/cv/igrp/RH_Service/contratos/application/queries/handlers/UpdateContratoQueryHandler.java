package cv.igrp.RH_Service.contratos.application.queries.handlers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import cv.igrp.RH_Service.contratos.application.queries.queries.UpdateContratoQuery;


@Service
public class UpdateContratoQueryHandler implements QueryHandler<UpdateContratoQuery, ResponseEntity<String>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(UpdateContratoQueryHandler.class);


   public UpdateContratoQueryHandler() {

   }

   @IgrpQueryHandler
   public ResponseEntity<String> handle(UpdateContratoQuery query) {
      // TODO: Implement the query handling logic here
      return null;
   }

}