package cv.igrp.RH_Service.options.application.queries;

import cv.igrp.RH_Service.options.domain.filter.OptionFilter;
import cv.igrp.RH_Service.options.domain.repository.OptionRepository;
import cv.igrp.RH_Service.options.infrastructure.mappers.OptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import cv.igrp.RH_Service.options.application.dto.WrapperListOptionsDTO;

@Component
public class GetListOptionsQueryHandler implements QueryHandler<GetListOptionsQuery, ResponseEntity<WrapperListOptionsDTO>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(GetListOptionsQueryHandler.class);
  private final OptionRepository optionRepository;
  private final OptionMapper optionMapper;

  public GetListOptionsQueryHandler(OptionRepository optionRepository, OptionMapper optionMapper) {
    this.optionRepository = optionRepository;
    this.optionMapper = optionMapper;
  }

  @IgrpQueryHandler
  public ResponseEntity<WrapperListOptionsDTO> handle(GetListOptionsQuery query) {

    LOGGER.info("Handling GetListOptionsQuery with filters: code={}, key={}, value={}", query.getCcode(), query.getCkey(), query.getCvalue());

    var filter = OptionFilter.builder()
        .ccode(query.getCcode())
        .ckey(query.getCkey())
        .cvalue(query.getCvalue())
        //.locale(query.getLocale() != null ? query.getLocale() : "pt-CV")
        .locale(query.getLocale() )
        .sortOrder(query.getSortOrder())
        .active(query.isActive())
        .pageNumber(query.getPageNumber() != null ? Integer.parseInt(query.getPageNumber()) : 0)
        .pageSize(query.getPageSize() != null ? Integer.parseInt(query.getPageSize()) : 20)
        .build();

    var options = optionRepository.findAll(filter);

    var content = options.stream()
        .map(optionMapper::toResponseDTO)
        .toList();

    WrapperListOptionsDTO wrapper = new WrapperListOptionsDTO();
    wrapper.setContent(content);
    wrapper.setPageNumber(filter.getPageNumber());
    wrapper.setPageSize(filter.getPageSize());
    wrapper.setTotalElements((long) content.size());
    wrapper.setTotalPages((int) Math.ceil((double) content.size() / filter.getPageSize()));
    wrapper.setFirst(filter.getPageNumber() == 0);
    wrapper.setLast(content.size() < filter.getPageSize());

    return ResponseEntity.ok(wrapper);
  }

}
