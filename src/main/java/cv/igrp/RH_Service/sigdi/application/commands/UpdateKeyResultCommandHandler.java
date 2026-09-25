package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.KeyResultMetricUnit;
import cv.igrp.RH_Service.sigdi.application.dto.KeyResultRequestDTO;
import cv.igrp.RH_Service.sigdi.application.service.PaaActivityWindowPolicy;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import cv.igrp.RH_Service.sigdi.application.dto.KeyResultResponseDTO;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.TacticalActivity;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.KeyResultRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.TacticalActivityRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.KeyResultId;

@Component
public class UpdateKeyResultCommandHandler implements CommandHandler<UpdateKeyResultCommand, ResponseEntity<KeyResultResponseDTO>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(UpdateKeyResultCommandHandler.class);

   private final KeyResultRepository keyResultRepository;
   private final TacticalActivityRepository activityRepository;
   private final PaaActivityWindowPolicy windowPolicy;

   public UpdateKeyResultCommandHandler(KeyResultRepository keyResultRepository,
                                        TacticalActivityRepository activityRepository,
                                        PaaActivityWindowPolicy windowPolicy) {
     this.keyResultRepository = keyResultRepository;
     this.activityRepository = activityRepository;
     this.windowPolicy = windowPolicy;
   }

   @IgrpCommandHandler
   @Transactional
   public ResponseEntity<KeyResultResponseDTO> handle(UpdateKeyResultCommand command) {

      LOGGER.debug("UpdateKeyResultCommand : {}", command);

      KeyResultRequestDTO request = command.getKeyresultrequest();
      KeyResultId id = KeyResultId.from(command.getId());

      var current = keyResultRepository.findByIdFull(id)
          .orElseThrow(() -> IgrpResponseStatusException.notFound("KeyResult não encontrado"));

      if (request.getCurrentValue() != null && current.getCurrentValue().compareTo(request.getCurrentValue()) != 0) {
        throw IgrpResponseStatusException.badRequest("currentValue só pode ser alterado via check-in");
      }

      if (request.getActivityId() != null && current.getActivityId() != null
          && !current.getActivityId().getValor().getValor().equals(request.getActivityId())) {
        throw IgrpResponseStatusException.badRequest("activityId não pode ser alterado");
      }

      // A-132-112 (Fase 136-06): o activityId vem sempre do resultado-chave já carregado,
      // nunca do pedido -- usar o do pedido reabriria por outra porta a guarda de cima. Um
      // resultado-chave sem activityId é órfão e é recusado, não deixado passar em silêncio.
      // O ato entra por reversão da T-139 pela D-47.
      if (current.getActivityId() == null) {
        throw IgrpResponseStatusException.badRequest(
            "KeyResult sem activityId associado -- não é possível determinar a janela do PAA");
      }
      TacticalActivity activity = activityRepository.findById(current.getActivityId())
          .orElseThrow(() -> IgrpResponseStatusException.badRequest(
              "activityId do KeyResult é inválido"));
      windowPolicy.requireOpenFor(activity.getPaaLevel());

      KeyResultMetricUnit metricUnit = request.getMetricUnit() != null
          ? KeyResultMetricUnit.fromCodeOrThrow(request.getMetricUnit())
          : current.getMetricUnit();

      var updated = current.updateDetails(request.getTitle(), request.getTargetValue(), metricUnit);
      var saved = keyResultRepository.save(updated);

      return ResponseEntity.ok(toResponse(saved));
   }

   private KeyResultResponseDTO toResponse(cv.igrp.RH_Service.sigdi.domain.tatical.models.KeyResult kr) {
     KeyResultResponseDTO dto = new KeyResultResponseDTO();
     dto.setId(kr.getId().getValor().getValor());
     dto.setTitle(kr.getTitle());
     dto.setTargetValue(kr.getTargetValue());
     dto.setCurrentValue(kr.getCurrentValue());
     dto.setMetricUnit(kr.getMetricUnit() != null ? kr.getMetricUnit().getCode() : null);
     dto.setActivityId(kr.getActivityId() != null ? kr.getActivityId().getValor().getValor() : null);
     return dto;
   }

}
