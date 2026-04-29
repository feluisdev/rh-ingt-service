package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.security.SecurityContextHelper;
import cv.igrp.RH_Service.sigdi.application.dto.CreateDelegationRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.DelegationResponseDTO;
import cv.igrp.RH_Service.sigdi.domain.admin.models.Delegation;
import cv.igrp.RH_Service.sigdi.domain.admin.repository.UserDelegationRepository;
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
import java.util.UUID;

@Component
public class CreateDelegationCommandHandler
    implements CommandHandler<CreateDelegationCommand, ResponseEntity<DelegationResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateDelegationCommandHandler.class);

  private final UserDelegationRepository userDelegationRepository;
  private final SecurityContextHelper securityContextHelper;

  public CreateDelegationCommandHandler(UserDelegationRepository userDelegationRepository,
                                        SecurityContextHelper securityContextHelper) {
    this.userDelegationRepository = userDelegationRepository;
    this.securityContextHelper = securityContextHelper;
  }

  @IgrpCommandHandler
  @Transactional
  public ResponseEntity<DelegationResponseDTO> handle(CreateDelegationCommand command) {
    LOGGER.debug("CreateDelegationCommand: {}", command);

    CreateDelegationRequestDTO req = command.getBody();

    UUID delegatorId;
    UUID delegateId;
    try {
      delegatorId = UUID.fromString(command.getDelegatorUserId());
      delegateId = UUID.fromString(req.getDelegateUserId());
    } catch (IllegalArgumentException e) {
      throw IgrpResponseStatusException.badRequest("SIGDI-ADM-010: delegatorId ou delegateId inválido");
    }

    LocalDate startDate;
    LocalDate endDate;
    try {
      startDate = LocalDate.parse(req.getStartDate());
      endDate = LocalDate.parse(req.getEndDate());
    } catch (DateTimeParseException e) {
      throw IgrpResponseStatusException.badRequest("SIGDI-ADM-011: startDate ou endDate inválido (formato esperado: yyyy-MM-dd)");
    }

    Delegation delegation = Delegation.create(securityContextHelper.getCurrentInstitutionId(),
        delegatorId, delegateId, req.getScope(), startDate, endDate, req.getReason());
    Delegation saved = userDelegationRepository.save(delegation);

    DelegationResponseDTO response = new DelegationResponseDTO();
    response.setId(saved.getId().getStringValor());
    response.setDelegatorId(saved.getDelegatorId().toString());
    response.setDelegateId(saved.getDelegateId().toString());
    response.setScope(saved.getScope());
    response.setStartDate(saved.getStartDate().toString());
    response.setEndDate(saved.getEndDate().toString());
    response.setReason(saved.getReason());
    response.setIsActive(saved.isActive());

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}
