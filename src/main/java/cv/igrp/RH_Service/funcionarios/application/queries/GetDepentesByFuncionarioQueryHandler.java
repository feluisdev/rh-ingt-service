package cv.igrp.RH_Service.funcionarios.application.queries;
import cv.igrp.RH_Service.funcionarios.domain.repository.DependenteRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.DependenteMapper;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import cv.igrp.RH_Service.funcionarios.application.dto.DependenteResponseDTO;

import java.util.List;

@Component
public class GetDepentesByFuncionarioQueryHandler implements QueryHandler<GetDepentesByFuncionarioQuery,  ResponseEntity<List<DependenteResponseDTO>>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(GetDepentesByFuncionarioQueryHandler.class);

  private final DependenteRepository dependenteRepository;
  private final DependenteMapper dependenteMapper;

  public GetDepentesByFuncionarioQueryHandler(DependenteRepository dependenteRepository, DependenteMapper dependenteMapper) {

    this.dependenteRepository = dependenteRepository;
    this.dependenteMapper = dependenteMapper;
  }

   @IgrpQueryHandler
  public  ResponseEntity<List<DependenteResponseDTO>> handle(GetDepentesByFuncionarioQuery query) {
     var funcionarioId = ExternalID.from(query.getFuncionarioId());
     var dependentes = dependenteRepository.getAllByFuncionarioId(funcionarioId);

     var listaDTO = dependentes.stream()
         .map(dependenteMapper::toResponseDTO)
         .toList();

     return ResponseEntity.ok(listaDTO);
  }

}
