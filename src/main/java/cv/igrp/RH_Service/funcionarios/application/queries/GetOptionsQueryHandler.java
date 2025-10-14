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

    var active = true;

     String activeStr = query.getActive();

     if (activeStr != null && !activeStr.isEmpty()) {
       activeStr = activeStr.trim().toLowerCase();

       if (activeStr.equals("true") || activeStr.equals("1") || activeStr.equals("yes") || activeStr.equals("sim")) {
         active = true;
       } else if (activeStr.equals("false") || activeStr.equals("0") || activeStr.equals("no") || activeStr.equals("nao")) {
         active = false;
       }
     }
     
    OptionFilter filter = OptionFilter.builder()
        .ccode(query.getCcode())
        .ckey(query.getCkey())
        .cvalue(query.getCvalue())
        .locale(query.getLocale() != null ? query.getLocale() : "pt-CV")
        //.locale(query.getLocale() )
        .active(active)
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
