package cv.igrp.RH_Service.funcionarios.application.queries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import cv.igrp.RH_Service.funcionarios.application.dto.DepartamentoResponseDTO;

@Component
public class GetDepartamentoByIdQueryHandler implements QueryHandler<GetDepartamentoByIdQuery, ResponseEntity<DepartamentoResponseDTO>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(GetDepartamentoByIdQueryHandler.class);


  public GetDepartamentoByIdQueryHandler() {

  }

   @IgrpQueryHandler
  public ResponseEntity<DepartamentoResponseDTO> handle(GetDepartamentoByIdQuery query) {
    // TODO: Implement the query handling logic here
    return null;
  }

}