package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.sigdi.application.dto.CreateDelegationRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.DelegationResponseDTO;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class CreateDelegationCommandHandler
    implements CommandHandler<CreateDelegationCommand, ResponseEntity<DelegationResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateDelegationCommandHandler.class);

  @IgrpCommandHandler
  @Transactional
  public ResponseEntity<DelegationResponseDTO> handle(CreateDelegationCommand command) {
    LOGGER.debug("CreateDelegationCommand: {}", command);

    CreateDelegationRequestDTO req = command.getBody();

    // Stub: no DelegationEntity backing this — returns a generated response
    DelegationResponseDTO response = new DelegationResponseDTO();
    response.setId(UUID.randomUUID().toString());
    response.setDelegatorId(command.getDelegatorUserId());
    response.setDelegateId(req.getDelegateUserId());
    response.setScope(req.getScope());
    response.setStartDate(req.getStartDate());
    response.setEndDate(req.getEndDate());
    response.setReason(req.getReason());
    response.setIsActive(true);
    response.setCreatedAt(LocalDateTime.now().toString());

    return ResponseEntity.status(201).body(response);
  }
}
