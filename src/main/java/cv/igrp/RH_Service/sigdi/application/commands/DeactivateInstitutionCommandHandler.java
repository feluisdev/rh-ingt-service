package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.InstitutionEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.InstitutionEntityRepository;
import cv.igrp.RH_Service.sigdi.application.dto.DeactivateInstitutionResponseDTO;
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
public class DeactivateInstitutionCommandHandler
    implements CommandHandler<DeactivateInstitutionCommand, ResponseEntity<DeactivateInstitutionResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(DeactivateInstitutionCommandHandler.class);

  private final InstitutionEntityRepository institutionRepository;

  public DeactivateInstitutionCommandHandler(InstitutionEntityRepository institutionRepository) {
    this.institutionRepository = institutionRepository;
  }

  @IgrpCommandHandler
  @Transactional
  public ResponseEntity<DeactivateInstitutionResponseDTO> handle(DeactivateInstitutionCommand command) {
    LOGGER.debug("DeactivateInstitutionCommand: {}", command);

    UUID id = UUID.fromString(command.getInstitutionId());
    InstitutionEntity entity = institutionRepository.findByIdOrThrow(id);

    if (!entity.isActive()) {
      throw IgrpResponseStatusException.conflict(
          "SIGDI-ADM-002: Institution '" + command.getInstitutionId() + "' is already inactive.");
    }

    entity.setActive(false);
    institutionRepository.save(entity);

    DeactivateInstitutionResponseDTO response = new DeactivateInstitutionResponseDTO();
    response.setInstitutionId(entity.getId().toString());
    response.setIsActive(false);
    response.setDeactivatedAt(LocalDateTime.now().toString());
    response.setAffectedUsers(0);

    return ResponseEntity.ok(response);
  }
}
