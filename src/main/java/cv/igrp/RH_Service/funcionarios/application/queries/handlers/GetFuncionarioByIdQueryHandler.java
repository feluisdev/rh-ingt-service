package cv.igrp.RH_Service.funcionarios.application.queries.handlers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import cv.igrp.RH_Service.funcionarios.application.queries.queries.GetFuncionarioByIdQuery;
import cv.igrp.RH_Service.funcionarios.application.dto.FuncionarioResponseDTO;

@Service
public class GetFuncionarioByIdQueryHandler implements QueryHandler<GetFuncionarioByIdQuery, ResponseEntity<FuncionarioResponseDTO>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(GetFuncionarioByIdQueryHandler.class);


   public GetFuncionarioByIdQueryHandler() {

   }

   @IgrpQueryHandler
   public ResponseEntity<FuncionarioResponseDTO> handle(GetFuncionarioByIdQuery query) {
      // TODO: Implement the query handling logic here
      return null;
   }

}