package cv.igrp.RH_Service.funcionarios.application.queries;

import cv.igrp.RH_Service.funcionarios.application.dto.DocumentoResponseDTO;
import cv.igrp.RH_Service.funcionarios.application.dto.TipoDocumentoResponseDTO;
import cv.igrp.RH_Service.funcionarios.application.dto.WrapperListaDocumentoDTO;
import cv.igrp.RH_Service.funcionarios.application.dto.WrapperListaTipoDocumentoDTO;
import cv.igrp.RH_Service.funcionarios.domain.filter.DocumentoFilter;
import cv.igrp.RH_Service.funcionarios.domain.filter.TipoDocumentoFilter;
import cv.igrp.RH_Service.funcionarios.domain.repository.DocumentoRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.DocumentoMapper;
import cv.igrp.RH_Service.shared.application.constants.Estado;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class GetDocumentosQueryHandler implements QueryHandler<GetDocumentosQuery, ResponseEntity<WrapperListaDocumentoDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GetDocumentosQueryHandler.class);

  private final DocumentoRepository documentoRepository;
  private final DocumentoMapper documentoMapper;

  public GetDocumentosQueryHandler(DocumentoRepository documentoRepository, DocumentoMapper documentoMapper) {

    this.documentoRepository = documentoRepository;
    this.documentoMapper = documentoMapper;
  }

  @IgrpQueryHandler
  public ResponseEntity<WrapperListaDocumentoDTO> handle(GetDocumentosQuery query) {
    // TODO: Implement the query handling logic here
    DocumentoFilter filter = DocumentoFilter.builder()
        .documentoId(query.getDocumentoId()!=null ? ExternalID.from(query.getDocumentoId()) : null)
        .idTipoDocumento(query.getIdTipoDocumento()!=null ? ExternalID.from(query.getIdTipoDocumento()) : null)
        .estado(query.getEstado()!=null ? Estado.fromCodeOrThrow(query.getEstado()) : null)
        .pageNumber(Integer.parseInt(query.getPagina()))
        .pageSize(Integer.parseInt(query.getTamanho()))
        .build();

    List<DocumentoResponseDTO> lista = documentoRepository.getAll(filter)
        .stream()
        .map(documentoMapper::toDTO)
        .toList();

    var wrapper = new WrapperListaDocumentoDTO();
    wrapper.setContent(lista);
    wrapper.setPageNumber(filter.getPageNumber());
    wrapper.setPageSize(filter.getPageSize());
    wrapper.setTotalElements((long) lista.size());
    return ResponseEntity.ok(wrapper);

  }

}
