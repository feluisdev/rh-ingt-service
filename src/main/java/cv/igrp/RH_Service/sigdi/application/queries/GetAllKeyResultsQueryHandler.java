package cv.igrp.RH_Service.sigdi.application.queries;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import cv.igrp.RH_Service.sigdi.application.dto.KeyResultResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.WrapperKeyResultListDTO;
import cv.igrp.RH_Service.sigdi.domain.tatical.filter.KeyResultFilter;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.KeyResult;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.KeyResultRepository;

@Component
public class GetAllKeyResultsQueryHandler
    implements QueryHandler<GetAllKeyResultsQuery, ResponseEntity<WrapperKeyResultListDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GetAllKeyResultsQueryHandler.class);

  private final KeyResultRepository repository;

  public GetAllKeyResultsQueryHandler(KeyResultRepository repository) {
    this.repository = repository;
  }

  @IgrpQueryHandler
  public ResponseEntity<WrapperKeyResultListDTO> handle(GetAllKeyResultsQuery query) {

    LOGGER.debug("GetAllKeyResultsQuery: {}", query);

    int pageNumber = parsePageNumber(query.getPageNumber());
    int pageSize = parsePageSize(query.getPageSize());

    KeyResultFilter filter = KeyResultFilter.builder()
        .pageNumber(pageNumber)
        .pageSize(pageSize)
        .build();

    var page = repository.findAll(filter);

    WrapperKeyResultListDTO response = new WrapperKeyResultListDTO();
    response.setPageNumber(page.getPageNumber());
    response.setPageSize(page.getPageSize());
    response.setTotalElements(page.getTotalElements());
    response.setTotalPages(page.getTotalPages());
    response.setFirst(page.isFirst());
    response.setLast(page.isLast());
    response.setData(page.getData().stream().map(this::toResponse).toList());

    return ResponseEntity.ok(response);
  }

  private int parsePageNumber(String value) {
    try {
      int parsed = Integer.parseInt(value);
      if (parsed < 0)
        throw new NumberFormatException("pageNumber must be >= 0");
      return parsed;
    } catch (Exception e) {
      throw IgrpResponseStatusException.badRequest("pageNumber inválido");
    }
  }

  private int parsePageSize(String value) {
    try {
      int parsed = Integer.parseInt(value);
      if (parsed <= 0)
        throw new NumberFormatException("pageSize must be > 0");
      return parsed;
    } catch (Exception e) {
      throw IgrpResponseStatusException.badRequest("pageSize inválido");
    }
  }

  private KeyResultResponseDTO toResponse(KeyResult kr) {
    KeyResultResponseDTO dto = new KeyResultResponseDTO();
    dto.setId(kr.getId().getValor().getValor());
    dto.setTitle(kr.getTitle());
    dto.setTargetValue(kr.getTargetValue());
    dto.setCurrentValue(kr.getCurrentValue());
    dto.setMetricUnit(kr.getMetricUnit() != null ? kr.getMetricUnit().getCode() : null);
    dto.setActivityId(kr.getActivityId() != null ? kr.getActivityId().getValor().getValor() : null);
    return dto;
  }

}
