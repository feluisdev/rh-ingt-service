package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.dto.KeyResultCheckinRequestDTO;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.KeyResultRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.KeyResultId;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RegistraProcessoKrCommandHandler
      implements CommandHandler<RegistraProcessoKrCommand, ResponseEntity<String>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(RegistraProcessoKrCommandHandler.class);

   private final KeyResultRepository keyResultRepository;

   public RegistraProcessoKrCommandHandler(KeyResultRepository keyResultRepository) {
      this.keyResultRepository = keyResultRepository;
   }

   @IgrpCommandHandler
   @Transactional
   public ResponseEntity<String> handle(RegistraProcessoKrCommand command) {

      LOGGER.debug("RegistraProcessoKrCommand : {}", command);

      KeyResultCheckinRequestDTO request = command.getKeyresultcheckinrequest();

      KeyResultId keyResultId = KeyResultId.from(command.getId());

      var keyResult = keyResultRepository.findByIdFull(keyResultId)
            .orElseThrow(() -> IgrpResponseStatusException.notFound("KeyResult não encontrado"));

      var updated = keyResult.applyCheckin(request.getValueAdded(), request.getEvidenceUrl(), request.getComment());

      keyResultRepository.save(updated);

      return ResponseEntity.ok("Check-in registado com sucesso");
   }

}
