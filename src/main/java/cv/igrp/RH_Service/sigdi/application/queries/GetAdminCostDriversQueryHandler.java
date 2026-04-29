package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.RH_Service.sigdi.application.dto.AdminCostDriverResponseDTO;
import cv.igrp.RH_Service.sigdi.domain.budget.models.CostDriver;
import cv.igrp.RH_Service.sigdi.domain.budget.repository.CostDriverRepository;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GetAdminCostDriversQueryHandler
    implements QueryHandler<GetAdminCostDriversQuery, ResponseEntity<List<AdminCostDriverResponseDTO>>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GetAdminCostDriversQueryHandler.class);

  private final CostDriverRepository costDriverRepository;

  public GetAdminCostDriversQueryHandler(CostDriverRepository costDriverRepository) {
    this.costDriverRepository = costDriverRepository;
  }

  @IgrpQueryHandler
  @Transactional(readOnly = true)
  public ResponseEntity<List<AdminCostDriverResponseDTO>> handle(GetAdminCostDriversQuery query) {
    LOGGER.debug("GetAdminCostDriversQuery: {}", query);

    List<CostDriver> drivers = costDriverRepository.findAll();

    List<AdminCostDriverResponseDTO> result = drivers.stream()
        .filter(driver -> {
          if (query.getDriverType() != null && !query.getDriverType().isBlank()) {
            return driver.getDriverType().getCode().equalsIgnoreCase(query.getDriverType());
          }
          return true;
        })
        .sorted((a, b) -> b.getValidFrom().compareTo(a.getValidFrom()))
        .map(driver -> {
          AdminCostDriverResponseDTO dto = new AdminCostDriverResponseDTO();
          dto.setId(driver.getId().getStringValor());
          dto.setDriverType(driver.getDriverType().getCode());
          dto.setValidFrom(driver.getValidFrom().toString());
          dto.setCurrency(driver.getCurrency());
          dto.setLegalReference(driver.getLegalReference());
          dto.setCreatedAt(null);
          dto.setAffectedActivities(0);
          return dto;
        })
        .collect(Collectors.toList());

    return ResponseEntity.ok(result);
  }
}
