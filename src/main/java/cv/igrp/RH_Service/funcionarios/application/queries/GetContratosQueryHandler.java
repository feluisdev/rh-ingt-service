package cv.igrp.RH_Service.funcionarios.application.queries;
import cv.igrp.RH_Service.funcionarios.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.funcionarios.domain.repository.DependenteRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.ContratoMapper;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.DependenteMapper;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
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

  private final ContratoRepository contratoRepository;
  private final ContratoMapper contratoMapper;

  public GetContratosQueryHandler(ContratoRepository contratoRepository, ContratoMapper contratoMapper) {

    this.contratoRepository = contratoRepository;
    this.contratoMapper = contratoMapper;
  }

   @IgrpQueryHandler
  public ResponseEntity<List<ContratoResponseDTO>> handle(GetContratosQuery query) {
     var funcionarioId = ExternalID.from(query.getFuncionarioId());
     var contratos = contratoRepository.getAllByFuncionariolId(funcionarioId);

     var listaDTO = contratos.stream()
         .map(contratoMapper::toDTO)
         .toList();

     return ResponseEntity.ok(listaDTO);
  }

}
