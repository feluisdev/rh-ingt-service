package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.dto.KeyResultCheckinRequestDTO;
import cv.igrp.RH_Service.sigdi.application.service.PaaActivityWindowPolicy;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.TacticalActivity;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.KeyResultRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.TacticalActivityRepository;
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
   private final TacticalActivityRepository activityRepository;
   private final PaaActivityWindowPolicy windowPolicy;

   public RegistraProcessoKrCommandHandler(KeyResultRepository keyResultRepository,
                                            TacticalActivityRepository activityRepository,
                                            PaaActivityWindowPolicy windowPolicy) {
      this.keyResultRepository = keyResultRepository;
      this.activityRepository = activityRepository;
      this.windowPolicy = windowPolicy;
   }

   @IgrpCommandHandler
   @Transactional
   public ResponseEntity<String> handle(RegistraProcessoKrCommand command) {

      LOGGER.debug("RegistraProcessoKrCommand : {}", command);

      KeyResultCheckinRequestDTO request = command.getKeyresultcheckinrequest();

      KeyResultId keyResultId = KeyResultId.from(command.getId());

      var keyResult = keyResultRepository.findByIdFull(keyResultId)
            .orElseThrow(() -> IgrpResponseStatusException.notFound("KeyResult não encontrado"));

      // A-132-112 / T-136-19 (Fase 136-06): check-in de progresso passa a exigir a janela PAA
      // do nível da atividade a que o resultado-chave pertence -- o activityId vem sempre do
      // resultado-chave carregado, nunca do pedido. Um resultado-chave sem activityId é órfão
      // e é recusado. O ato entra por reversão da T-139 pela D-47 (decisão D-58, com o risco
      // aceite pelo operador a 2026-09-10: a execução decorre o ano inteiro e a janela de
      // submissão não).
      if (keyResult.getActivityId() == null) {
        throw IgrpResponseStatusException.badRequest(
            "KeyResult sem activityId associado -- não é possível determinar a janela do PAA");
      }
      TacticalActivity activity = activityRepository.findById(keyResult.getActivityId())
          .orElseThrow(() -> IgrpResponseStatusException.badRequest(
              "activityId do KeyResult é inválido"));
      windowPolicy.requireOpenFor(activity.getPaaLevel());

      var updated = keyResult.applyCheckin(request.getValueAdded(), request.getEvidenceUrl(), request.getComment());

      keyResultRepository.save(updated);

      return ResponseEntity.ok("Check-in registado com sucesso");
   }

}
