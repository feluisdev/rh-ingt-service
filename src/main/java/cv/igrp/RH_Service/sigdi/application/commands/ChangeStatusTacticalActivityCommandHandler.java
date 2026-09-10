package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.TacticalActivityStatus;
import cv.igrp.RH_Service.sigdi.application.dto.TaticalActivityStatusDTO;
import cv.igrp.RH_Service.sigdi.application.service.PaaActivityWindowPolicy;
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
import cv.igrp.RH_Service.sigdi.application.service.ActivityApprovalHistoryRecorder;

@Component
public class ChangeStatusTacticalActivityCommandHandler implements CommandHandler<ChangeStatusTacticalActivityCommand, ResponseEntity<Map<String, ?>>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(ChangeStatusTacticalActivityCommandHandler.class);

   private final TacticalActivityRepository repository;
   private final ActivityApprovalHistoryRecorder historyRecorder;
   private final PaaActivityWindowPolicy windowPolicy;

   public ChangeStatusTacticalActivityCommandHandler(TacticalActivityRepository repository,
         ActivityApprovalHistoryRecorder historyRecorder,
         PaaActivityWindowPolicy windowPolicy) {
     this.repository = repository;
     this.historyRecorder = historyRecorder;
     this.windowPolicy = windowPolicy;
   }

   @IgrpCommandHandler
   @Transactional
   public ResponseEntity<Map<String, ?>> handle(ChangeStatusTacticalActivityCommand command) {

      LOGGER.debug("ChangeStatusTacticalActivityCommand : {}", command);

      TaticalActivityStatusDTO request = command.getTaticalactivitystatus();
      TacticalActivityId id = TacticalActivityId.from(command.getId());

      var activity = repository.findByIdFull(id)
          .orElseThrow(() -> IgrpResponseStatusException.notFound("TacticalActivity não encontrada"));

      // A-132-111 / COR-01 (Phase 136, D-47 reverts T-139's 2026-09-05 exclusion of this
      // handler -- the sixth of six transition handlers A-132-111 named; the other five closed
      // in Phase 134). D-54: reachable via PATCH /api/tactical/activities/[id]/status, but
      // changeActivityStatus (functions/tactical.ts:406) has no caller in the interface today --
      // reproduce this one against the BFF route directly, not through the UI.
      windowPolicy.requireOpenFor(activity.getPaaLevel());

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

      // A-135-2AB (Phase 136, plano 136-10): escrita de histórico movida para o colaborador
      // único (ActivityApprovalHistoryRecorder), depois do save e dentro da mesma fronteira
      // @Transactional -- mesmo comportamento de antes (fromStatus = estado antes do switch).
      historyRecorder.record(id, oldStatus, updated.getStatus().getCode(), desiredStatus.getCode(), null);

      return ResponseEntity.ok(Map.of(
          "id", updated.getId().getValor().getValor(),
          "status", updated.getStatus().getCode(),
          "statusDesc", updated.getStatus().getDescription()
      ));
   }

}
