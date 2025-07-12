package cv.igrp.RH_Service.funcionarios.application.queries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import cv.igrp.RH_Service.funcionarios.application.dto.WrapperListaDepartamentoDTO;

@Component
public class GetDepartamentosQueryHandler implements QueryHandler<GetDepartamentosQuery, ResponseEntity<WrapperListaDepartamentoDTO>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(GetDepartamentosQueryHandler.class);


  public GetDepartamentosQueryHandler() {

  }

   @IgrpQueryHandler
  public ResponseEntity<WrapperListaDepartamentoDTO> handle(GetDepartamentosQuery query) {
    // TODO: Implement the query handling logic here
    return null;
  }

}