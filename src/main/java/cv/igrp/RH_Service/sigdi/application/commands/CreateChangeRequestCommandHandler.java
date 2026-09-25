package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.ChangeRequestField;
import cv.igrp.RH_Service.sigdi.application.dto.ChangeRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.ChangeRequestResponseDTO;
import cv.igrp.RH_Service.sigdi.application.service.PaaActivityWindowPolicy;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.ChangeRequest;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.TacticalActivity;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.ChangeRequestRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.TacticalActivityRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.TacticalActivityId;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CreateChangeRequestCommandHandler
    implements CommandHandler<CreateChangeRequestCommand, ResponseEntity<ChangeRequestResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateChangeRequestCommandHandler.class);

  private final TacticalActivityRepository activityRepository;
  private final ChangeRequestRepository changeRequestRepository;
  private final PaaActivityWindowPolicy windowPolicy;

  public CreateChangeRequestCommandHandler(TacticalActivityRepository activityRepository,
                                           ChangeRequestRepository changeRequestRepository,
                                           PaaActivityWindowPolicy windowPolicy) {
    this.activityRepository = activityRepository;
    this.changeRequestRepository = changeRequestRepository;
    this.windowPolicy = windowPolicy;
  }

  @IgrpCommandHandler
  @Transactional
  public ResponseEntity<ChangeRequestResponseDTO> handle(CreateChangeRequestCommand command) {
    LOGGER.debug("CreateChangeRequestCommand: {}", command);

    TacticalActivityId activityId = TacticalActivityId.from(command.getActivityId());

    TacticalActivity activity = activityRepository.findById(activityId)
        .orElseThrow(() -> IgrpResponseStatusException.notFound("TacticalActivity não encontrada"));

    if (!activity.isApproved()) {
      throw IgrpResponseStatusException.badRequest(
          "Change Request só é permitido em atividades APPROVED");
    }

    // A-132-108 / COR-01 (Phase 136, D-47 reverts T-139's 2026-09-05 exclusion of this handler):
    // submission window checked before the save at the bottom of this method, sourced from the
    // loaded activity's paaLevel -- never from the request (D-27, Phase 134).
    windowPolicy.requireOpenFor(activity.getPaaLevel());

    ChangeRequestDTO dto = command.getChangerequest();

    // Rejected here, at creation, and not only at approval: a request whose field cannot be
    // applied is a request that will never be approvable, and the person who wrote the
    // justification should learn that now rather than after someone approves it.
    ChangeRequestField.fromCodeOrThrow(dto.getFieldName());

    ChangeRequest changeRequest = ChangeRequest.create(
        activity.getInstitutionId(),
        activityId,
        dto.getFieldName(),
        dto.getCurrentValue(),
        dto.getProposedValue(),
        dto.getJustification()
    );

    ChangeRequest saved = changeRequestRepository.save(changeRequest);

    return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
  }

  private ChangeRequestResponseDTO toResponse(ChangeRequest cr) {
    ChangeRequestResponseDTO dto = new ChangeRequestResponseDTO();
    dto.setId(cr.getId().getValor().getValor());
    dto.setActivityId(cr.getActivityId().getValor().getValor());
    dto.setFieldName(cr.getFieldName());
    dto.setCurrentValue(cr.getCurrentValue());
    dto.setProposedValue(cr.getProposedValue());
    dto.setJustification(cr.getJustification());
    dto.setStatus(cr.getStatus().getCode());
    dto.setReviewerComment(cr.getReviewerComment());
    return dto;
  }
}
