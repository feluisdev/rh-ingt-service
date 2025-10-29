package cv.igrp.RH_Service.funcionarios.application.queries;
import cv.igrp.RH_Service.funcionarios.domain.repository.DepartamentoRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.DepartamentoMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
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

  private final DepartamentoRepository departamentoRepository;
  private final DepartamentoMapper departamentoMapper;

  public GetDepartamentoByIdQueryHandler(DepartamentoRepository departamentoRepository, DepartamentoMapper departamentoMapper) {

    this.departamentoRepository = departamentoRepository;
    this.departamentoMapper = departamentoMapper;
  }

   @IgrpQueryHandler
  public ResponseEntity<DepartamentoResponseDTO> handle(GetDepartamentoByIdQuery query) {
     var departamentoId = ExternalID.from(query.getDepartamentoId());

     var departamento = departamentoRepository.getById(departamentoId)
         .orElseThrow(() -> IgrpResponseStatusException.notFound(
             "Departamento não encontrado com ID: " + departamentoId.getStringValor()
         ));

     var responseDto = departamentoMapper.toDTO(departamento);

     return ResponseEntity.ok(responseDto);
  }

}
