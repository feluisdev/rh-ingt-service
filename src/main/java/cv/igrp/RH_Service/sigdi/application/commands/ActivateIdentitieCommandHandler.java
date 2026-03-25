package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.InstitutionalIdentityRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.InstitutionalIdentityId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.InstitutionalIdentityMapper;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cv.igrp.RH_Service.sigdi.application.dto.IdentityResponseDTO;

@Component
public class ActivateIdentitieCommandHandler implements CommandHandler<ActivateIdentitieCommand, ResponseEntity<IdentityResponseDTO>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(ActivateIdentitieCommandHandler.class);

  private final InstitutionalIdentityRepository identityRepository;
  private final InstitutionalIdentityMapper dtoMapper;

   public ActivateIdentitieCommandHandler(InstitutionalIdentityRepository identityRepository, InstitutionalIdentityMapper dtoMapper) {

     this.identityRepository = identityRepository;
     this.dtoMapper = dtoMapper;
   }

   @IgrpCommandHandler
   public ResponseEntity<IdentityResponseDTO> handle(ActivateIdentitieCommand command) {

      LOGGER.debug("ActivateIdentitieCommand : {}", command);

     var identityId = InstitutionalIdentityId.from(command.getId());

     var identity = identityRepository.findById(identityId)
         .orElseThrow(() -> IgrpResponseStatusException.notFound(
             "Identidade Institucional não encontrada: " + command.getId()));

     var activated = identity.activate();

     // Persiste
     var saved = identityRepository.save(activated);

     // Mapeia para response
     var response = dtoMapper.toResponse(saved);

     return ResponseEntity.ok(response);
   }

}
