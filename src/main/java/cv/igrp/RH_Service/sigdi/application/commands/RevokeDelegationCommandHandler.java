package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.dto.DelegationResponseDTO;
import cv.igrp.RH_Service.sigdi.domain.admin.models.Delegation;
import cv.igrp.RH_Service.sigdi.domain.admin.repository.UserDelegationRepository;
import cv.igrp.RH_Service.sigdi.domain.admin.valueobject.DelegationId;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RevokeDelegationCommandHandler
    implements CommandHandler<RevokeDelegationCommand, ResponseEntity<DelegationResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(RevokeDelegationCommandHandler.class);

  private final UserDelegationRepository userDelegationRepository;

  public RevokeDelegationCommandHandler(UserDelegationRepository userDelegationRepository) {
    this.userDelegationRepository = userDelegationRepository;
  }

  @IgrpCommandHandler
  @Transactional
  public ResponseEntity<DelegationResponseDTO> handle(RevokeDelegationCommand command) {
    LOGGER.debug("RevokeDelegationCommand: {}", command);

    DelegationId delegationId;
    try {
      delegationId = DelegationId.from(command.getDelegationId());
    } catch (IllegalArgumentException e) {
      throw IgrpResponseStatusException.badRequest("SIGDI-ADM-020: delegationId inválido");
    }

    Delegation revoked = userDelegationRepository.revoke(delegationId);

    DelegationResponseDTO response = new DelegationResponseDTO();
    response.setId(revoked.getId().getStringValor());
    response.setDelegatorId(revoked.getDelegatorId().toString());
    response.setDelegateId(revoked.getDelegateId().toString());
    response.setScope(revoked.getScope());
    response.setStartDate(revoked.getStartDate().toString());
    response.setEndDate(revoked.getEndDate().toString());
    response.setReason(revoked.getReason());
    response.setIsActive(revoked.isActive());
    response.setCreatedAt(null);

    return ResponseEntity.ok(response);
  }
}
