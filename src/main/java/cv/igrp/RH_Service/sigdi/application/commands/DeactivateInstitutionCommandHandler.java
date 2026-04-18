package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.dto.DeactivateInstitutionResponseDTO;
import cv.igrp.RH_Service.sigdi.domain.admin.models.Institution;
import cv.igrp.RH_Service.sigdi.domain.admin.repository.InstitutionRepository;
import cv.igrp.RH_Service.sigdi.domain.admin.valueobject.InstitutionId;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DeactivateInstitutionCommandHandler
    implements CommandHandler<DeactivateInstitutionCommand, ResponseEntity<DeactivateInstitutionResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(DeactivateInstitutionCommandHandler.class);

  private final InstitutionRepository institutionRepository;

  public DeactivateInstitutionCommandHandler(InstitutionRepository institutionRepository) {
    this.institutionRepository = institutionRepository;
  }

  @IgrpCommandHandler
  @Transactional
  public ResponseEntity<DeactivateInstitutionResponseDTO> handle(DeactivateInstitutionCommand command) {
    LOGGER.debug("DeactivateInstitutionCommand: {}", command);

    InstitutionId id = InstitutionId.from(command.getInstitutionId());

    Institution institution = institutionRepository.findById(id)
        .orElseThrow(() -> IgrpResponseStatusException.notFound(
            "Institution not found for id: " + command.getInstitutionId()));

    if (!institution.isActive()) {
      throw IgrpResponseStatusException.conflict(
          "SIGDI-ADM-002: Institution '" + command.getInstitutionId() + "' is already inactive.");
    }

    Institution deactivated = institution.deactivate();
    institutionRepository.save(deactivated);

    DeactivateInstitutionResponseDTO response = new DeactivateInstitutionResponseDTO();
    response.setInstitutionId(deactivated.getId().getStringValor());
    response.setIsActive(false);
    response.setDeactivatedAt(deactivated.getDeactivatedAt() != null
        ? deactivated.getDeactivatedAt().toString() : null);
    response.setAffectedUsers(0);

    return ResponseEntity.ok(response);
  }
}
