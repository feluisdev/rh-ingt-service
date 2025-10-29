package cv.igrp.RH_Service.funcionarios.application.queries;

import cv.igrp.RH_Service.funcionarios.application.dto.TipoDocumentoResponseDTO;
import cv.igrp.RH_Service.funcionarios.application.dto.WrapperListaTipoDocumentoDTO;
import cv.igrp.RH_Service.funcionarios.domain.filter.TipoDocumentoFilter;
import cv.igrp.RH_Service.funcionarios.domain.repository.TipoDocumentoRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.TipoDocumentoMapper;
import cv.igrp.RH_Service.shared.application.constants.Estado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class GetTipoDocumentoQueryHandler implements QueryHandler<GetTipoDocumentoQuery, ResponseEntity<WrapperListaTipoDocumentoDTO>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(GetTipoDocumentoQueryHandler.class);

  private final TipoDocumentoRepository tipoDocumentoRepository;
  private final TipoDocumentoMapper tipoDocumentoMapper;

  public GetTipoDocumentoQueryHandler(TipoDocumentoRepository tipoDocumentoRepository, TipoDocumentoMapper tipoDocumentoMapper) {

    this.tipoDocumentoRepository = tipoDocumentoRepository;
    this.tipoDocumentoMapper = tipoDocumentoMapper;
  }

   @IgrpQueryHandler
  public ResponseEntity<WrapperListaTipoDocumentoDTO>  handle(GetTipoDocumentoQuery query) {
     LOGGER.info("Buscando TipoDocumento com filtros: codigo={}, descricao={}, pagina={}, tamanho={}",
         query.getCodigo(), query.getDescricao(), query.getPagina(), query.getTamanho());

     TipoDocumentoFilter filter = TipoDocumentoFilter.builder()
         .descricao(query.getDescricao())
         .codigo(query.getCodigo())
         .estado(query.getEstado()!=null ? Estado.fromCodeOrThrow(query.getEstado()) : null)
         .pageNumber(Integer.parseInt(query.getPagina()))
         .pageSize(Integer.parseInt(query.getTamanho()))
         .build();


     List<TipoDocumentoResponseDTO> lista = tipoDocumentoRepository.getAll(filter)
         .stream()
         .map(tipoDocumentoMapper::toDto)
         .toList();

     WrapperListaTipoDocumentoDTO wrapper = new WrapperListaTipoDocumentoDTO();
     wrapper.setContent(lista);
     wrapper.setPageNumber(filter.getPageNumber());
     wrapper.setPageSize(filter.getPageSize());
     wrapper.setTotalElements((long) lista.size());
     return ResponseEntity.ok(wrapper);
  }

}
