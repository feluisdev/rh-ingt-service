package cv.igrp.RH_Service.funcionarios.application.queries;
import cv.igrp.RH_Service.funcionarios.domain.models.Contrato;
import cv.igrp.RH_Service.funcionarios.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.funcionarios.domain.repository.DependenteRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.ContratoMapper;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.DependenteMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


@Component
public class GetContratoQueryHandler implements QueryHandler<GetContratoQuery, ResponseEntity<String>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(GetContratoQueryHandler.class);


  private final ContratoRepository contratoRepository;
  private final ContratoMapper contratoMapper;

  public GetContratoQueryHandler(ContratoRepository contratoRepository, ContratoMapper contratoMapper) {

    this.contratoRepository = contratoRepository;
    this.contratoMapper = contratoMapper;
  }

   @IgrpQueryHandler
  public ResponseEntity<String> handle(GetContratoQuery query) {
    // TODO: Implement the query handling logic here
    return null;
  }

}
