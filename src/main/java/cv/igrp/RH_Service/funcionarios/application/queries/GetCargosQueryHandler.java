package cv.igrp.RH_Service.funcionarios.application.queries;
import cv.igrp.RH_Service.funcionarios.domain.filter.CargoFilter;
import cv.igrp.RH_Service.funcionarios.domain.models.Cargo;
import cv.igrp.RH_Service.funcionarios.domain.repository.CargoRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.CargoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import cv.igrp.RH_Service.funcionarios.application.dto.WrapperListaCargoDTO;

import java.math.BigDecimal;
import java.util.List;

@Component
public class GetCargosQueryHandler implements QueryHandler<GetCargosQuery, ResponseEntity<WrapperListaCargoDTO>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(GetCargosQueryHandler.class);

  private final CargoRepository cargoRepository;
  private final CargoMapper cargoMapper;

  public GetCargosQueryHandler(CargoRepository cargoRepository, CargoMapper cargoMapper) {

    this.cargoRepository = cargoRepository;
    this.cargoMapper = cargoMapper;
  }

   @IgrpQueryHandler
  public ResponseEntity<WrapperListaCargoDTO> handle(GetCargosQuery query) {

     var filter = CargoFilter.builder()
         .nome(query.getNome())
         .codigo(query.getCodigo())
         .nivelHierarquico(query.getNivelHierarquico())
         .salarioBaseMin(query.getSalarioBaseMin() != null ? BigDecimal.valueOf(query.getSalarioBaseMin()) : null)
         .salarioBaseMax(query.getSalarioBaseMax() != null ? BigDecimal.valueOf(query.getSalarioBaseMax()) : null)
         .estado(query.getEstado()) // continua como string
         .pageNumber(Integer.parseInt(query.getPagina()))
         .pageSize(Integer.parseInt(query.getTamanho()))
         .build();

     List<Cargo> cargos = cargoRepository.getAll(filter);

     var contentDTO = cargos.stream()
         .map(cargoMapper::toDTO)
         .toList();

     WrapperListaCargoDTO response = new WrapperListaCargoDTO();
     response.setContent(contentDTO);
     response.setPageNumber(filter.getPageNumber());
     response.setPageSize(filter.getPageSize());
     response.setTotalElements((long) contentDTO.size());

     return ResponseEntity.ok(response);

  }

}
