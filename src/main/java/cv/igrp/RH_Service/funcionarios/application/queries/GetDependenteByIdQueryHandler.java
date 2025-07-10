package cv.igrp.RH_Service.funcionarios.application.queries;
import cv.igrp.RH_Service.funcionarios.domain.repository.DependenteRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.DependenteMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import cv.igrp.RH_Service.funcionarios.application.dto.DependenteResponseDTO;

@Component
public class GetDependenteByIdQueryHandler implements QueryHandler<GetDependenteByIdQuery, ResponseEntity<DependenteResponseDTO>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(GetDependenteByIdQueryHandler.class);

  private final DependenteRepository dependenteRepository;
  private final DependenteMapper dependenteMapper;

  public GetDependenteByIdQueryHandler(DependenteRepository dependenteRepository, DependenteMapper dependenteMapper) {

    this.dependenteRepository = dependenteRepository;
    this.dependenteMapper = dependenteMapper;
  }

   @IgrpQueryHandler
  public ResponseEntity<DependenteResponseDTO> handle(GetDependenteByIdQuery query) {

     var dependenteId = ExternalID.from(query.getDependenteId());
     var funcionarioId = ExternalID.from(query.getFuncionarioId());

     var dependente = dependenteRepository.getByExternalId(dependenteId)
         .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.NOT_FOUND, "Dependente não encontrado: " + dependenteId));

     if (!dependente.getFuncionario().getExternalId().equals(funcionarioId)) {
       throw IgrpResponseStatusException.of(HttpStatus.FORBIDDEN, "Dependente não pertence ao funcionário informado.");
     }

     var dto = dependenteMapper.toResponseDTO(dependente);
     return ResponseEntity.ok(dto);
  }

}
