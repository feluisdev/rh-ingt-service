package cv.igrp.RH_Service.funcionarios.application.queries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import java.util.List;
import cv.igrp.RH_Service.funcionarios.application.dto.ContratoResponseDTO;

@Component
public class GetContratosQueryHandler implements QueryHandler<GetContratosQuery, ResponseEntity<List<ContratoResponseDTO>>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(GetContratosQueryHandler.class);


  public GetContratosQueryHandler() {

  }

   @IgrpQueryHandler
  public ResponseEntity<List<ContratoResponseDTO>> handle(GetContratosQuery query) {
    // TODO: Implement the query handling logic here
    return null;
  }

}