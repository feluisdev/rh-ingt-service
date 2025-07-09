package cv.igrp.RH_Service.funcionarios.application.queries;
import cv.igrp.RH_Service.funcionarios.application.dto.FuncionarioResponseDTO;
import cv.igrp.RH_Service.funcionarios.domain.filter.FuncionarioFilter;
import cv.igrp.RH_Service.funcionarios.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.FuncionarioMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import cv.igrp.RH_Service.funcionarios.application.dto.WrapperListaFuncionarioDTO;

import java.util.List;

@Component
public class GetFuncionariosQueryHandler implements QueryHandler<GetFuncionariosQuery, ResponseEntity<WrapperListaFuncionarioDTO>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(GetFuncionariosQueryHandler.class);

  private final FuncionarioMapper funcionarioMapper;
  private final FuncionarioRepository funcionarioRepository;

  public GetFuncionariosQueryHandler(FuncionarioMapper funcionarioMapper, FuncionarioRepository funcionarioRepository) {

    this.funcionarioMapper = funcionarioMapper;
    this.funcionarioRepository = funcionarioRepository;
  }

   @IgrpQueryHandler
  public ResponseEntity<WrapperListaFuncionarioDTO> handle(GetFuncionariosQuery query) {

    FuncionarioFilter filter = FuncionarioFilter.builder()
         .nome(query.getNome())
         .nif(query.getNif())
         .numSegurado(query.getNumSegurado())
         .email(query.getEmail())
         .pageNumber(Integer.parseInt(query.getPagina()))
         .pageSize(Integer.parseInt(query.getTamanho()))
         .build();

     List<FuncionarioResponseDTO> lista = funcionarioRepository.getAll(filter)
         .stream()
         .map(funcionarioMapper::toResponseDTO)
         .toList();


     WrapperListaFuncionarioDTO wrapper = new WrapperListaFuncionarioDTO();
     wrapper.setContent(lista);
     wrapper.setPageNumber(filter.getPageNumber());
     wrapper.setPageSize(filter.getPageSize());
     wrapper.setTotalElements((long) lista.size());
     return ResponseEntity.ok(wrapper);
  }

}
