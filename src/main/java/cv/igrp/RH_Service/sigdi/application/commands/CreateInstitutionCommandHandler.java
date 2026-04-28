package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.dto.CreateInstitutionRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.InstitutionResponseDTO;
import cv.igrp.RH_Service.sigdi.domain.admin.models.Institution;
import cv.igrp.RH_Service.sigdi.domain.admin.repository.InstitutionRepository;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CreateInstitutionCommandHandler
    implements CommandHandler<CreateInstitutionCommand, ResponseEntity<InstitutionResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateInstitutionCommandHandler.class);

  private final InstitutionRepository institutionRepository;

  public CreateInstitutionCommandHandler(InstitutionRepository institutionRepository) {
    this.institutionRepository = institutionRepository;
  }

  @IgrpCommandHandler
  @Transactional
  public ResponseEntity<InstitutionResponseDTO> handle(CreateInstitutionCommand command) {
    LOGGER.debug("CreateInstitutionCommand: {}", command);

    CreateInstitutionRequestDTO req = command.getBody();

    institutionRepository.findByCode(req.getCode()).ifPresent(existing -> {
      throw IgrpResponseStatusException.conflict(
          "SIGDI-ADM-001: Institution with code '" + req.getCode() + "' already exists.");
    });

    Institution institution = Institution.create(req.getCode(), req.getName(), req.getType(), req.getContactEmail());
    Institution saved = institutionRepository.save(institution);

    InstitutionResponseDTO response = new InstitutionResponseDTO();
    response.setId(saved.getId().getStringValor());
    response.setCode(saved.getCode());
    response.setName(saved.getName());
    response.setType(saved.getType());
    response.setIsActive(saved.isActive());
    response.setContactEmail(saved.getContactEmail());
    response.setCreatedAt(null);

    return ResponseEntity.status(201).body(response);
  }
}
