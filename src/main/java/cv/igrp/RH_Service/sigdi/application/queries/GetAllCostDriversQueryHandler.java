package cv.igrp.RH_Service.sigdi.application.queries;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cv.igrp.RH_Service.sigdi.application.dto.CostDriverResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.WrapperCostDriverListDTO;
import cv.igrp.RH_Service.sigdi.domain.budget.models.CostDriver;
import cv.igrp.RH_Service.sigdi.domain.budget.repository.CostDriverRepository;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class GetAllCostDriversQueryHandler implements QueryHandler<GetAllCostDriversQuery, ResponseEntity<WrapperCostDriverListDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GetAllCostDriversQueryHandler.class);

  private final CostDriverRepository costDriverRepository;
  private final ObjectMapper objectMapper;

  public GetAllCostDriversQueryHandler(CostDriverRepository costDriverRepository, ObjectMapper objectMapper) {
    this.costDriverRepository = costDriverRepository;
    this.objectMapper = objectMapper;
  }

  @IgrpQueryHandler
  @Transactional(readOnly = true)
  public ResponseEntity<WrapperCostDriverListDTO> handle(GetAllCostDriversQuery query) {
    LOGGER.debug("GetAllCostDriversQuery: {}", query);

    List<CostDriver> all = costDriverRepository.findAll();

    int pageNumber = 0;
    int pageSize = 20;
    try {
      if (query.getPageNumber() != null) pageNumber = Integer.parseInt(query.getPageNumber());
      if (query.getPageSize() != null) pageSize = Integer.parseInt(query.getPageSize());
    } catch (NumberFormatException ignored) {}

    int fromIndex = Math.min(pageNumber * pageSize, all.size());
    int toIndex = Math.min(fromIndex + pageSize, all.size());
    List<CostDriver> paged = all.subList(fromIndex, toIndex);

    List<CostDriverResponseDTO> dtoList = new ArrayList<>();
    for (CostDriver driver : paged) {
      dtoList.add(toDto(driver));
    }

    WrapperCostDriverListDTO wrapper = new WrapperCostDriverListDTO();
    wrapper.setData(dtoList);
    wrapper.setPageNumber(pageNumber);
    wrapper.setPageSize(pageSize);
    wrapper.setTotalElements((long) all.size());
    wrapper.setTotalPages(pageSize > 0 ? (int) Math.ceil((double) all.size() / pageSize) : 0);

    return ResponseEntity.ok(wrapper);
  }

  private CostDriverResponseDTO toDto(CostDriver driver) {
    CostDriverResponseDTO dto = new CostDriverResponseDTO();
    dto.setId(driver.getId().getValor().getValor());
    dto.setDriverType(driver.getDriverType().getCode());
    dto.setValid_from(driver.getValidFrom());
    try {
      if (driver.getParameters() != null && driver.getParameters().toJson() != null) {
        Map<String, Object> params = objectMapper.readValue(driver.getParameters().toJson(), new TypeReference<Map<String, Object>>() {});
        dto.setParameters(params);
      }
    } catch (Exception e) {
      LOGGER.warn("Failed to parse driver parameters to map: {}", e.getMessage());
    }
    return dto;
  }
}