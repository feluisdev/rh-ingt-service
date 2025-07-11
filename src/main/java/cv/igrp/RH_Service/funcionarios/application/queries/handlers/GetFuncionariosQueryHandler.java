package cv.igrp.RH_Service.funcionarios.application.queries.handlers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import cv.igrp.RH_Service.funcionarios.application.queries.queries.GetFuncionariosQuery;
import cv.igrp.RH_Service.funcionarios.application.dto.WrapperListaFuncionarioDTO;

@Service
public class GetFuncionariosQueryHandler implements QueryHandler<GetFuncionariosQuery, ResponseEntity<WrapperListaFuncionarioDTO>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(GetFuncionariosQueryHandler.class);


   public GetFuncionariosQueryHandler() {

   }

   @IgrpQueryHandler
   public ResponseEntity<WrapperListaFuncionarioDTO> handle(GetFuncionariosQuery query) {
      // TODO: Implement the query handling logic here
      return null;
   }

}