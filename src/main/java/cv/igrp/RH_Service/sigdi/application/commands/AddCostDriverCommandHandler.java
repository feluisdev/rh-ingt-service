package cv.igrp.RH_Service.sigdi.application.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.CostDriverType;
import cv.igrp.RH_Service.sigdi.application.dto.CostDriverRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.CostDriverResponseDTO;
import cv.igrp.RH_Service.sigdi.domain.budget.models.CostDriver;
import cv.igrp.RH_Service.sigdi.domain.budget.repository.CostDriverRepository;
import cv.igrp.RH_Service.sigdi.domain.budget.valueobject.CostDriverParams;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AddCostDriverCommandHandler
    implements CommandHandler<AddCostDriverCommand, ResponseEntity<CostDriverResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AddCostDriverCommandHandler.class);

  private final CostDriverRepository costDriverRepository;
  private final ObjectMapper objectMapper;

  public AddCostDriverCommandHandler(CostDriverRepository costDriverRepository,
                                     ObjectMapper objectMapper) {
    this.costDriverRepository = costDriverRepository;
    this.objectMapper = objectMapper;
  }

  @IgrpCommandHandler
  @Transactional
  public ResponseEntity<CostDriverResponseDTO> handle(AddCostDriverCommand command) {
    LOGGER.debug("AddCostDriverCommand: {}", command);

    CostDriverRequestDTO req = command.getCostdriverrequest();

    CostDriverType driverType = CostDriverType.fromCodeOrThrow(req.getDriverType());

    if (req.getValid_from() == null) {
      throw IgrpResponseStatusException.badRequest("SIGDI-BUD-003: valid_from é obrigatório");
    }

    String paramsJson;
    try {
      paramsJson = (req.getParameters() != null && !req.getParameters().isEmpty())
          ? objectMapper.writeValueAsString(req.getParameters())
          : "{}";
    } catch (JsonProcessingException e) {
      throw IgrpResponseStatusException.badRequest(
          "SIGDI-BUD-004: Failed to serialize parameters: " + e.getMessage());
    }

    CostDriverParams params = CostDriverParams.fromJson(driverType.getCode(), paramsJson);

    CostDriver costDriver = CostDriver.create(driverType, params, req.getValid_from(), null, null);
    CostDriver saved = costDriverRepository.save(costDriver);

    CostDriverResponseDTO response = new CostDriverResponseDTO();
    response.setId(saved.getId().getValor().getValor());
    response.setDriverType(saved.getDriverType().getCode());
    response.setParameters(req.getParameters());
    response.setValid_from(saved.getValidFrom());

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}
