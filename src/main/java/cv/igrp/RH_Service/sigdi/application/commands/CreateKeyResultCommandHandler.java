package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.KeyResultMetricUnit;
import cv.igrp.RH_Service.sigdi.application.dto.KeyResultRequestDTO;
import cv.igrp.RH_Service.sigdi.application.service.PaaActivityWindowPolicy;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import cv.igrp.RH_Service.sigdi.application.dto.KeyResultResponseDTO;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.KeyResult;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.TacticalActivity;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.KeyResultRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.TacticalActivityRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.TacticalActivityId;

@Component
public class CreateKeyResultCommandHandler
    implements CommandHandler<CreateKeyResultCommand, ResponseEntity<KeyResultResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateKeyResultCommandHandler.class);

  private final KeyResultRepository keyResultRepository;
  private final TacticalActivityRepository activityRepository;
  private final PaaActivityWindowPolicy windowPolicy;

  public CreateKeyResultCommandHandler(KeyResultRepository keyResultRepository,
                                       TacticalActivityRepository activityRepository,
                                       PaaActivityWindowPolicy windowPolicy) {
    this.keyResultRepository = keyResultRepository;
    this.activityRepository = activityRepository;
    this.windowPolicy = windowPolicy;
  }

  @IgrpCommandHandler
  @Transactional
  public ResponseEntity<KeyResultResponseDTO> handle(CreateKeyResultCommand command) {
    LOGGER.debug("CreateKeyResultCommand : {}", command);

    KeyResultRequestDTO request = command.getKeyresultrequest();

    if (request.getCurrentValue() != null && request.getCurrentValue().signum() != 0) {
      throw IgrpResponseStatusException.badRequest("currentValue não pode ser definido manualmente");
    }

    TacticalActivityId activityId = TacticalActivityId.from(request.getActivityId());
    TacticalActivity activity = activityRepository.findById(activityId)
        .orElseThrow(() -> IgrpResponseStatusException.badRequest("activityId inválido"));

    // A-132-112 (Fase 136-06): paaLevel vem sempre da atividade carregada acima, nunca do
    // pedido -- o ato entra por reversão da T-139 pela D-47.
    windowPolicy.requireOpenFor(activity.getPaaLevel());

    KeyResultMetricUnit metricUnit = KeyResultMetricUnit.fromCodeOrThrow(request.getMetricUnit());

    KeyResult keyResult = KeyResult.create(
        activity.getInstitutionId(),
        activityId,
        request.getTitle(),
        request.getTargetValue(),
        metricUnit,
        request.getCriteriaSuperado(),
        request.getCriteriaSegurancaMin(),
        request.getCriteriaSegurancaMax(),
        request.getCriteriaAlcancadoMin(),
        request.getCriteriaAlcancadoMax(),
        request.getCriteriaInsuficiente()
    );

    KeyResult saved = keyResultRepository.save(keyResult);

    return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
  }

  private KeyResultResponseDTO toResponse(KeyResult kr) {
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
