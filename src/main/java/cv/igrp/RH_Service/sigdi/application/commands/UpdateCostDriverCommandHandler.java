package cv.igrp.RH_Service.sigdi.application.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.CostDriverType;
import cv.igrp.RH_Service.sigdi.application.dto.CostDriverRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.CostDriverResponseDTO;
import cv.igrp.RH_Service.sigdi.domain.budget.models.CostDriver;
import cv.igrp.RH_Service.sigdi.domain.budget.repository.CostDriverRepository;
import cv.igrp.RH_Service.sigdi.domain.budget.valueobject.CostDriverId;
import cv.igrp.RH_Service.sigdi.domain.budget.valueobject.CostDriverParams;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UpdateCostDriverCommandHandler implements CommandHandler<UpdateCostDriverCommand, ResponseEntity<CostDriverResponseDTO>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(UpdateCostDriverCommandHandler.class);

   private final CostDriverRepository costDriverRepository;
   private final ObjectMapper objectMapper;

   public UpdateCostDriverCommandHandler(CostDriverRepository costDriverRepository, ObjectMapper objectMapper) {
      this.costDriverRepository = costDriverRepository;
      this.objectMapper = objectMapper;
   }

   @IgrpCommandHandler
   @Transactional
   public ResponseEntity<CostDriverResponseDTO> handle(UpdateCostDriverCommand command) {
      LOGGER.debug("UpdateCostDriverCommand : {}", command);

      CostDriverId id;
      try {
         id = CostDriverId.from(command.getId());
      } catch (IllegalArgumentException e) {
         throw IgrpResponseStatusException.badRequest("Invalid CostDriver ID: " + command.getId());
      }

      CostDriver existing = costDriverRepository.findById(id)
          .orElseThrow(() -> IgrpResponseStatusException.notFound("CostDriver not found: " + command.getId()));

      CostDriverRequestDTO req = command.getCostdriverrequest();

      CostDriverType driverType = (req.getDriverType() != null && !req.getDriverType().isBlank())
          ? CostDriverType.fromCodeOrThrow(req.getDriverType())
          : existing.getDriverType();

      CostDriverParams params = existing.getParameters();
      if (req.getParameters() != null && !req.getParameters().isEmpty()) {
         try {
            String paramsJson = objectMapper.writeValueAsString(req.getParameters());
            params = CostDriverParams.fromJson(driverType.getCode(), paramsJson);
         } catch (JsonProcessingException e) {
            throw IgrpResponseStatusException.badRequest("Failed to serialize parameters: " + e.getMessage());
         }
      }

      var validFrom = req.getValid_from() != null ? req.getValid_from() : existing.getValidFrom();
      CostDriver updated = existing.update(params, validFrom, existing.getCurrency(), existing.getLegalReference());
      CostDriver saved = costDriverRepository.update(updated);

      CostDriverResponseDTO response = new CostDriverResponseDTO();
      response.setId(saved.getId().getValor().getValor());
      response.setDriverType(saved.getDriverType().getCode());
      response.setParameters(req.getParameters());
      response.setValid_from(saved.getValidFrom());

      return ResponseEntity.ok(response);
   }
}