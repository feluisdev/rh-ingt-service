package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.dto.InstitutionResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.UpdateInstitutionRequestDTO;
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
public class UpdateInstitutionCommandHandler
    implements CommandHandler<UpdateInstitutionCommand, ResponseEntity<InstitutionResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(UpdateInstitutionCommandHandler.class);

  private final InstitutionRepository institutionRepository;

  public UpdateInstitutionCommandHandler(InstitutionRepository institutionRepository) {
    this.institutionRepository = institutionRepository;
  }

  @IgrpCommandHandler
  @Transactional
  public ResponseEntity<InstitutionResponseDTO> handle(UpdateInstitutionCommand command) {
    LOGGER.debug("UpdateInstitutionCommand: {}", command);

    Institution existing = institutionRepository.findById(InstitutionId.from(command.getId()))
        .orElseThrow(() -> IgrpResponseStatusException.notFound(
            "Institution with id '" + command.getId() + "' not found."));

    UpdateInstitutionRequestDTO req = command.getBody();
    Institution updated = existing.update(req.getName(), req.getType(), req.getContactEmail());
    Institution saved = institutionRepository.update(updated);

    InstitutionResponseDTO response = new InstitutionResponseDTO();
    response.setId(saved.getId().getStringValor());
    response.setCode(saved.getCode());
    response.setName(saved.getName());
    response.setType(saved.getType());
    response.setIsActive(saved.isActive());
    response.setContactEmail(saved.getContactEmail());
    response.setCreatedAt(null);

    return ResponseEntity.ok(response);
  }
}
