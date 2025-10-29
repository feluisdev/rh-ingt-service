package cv.igrp.RH_Service.funcionarios.application.queries;
import cv.igrp.RH_Service.funcionarios.domain.filter.DepartamentoFilter;
import cv.igrp.RH_Service.funcionarios.domain.repository.DepartamentoRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.DepartamentoMapper;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import cv.igrp.RH_Service.funcionarios.application.dto.WrapperListaDepartamentoDTO;

@Component
public class GetDepartamentosQueryHandler implements QueryHandler<GetDepartamentosQuery, ResponseEntity<WrapperListaDepartamentoDTO>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(GetDepartamentosQueryHandler.class);

  private final DepartamentoRepository departamentoRepository;
  private final DepartamentoMapper departamentoMapper;

  public GetDepartamentosQueryHandler(DepartamentoRepository departamentoRepository, DepartamentoMapper departamentoMapper) {

    this.departamentoRepository = departamentoRepository;
    this.departamentoMapper = departamentoMapper;
  }

   @IgrpQueryHandler
  public ResponseEntity<WrapperListaDepartamentoDTO> handle(GetDepartamentosQuery query) {
     DepartamentoFilter filter = DepartamentoFilter.builder()
         .nome(query.getNome() != null ? query.getNome() : null)
         .localizacao(query.getLocalizacao() != null ? query.getLocalizacao() : null)
         .codigo(query.getCodigo() != null ? query.getCodigo() : null)
         .estado(query.getEstado() != null ? query.getEstado() : null)
         .responsavelId(query.getResponsavelId() != null ? ExternalID.from(query.getResponsavelId()) : null)
         .pageNumber(Integer.parseInt(query.getPagina()))
         .pageSize(Integer.parseInt(query.getTamanho()))
         .build();

     var departamentos = departamentoRepository.getAllForRead(filter);

     var dtoList = departamentos.stream()
         .map(departamentoMapper::toDTO)
         .toList();

     WrapperListaDepartamentoDTO wrapper = new WrapperListaDepartamentoDTO();
     wrapper.setContent(dtoList);
     wrapper.setPageNumber(filter.getPageNumber());
     wrapper.setPageSize(filter.getPageSize());
     wrapper.setTotalElements((long) dtoList.size());


     return ResponseEntity.ok(wrapper);
  }

}
