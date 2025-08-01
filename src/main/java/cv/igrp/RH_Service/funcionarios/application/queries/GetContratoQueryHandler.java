package cv.igrp.RH_Service.funcionarios.application.queries;
import cv.igrp.RH_Service.funcionarios.application.dto.ContratoResponseDTO;
import cv.igrp.RH_Service.funcionarios.domain.models.Contrato;
import cv.igrp.RH_Service.funcionarios.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.funcionarios.domain.repository.DependenteRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.ContratoMapper;
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


@Component
public class GetContratoQueryHandler implements QueryHandler<GetContratoQuery, ResponseEntity<ContratoResponseDTO>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(GetContratoQueryHandler.class);


  private final ContratoRepository contratoRepository;
  private final ContratoMapper contratoMapper;

  public GetContratoQueryHandler(ContratoRepository contratoRepository, ContratoMapper contratoMapper) {

    this.contratoRepository = contratoRepository;
    this.contratoMapper = contratoMapper;
  }

   @IgrpQueryHandler
  public ResponseEntity<ContratoResponseDTO> handle(GetContratoQuery query) {
     var contratoId = ExternalID.from(query.getContratoId());
     var funcionarioId = ExternalID.from(query.getFuncionarioId());

     var contrato = contratoRepository.getById(contratoId)
         .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.NOT_FOUND, "Contrato não encontrado: " + contratoId));

     if (!contrato.getFuncionario().getIdFuncionario().equals(funcionarioId)) {
       throw IgrpResponseStatusException.of(HttpStatus.FORBIDDEN, "Contrato não pertence ao funcionário informado.");
     }

     var dto = contratoMapper.toDTO(contrato);
     return ResponseEntity.ok(dto);
  }

}
