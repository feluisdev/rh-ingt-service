package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.InstitutionEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.InstitutionEntityRepository;
import cv.igrp.RH_Service.sigdi.application.dto.CreateInstitutionRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.InstitutionResponseDTO;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class CreateInstitutionCommandHandler
    implements CommandHandler<CreateInstitutionCommand, ResponseEntity<InstitutionResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateInstitutionCommandHandler.class);

  private final InstitutionEntityRepository institutionRepository;

  public CreateInstitutionCommandHandler(InstitutionEntityRepository institutionRepository) {
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

    InstitutionEntity entity = new InstitutionEntity();
    entity.setId(UUID.randomUUID());
    entity.setCode(req.getCode());
    entity.setName(req.getName());
    entity.setType(req.getType());
    entity.setActive(true);

    InstitutionEntity saved = institutionRepository.save(entity);

    InstitutionResponseDTO response = new InstitutionResponseDTO();
    response.setId(saved.getId().toString());
    response.setCode(saved.getCode());
    response.setName(saved.getName());
    response.setType(saved.getType());
    response.setIsActive(saved.isActive());
    response.setContactEmail(null);
    response.setCreatedAt(saved.getCreatedDate() != null ? saved.getCreatedDate().toString() : null);

    return ResponseEntity.status(201).body(response);
  }
}
