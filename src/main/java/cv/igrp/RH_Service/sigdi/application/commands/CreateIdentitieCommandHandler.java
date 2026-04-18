package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.security.SecurityContextHelper;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.InstitutionalIdentity;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.InstitutionalIdentityRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.strategy.InstitutionalIdentityMapper;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import cv.igrp.RH_Service.sigdi.application.dto.IdentityResponseDTO;

import java.time.Year;

@Component
public class CreateIdentitieCommandHandler
    implements CommandHandler<CreateIdentitieCommand, ResponseEntity<IdentityResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateIdentitieCommandHandler.class);

  private final InstitutionalIdentityRepository identityRepository;
  private final InstitutionalIdentityMapper identityMapper;
  private final SecurityContextHelper securityContextHelper;

  public CreateIdentitieCommandHandler(InstitutionalIdentityRepository identityRepository,
      InstitutionalIdentityMapper identityMapper,
      SecurityContextHelper securityContextHelper) {
    this.identityRepository = identityRepository;
    this.identityMapper = identityMapper;
    this.securityContextHelper = securityContextHelper;
  }

  @IgrpCommandHandler
  @Transactional
  public ResponseEntity<IdentityResponseDTO> handle(CreateIdentitieCommand command) {
    LOGGER.debug("CreateIdentitieCommand : {}", command);

    var request = command.getCreateidentityrequest();
    var values = identityMapper.toValues(request);

    identityRepository.findActive()
        .ifPresent(active -> identityRepository.save(active.deactivate()));

    var identity = InstitutionalIdentity.create(
        securityContextHelper.getCurrentInstitutionId(),
        Year.now().getValue(),
        request.getMission(),
        request.getVision(),
        values,
        request.getVersionComment()
    );

    InstitutionalIdentity saved = identityRepository.save(identity);
    IdentityResponseDTO response = identityMapper.toResponse(saved);

    return ResponseEntity.status(201).body(response);
  }
}
