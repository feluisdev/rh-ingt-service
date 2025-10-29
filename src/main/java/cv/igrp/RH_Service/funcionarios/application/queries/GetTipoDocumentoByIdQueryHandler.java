package cv.igrp.RH_Service.funcionarios.application.queries;

import cv.igrp.RH_Service.funcionarios.domain.repository.TipoDocumentoRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.TipoDocumentoMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import cv.igrp.RH_Service.funcionarios.application.dto.TipoDocumentoResponseDTO;

@Component
public class GetTipoDocumentoByIdQueryHandler implements QueryHandler<GetTipoDocumentoByIdQuery, ResponseEntity<TipoDocumentoResponseDTO>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(GetTipoDocumentoByIdQueryHandler.class);

  private final TipoDocumentoRepository tipoDocumentoRepository;
  private final TipoDocumentoMapper documentoMapper;

  public GetTipoDocumentoByIdQueryHandler(TipoDocumentoRepository tipoDocumentoRepository, TipoDocumentoMapper documentoMapper) {

    this.tipoDocumentoRepository = tipoDocumentoRepository;
    this.documentoMapper = documentoMapper;
  }

   @IgrpQueryHandler
  public ResponseEntity<TipoDocumentoResponseDTO> handle(GetTipoDocumentoByIdQuery query) {
    var idTipoDocumento = query.getTipoDocumentoId();
     LOGGER.info("Buscando TipoDocumento com externalId: {}", idTipoDocumento);

     var tipoDocumento = tipoDocumentoRepository
         .getById(ExternalID.from(idTipoDocumento))
         .orElseThrow(() -> IgrpResponseStatusException.notFound("Tipo de Documento não encontrado"));

     var responseDTO = documentoMapper.toDto(tipoDocumento);

     return ResponseEntity.ok(responseDTO);
  }

}
