package cv.igrp.RH_Service.sigdi.application.queries;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.dto.CostDriverResponseDTO;
import cv.igrp.RH_Service.sigdi.domain.budget.models.CostDriver;
import cv.igrp.RH_Service.sigdi.domain.budget.repository.CostDriverRepository;
import cv.igrp.RH_Service.sigdi.domain.budget.valueobject.CostDriverId;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component
public class GetCostDriverQueryHandler implements QueryHandler<GetCostDriverQuery, ResponseEntity<CostDriverResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GetCostDriverQueryHandler.class);

  private final CostDriverRepository costDriverRepository;
  private final ObjectMapper objectMapper;

  public GetCostDriverQueryHandler(CostDriverRepository costDriverRepository, ObjectMapper objectMapper) {
    this.costDriverRepository = costDriverRepository;
    this.objectMapper = objectMapper;
  }

  @IgrpQueryHandler
  @Transactional(readOnly = true)
  public ResponseEntity<CostDriverResponseDTO> handle(GetCostDriverQuery query) {
    LOGGER.debug("GetCostDriverQuery: {}", query);

    CostDriverId id;
    try {
      id = CostDriverId.from(query.getId());
    } catch (IllegalArgumentException e) {
      throw IgrpResponseStatusException.badRequest("Invalid CostDriver ID: " + query.getId());
    }

    CostDriver driver = costDriverRepository.findById(id)
        .orElseThrow(() -> IgrpResponseStatusException.notFound("CostDriver not found: " + query.getId()));

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
      LOGGER.warn("Failed to parse driver parameters: {}", e.getMessage());
    }

    return ResponseEntity.ok(dto);
  }
}