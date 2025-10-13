package cv.igrp.RH_Service.funcionarios.application.queries;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import cv.igrp.RH_Service.funcionarios.application.dto.WrapperListaOptionDTO;
import cv.igrp.RH_Service.funcionarios.application.dto.OptionResponseDTO;
import cv.igrp.RH_Service.funcionarios.domain.filter.OptionFilter;
import cv.igrp.RH_Service.funcionarios.domain.repository.OptionRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.OptionMapper;


@Component
public class GetOptionsQueryHandler implements QueryHandler<GetOptionsQuery, ResponseEntity<WrapperListaOptionDTO>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(GetOptionsQueryHandler.class);

  private final OptionRepository optionRepository;
  private final OptionMapper optionMapper;

  public GetOptionsQueryHandler(OptionRepository optionRepository, OptionMapper optionMapper) {
    this.optionRepository = optionRepository;
    this.optionMapper = optionMapper;
  }

   @IgrpQueryHandler
  public ResponseEntity<WrapperListaOptionDTO> handle(GetOptionsQuery query) {
    OptionFilter filter = OptionFilter.builder()
        .pageNumber(0)
        .pageSize(20)
        .build();

    List<OptionResponseDTO> lista = optionRepository.getAll(filter)
        .stream()
        .map(optionMapper::toDTO)
        .toList();

    WrapperListaOptionDTO wrapper = new WrapperListaOptionDTO();
    wrapper.setPageNumber(filter.getPageNumber());
    wrapper.setPageSize(filter.getPageSize());
    wrapper.setTotalElements((long) lista.size());
    if (!lista.isEmpty()) {
      wrapper.setContent(lista);
    }
    return ResponseEntity.ok(wrapper);
  }

}
