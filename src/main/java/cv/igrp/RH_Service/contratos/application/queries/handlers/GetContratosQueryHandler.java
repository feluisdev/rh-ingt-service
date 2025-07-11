package cv.igrp.RH_Service.contratos.application.queries.handlers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import cv.igrp.RH_Service.contratos.application.queries.queries.GetContratosQuery;


@Service
public class GetContratosQueryHandler implements QueryHandler<GetContratosQuery, ResponseEntity<String>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(GetContratosQueryHandler.class);


   public GetContratosQueryHandler() {

   }

   @IgrpQueryHandler
   public ResponseEntity<String> handle(GetContratosQuery query) {
      // TODO: Implement the query handling logic here
      return null;
   }

}