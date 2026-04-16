package cv.igrp.RH_Service.sigdi.application.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.CostDriverType;
import cv.igrp.RH_Service.sigdi.application.dto.AdminCreateCostDriverRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.AdminCostDriverResponseDTO;
import cv.igrp.RH_Service.sigdi.domain.budget.models.CostDriver;
import cv.igrp.RH_Service.sigdi.domain.budget.repository.CostDriverRepository;
import cv.igrp.RH_Service.sigdi.domain.budget.valueobject.CostDriverParams;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Component
public class AdminCreateCostDriverCommandHandler
    implements CommandHandler<AdminCreateCostDriverCommand, ResponseEntity<AdminCostDriverResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AdminCreateCostDriverCommandHandler.class);

  private final CostDriverRepository costDriverRepository;
  private final ObjectMapper objectMapper;

  public AdminCreateCostDriverCommandHandler(CostDriverRepository costDriverRepository,
                                              ObjectMapper objectMapper) {
    this.costDriverRepository = costDriverRepository;
    this.objectMapper = objectMapper;
  }

  @IgrpCommandHandler
  @Transactional
  public ResponseEntity<AdminCostDriverResponseDTO> handle(AdminCreateCostDriverCommand command) {
    LOGGER.debug("AdminCreateCostDriverCommand: {}", command);

    AdminCreateCostDriverRequestDTO req = command.getBody();

    CostDriverType driverType = CostDriverType.fromCodeOrThrow(req.getDriverType());

    LocalDate validFrom;
    try {
      validFrom = LocalDate.parse(req.getValidFrom());
    } catch (DateTimeParseException e) {
      throw IgrpResponseStatusException.badRequest(
          "SIGDI-ADM-003: Invalid validFrom date format. Expected ISO-8601 (yyyy-MM-dd): " + req.getValidFrom());
    }

    String paramsJson;
    try {
      paramsJson = req.getParams() != null && !req.getParams().isEmpty()
          ? objectMapper.writeValueAsString(req.getParams())
          : "{}";
    } catch (JsonProcessingException e) {
      throw IgrpResponseStatusException.badRequest(
          "SIGDI-ADM-004: Failed to serialize params: " + e.getMessage());
    }

    CostDriverParams params = CostDriverParams.fromJson(driverType.getCode(), paramsJson);

    CostDriver costDriver = CostDriver.create(driverType, params, validFrom, null);
    CostDriver saved = costDriverRepository.save(costDriver);

    AdminCostDriverResponseDTO response = new AdminCostDriverResponseDTO();
    response.setId(saved.getId().getStringValor());
    response.setDriverType(saved.getDriverType().getCode());
    response.setValidFrom(saved.getValidFrom().toString());
    response.setCurrency(req.getCurrency());
    response.setLegalReference(req.getLegalReference());
    response.setCreatedAt(LocalDate.now().toString());
    response.setAffectedActivities(0);

    return ResponseEntity.status(201).body(response);
  }
}
