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
import java.util.UUID;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.TaticalActivityHistoryEntityRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.TaticalActivityHistoryEntity;
import cv.igrp.RH_Service.shared.security.SecurityContextHelper;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.TacticalActivitiesEntityRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.TacticalActivitiesEntity;

@Component
public class ChangeStatusTacticalActivityCommandHandler implements CommandHandler<ChangeStatusTacticalActivityCommand, ResponseEntity<Map<String, ?>>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(ChangeStatusTacticalActivityCommandHandler.class);

   private final TacticalActivityRepository repository;
   private final TaticalActivityHistoryEntityRepository historyRepository;
   private final TacticalActivitiesEntityRepository entityRepository;
   private final SecurityContextHelper securityContextHelper;

   public ChangeStatusTacticalActivityCommandHandler(TacticalActivityRepository repository, 
         TaticalActivityHistoryEntityRepository historyRepository,
         TacticalActivitiesEntityRepository entityRepository,
         SecurityContextHelper securityContextHelper) {
     this.repository = repository;
     this.historyRepository = historyRepository;
     this.entityRepository = entityRepository;
     this.securityContextHelper = securityContextHelper;
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

      String oldStatus = activity.getStatus().getCode();

      var updated = switch (desiredStatus) {
        case APPROVED -> activity.approve();
        case REJECTED -> activity.reject();
        case CANCELLED -> activity.cancel();
        case PENDING_TACTICAL -> activity.submit();
        case DRAFT, PENDING_STRATEGIC, PENDING_BUDGET -> throw IgrpResponseStatusException.badRequest(
            "Não é permitido definir o status '" + desiredStatus.getCode() + "' diretamente");
      };

      repository.save(updated);

      // Save History
      TacticalActivitiesEntity actEntity = entityRepository.findById(id.getValor().getValor()).orElse(null);
      if (actEntity != null) {
          TaticalActivityHistoryEntity history = new TaticalActivityHistoryEntity();
          history.setId(UUID.randomUUID());
          history.setInstitutionId(actEntity.getInstitutionId());
          history.setActivityId(actEntity);
          history.setAction(desiredStatus.getCode());
          try {
              history.setActorId(UUID.fromString(securityContextHelper.getCurrentUserId()));
          } catch (Exception e) {
              history.setActorId(null);
          }
          history.setFromStatus(oldStatus);
          history.setToStatus(updated.getStatus().getCode());
          historyRepository.save(history);
      }

      return ResponseEntity.ok(Map.of(
          "id", updated.getId().getValor().getValor(),
          "status", updated.getStatus().getCode(),
          "statusDesc", updated.getStatus().getDescription()
      ));
   }

}
