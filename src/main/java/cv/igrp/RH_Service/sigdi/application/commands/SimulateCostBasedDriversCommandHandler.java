package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.CostDriverType;
import cv.igrp.RH_Service.sigdi.application.dto.CostDriverSimulateResponseDTO;
import cv.igrp.RH_Service.sigdi.domain.budget.models.CostDriver;
import cv.igrp.RH_Service.sigdi.domain.budget.repository.CostDriverRepository;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class SimulateCostBasedDriversCommandHandler
    implements CommandHandler<SimulateCostBasedDriversCommand, ResponseEntity<CostDriverSimulateResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(SimulateCostBasedDriversCommandHandler.class);

  private final CostDriverRepository repository;

  public SimulateCostBasedDriversCommandHandler(CostDriverRepository repository) {
    this.repository = repository;
  }

  @IgrpCommandHandler
  public ResponseEntity<CostDriverSimulateResponseDTO> handle(SimulateCostBasedDriversCommand command) {
    LOGGER.debug("SimulateCostBasedDriversCommand: {}", command);

    var req = command.getCostdriversimulatereq();
    CostDriverType type = CostDriverType.fromCodeOrThrow(req.getDriverType());

    CostDriver driver = repository.findActiveByType(type, LocalDate.now())
        .orElseThrow(() -> IgrpResponseStatusException.notFound(
            "No active CostDriver found for type: " + type.getCode()));

    Map<String, Object> params = req.getParams() != null ? req.getParams() : new HashMap<>();
    int quantity = parseQuantity(params);

    BigDecimal totalCost = driver.simulate(quantity);
    BigDecimal unitCost = driver.getParameters().calculate(1);

    Map<String, Object> breakdown = new HashMap<>(params);
    breakdown.put("quantity", quantity);
    breakdown.put("unitCost", unitCost);

    CostDriverSimulateResponseDTO response = new CostDriverSimulateResponseDTO();
    response.setDriverType(type.getCode());
    response.setUnitCost(unitCost);
    response.setCurrency("CVE");
    response.setTotalCost(totalCost);
    response.setDriverVersion(driver.getValidFrom().toString());
    response.setDriverSource("Internal CostDriver Registry");
    response.setCalculatedAt(LocalDateTime.now().toString());
    response.setParams(params);
    response.setBreakdown(breakdown);

    return ResponseEntity.ok(response);
  }

  private int parseQuantity(Map<String, Object> params) {
    Object raw = params.get("quantity");
    if (raw == null) raw = params.get("days");
    if (raw == null) raw = params.get("km");
    if (raw == null) raw = params.get("persons");
    if (raw == null) return 1;
    try {
      return Integer.parseInt(raw.toString());
    } catch (NumberFormatException e) {
      throw IgrpResponseStatusException.badRequest("'quantity' param must be a valid integer");
    }
  }
}
