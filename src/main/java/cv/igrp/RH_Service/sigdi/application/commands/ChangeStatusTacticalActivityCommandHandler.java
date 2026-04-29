package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.TacticalActivityStatus;
import cv.igrp.RH_Service.sigdi.application.dto.TaticalActivityStatusDTO;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.TacticalActivityRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.TacticalActivityId;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component
public class ChangeStatusTacticalActivityCommandHandler implements CommandHandler<ChangeStatusTacticalActivityCommand, ResponseEntity<Map<String, ?>>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(ChangeStatusTacticalActivityCommandHandler.class);

   private final TacticalActivityRepository repository;

   public ChangeStatusTacticalActivityCommandHandler(TacticalActivityRepository repository) {
     this.repository = repository;
   }

   @IgrpCommandHandler
   @Transactional
   public ResponseEntity<Map<String, ?>> handle(ChangeStatusTacticalActivityCommand command) {

      LOGGER.debug("ChangeStatusTacticalActivityCommand : {}", command);

      TaticalActivityStatusDTO request = command.getTaticalactivitystatus();
      TacticalActivityId id = TacticalActivityId.from(command.getId());

      var activity = repository.findByIdFull(id)
          .orElseThrow(() -> IgrpResponseStatusException.notFound("TacticalActivity não encontrada"));

      TacticalActivityStatus desiredStatus = TacticalActivityStatus.fromCodeOrThrow(request.getStatus());

      var updated = switch (desiredStatus) {
        case APPROVED -> activity.approve();
        case REJECTED -> activity.reject();
        case CANCELLED -> activity.cancel();
        case PENDING_TACTICAL -> activity.submit();
        case DRAFT, PENDING_STRATEGIC, PENDING_BUDGET -> throw IgrpResponseStatusException.badRequest(
            "Não é permitido definir o status '" + desiredStatus.getCode() + "' diretamente");
      };

      repository.save(updated);

      return ResponseEntity.ok(Map.of(
          "id", updated.getId().getValor().getValor(),
          "status", updated.getStatus().getCode(),
          "statusDesc", updated.getStatus().getDescription()
      ));
   }

}
