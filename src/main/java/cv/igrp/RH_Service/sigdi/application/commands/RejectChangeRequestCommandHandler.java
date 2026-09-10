package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.dto.ChangeRequestResponseDTO;
import cv.igrp.RH_Service.sigdi.application.service.PaaActivityWindowPolicy;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.ChangeRequest;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.TacticalActivity;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.ChangeRequestRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.TacticalActivityRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.ChangeRequestId;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RejectChangeRequestCommandHandler
    implements CommandHandler<RejectChangeRequestCommand, ResponseEntity<ChangeRequestResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(RejectChangeRequestCommandHandler.class);

  private final ChangeRequestRepository changeRequestRepository;
  private final TacticalActivityRepository activityRepository;
  private final PaaActivityWindowPolicy windowPolicy;

  public RejectChangeRequestCommandHandler(ChangeRequestRepository changeRequestRepository,
                                           TacticalActivityRepository activityRepository,
                                           PaaActivityWindowPolicy windowPolicy) {
    this.changeRequestRepository = changeRequestRepository;
    this.activityRepository = activityRepository;
    this.windowPolicy = windowPolicy;
  }

  @IgrpCommandHandler
  @Transactional
  public ResponseEntity<ChangeRequestResponseDTO> handle(RejectChangeRequestCommand command) {
    LOGGER.debug("RejectChangeRequestCommand: {}", command);

    ChangeRequestId crId = ChangeRequestId.from(command.getId());

    ChangeRequest changeRequest = changeRequestRepository.findById(crId)
        .orElseThrow(() -> IgrpResponseStatusException.notFound("Change Request não encontrado"));

    String comment = (command.getWorkflowcomment() != null) ? command.getWorkflowcomment().getComment() : null;
    if (comment == null || comment.isBlank()) {
      throw IgrpResponseStatusException.of(HttpStatus.BAD_REQUEST, "Comentário é obrigatório para rejeitar");
    }

    // A-132-109 / COR-01 (Phase 136, D-47 reverts T-139's 2026-09-05 exclusion of this handler).
    // Before this plan the handler did not load the activity at all -- it does now, only to read
    // the paaLevel the submission window is gated on (D-27, Phase 134: never from the request).
    // Not-found uses the same wording ApproveChangeRequestCommandHandler already uses for the
    // same fact, so there is not a second sentence for one thing.
    TacticalActivity activity = activityRepository.findById(changeRequest.getActivityId())
        .orElseThrow(() -> IgrpResponseStatusException.notFound(
            "A atividade tática visada pelo pedido de alteração não foi encontrada"));
    windowPolicy.requireOpenFor(activity.getPaaLevel());

    ChangeRequest rejected = changeRequest.reject(comment);
    ChangeRequest saved = changeRequestRepository.save(rejected);

    return ResponseEntity.ok(toResponse(saved));
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
