package cv.igrp.RH_Service.funcionarios.application.queries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import cv.igrp.RH_Service.funcionarios.application.dto.CargoResponseDTO;

@Component
public class GetCargoByIdQueryHandler implements QueryHandler<GetCargoByIdQuery, ResponseEntity<CargoResponseDTO>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(GetCargoByIdQueryHandler.class);


  public GetCargoByIdQueryHandler() {

  }

   @IgrpQueryHandler
  public ResponseEntity<CargoResponseDTO> handle(GetCargoByIdQuery query) {
    // TODO: Implement the query handling logic here
    return null;
  }

}