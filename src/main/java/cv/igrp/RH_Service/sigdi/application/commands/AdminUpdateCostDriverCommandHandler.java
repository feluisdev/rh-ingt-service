package cv.igrp.RH_Service.sigdi.application.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.dto.AdminCreateCostDriverRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.AdminCostDriverResponseDTO;
import cv.igrp.RH_Service.sigdi.domain.budget.models.CostDriver;
import cv.igrp.RH_Service.sigdi.domain.budget.repository.CostDriverRepository;
import cv.igrp.RH_Service.sigdi.domain.budget.valueobject.CostDriverId;
import cv.igrp.RH_Service.sigdi.domain.budget.valueobject.CostDriverParams;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Component
public class AdminUpdateCostDriverCommandHandler
    implements CommandHandler<AdminUpdateCostDriverCommand, ResponseEntity<AdminCostDriverResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AdminUpdateCostDriverCommandHandler.class);

  private final CostDriverRepository costDriverRepository;
  private final ObjectMapper objectMapper;

  public AdminUpdateCostDriverCommandHandler(CostDriverRepository costDriverRepository,
                                              ObjectMapper objectMapper) {
    this.costDriverRepository = costDriverRepository;
    this.objectMapper = objectMapper;
  }

  @IgrpCommandHandler
  @Transactional
  public ResponseEntity<AdminCostDriverResponseDTO> handle(AdminUpdateCostDriverCommand command) {
    LOGGER.debug("AdminUpdateCostDriverCommand: {}", command);

    CostDriverId costDriverId;
    try {
      costDriverId = CostDriverId.from(command.getId());
    } catch (IllegalArgumentException e) {
      throw IgrpResponseStatusException.badRequest(
          "SIGDI-ADM-010: Invalid cost driver id: " + command.getId());
    }

    CostDriver existing = costDriverRepository.findById(costDriverId)
        .orElseThrow(() -> IgrpResponseStatusException.of(
            HttpStatus.NOT_FOUND,
            "SIGDI-ADM-011: Cost driver not found for id: " + command.getId()));

    AdminCreateCostDriverRequestDTO req = command.getBody();

    LocalDate validFrom = existing.getValidFrom();
    if (req.getValidFrom() != null && !req.getValidFrom().isBlank()) {
      try {
        validFrom = LocalDate.parse(req.getValidFrom());
      } catch (DateTimeParseException e) {
        throw IgrpResponseStatusException.badRequest(
            "SIGDI-ADM-003: Invalid validFrom date format. Expected ISO-8601 (yyyy-MM-dd): " + req.getValidFrom());
      }
    }

    String paramsJson;
    try {
      paramsJson = req.getParams() != null && !req.getParams().isEmpty()
          ? objectMapper.writeValueAsString(req.getParams())
          : existing.getParameters().toJson();
    } catch (JsonProcessingException e) {
      throw IgrpResponseStatusException.badRequest(
          "SIGDI-ADM-004: Failed to serialize params: " + e.getMessage());
    }

    CostDriverParams params = CostDriverParams.fromJson(existing.getDriverType().getCode(), paramsJson);

    String currency = (req.getCurrency() != null && !req.getCurrency().isBlank())
        ? req.getCurrency()
        : existing.getCurrency();

    CostDriver updated = existing.update(params, validFrom, currency, req.getLegalReference());
    CostDriver saved = costDriverRepository.update(updated);

    AdminCostDriverResponseDTO response = new AdminCostDriverResponseDTO();
    response.setId(saved.getId().getStringValor());
    response.setDriverType(saved.getDriverType().getCode());
    response.setValidFrom(saved.getValidFrom().toString());
    response.setCurrency(saved.getCurrency());
    response.setLegalReference(saved.getLegalReference());
    response.setCreatedAt(LocalDate.now().toString());
    response.setAffectedActivities(0);

    return ResponseEntity.ok(response);
  }
}
