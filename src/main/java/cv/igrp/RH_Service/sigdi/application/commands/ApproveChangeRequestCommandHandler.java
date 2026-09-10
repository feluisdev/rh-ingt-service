package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.ChangeRequestField;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ApproveChangeRequestCommandHandler
    implements CommandHandler<ApproveChangeRequestCommand, ResponseEntity<ChangeRequestResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApproveChangeRequestCommandHandler.class);

  private final ChangeRequestRepository changeRequestRepository;
  private final TacticalActivityRepository activityRepository;
  private final PaaActivityWindowPolicy windowPolicy;

  public ApproveChangeRequestCommandHandler(ChangeRequestRepository changeRequestRepository,
                                            TacticalActivityRepository activityRepository,
                                            PaaActivityWindowPolicy windowPolicy) {
    this.changeRequestRepository = changeRequestRepository;
    this.activityRepository = activityRepository;
    this.windowPolicy = windowPolicy;
  }

  @IgrpCommandHandler
  @Transactional
  public ResponseEntity<ChangeRequestResponseDTO> handle(ApproveChangeRequestCommand command) {
    LOGGER.debug("ApproveChangeRequestCommand: {}", command);

    ChangeRequestId crId = ChangeRequestId.from(command.getId());

    ChangeRequest changeRequest = changeRequestRepository.findById(crId)
        .orElseThrow(() -> IgrpResponseStatusException.notFound("Change Request não encontrado"));

    String comment = (command.getWorkflowcomment() != null) ? command.getWorkflowcomment().getComment() : null;

    // Everything that can refuse the approval runs BEFORE the first save, and every step below
    // is pure -- approve() and applyApprovedChange() both return new instances rather than
    // mutating. That is what makes the second success criterion hold without leaning on
    // transaction rollback: there is no ordering of these lines that marks the request APPROVED
    // and leaves the activity unchanged.
    ChangeRequest approved = changeRequest.approve(comment);

    ChangeRequestField field = ChangeRequestField.fromCodeOrThrow(changeRequest.getFieldName());

    TacticalActivity activity = activityRepository.findById(changeRequest.getActivityId())
        .orElseThrow(() -> IgrpResponseStatusException.notFound(
            "A atividade tática visada pelo pedido de alteração não foi encontrada"));

    // A-132-107 / COR-01 (Phase 136, D-47 reverts T-139's 2026-09-05 exclusion of this handler,
    // which the D-29 ambiguity note of Phase 134 had left open): submission window checked here,
    // sourced from the loaded activity's paaLevel (D-27), still before either save below.
    windowPolicy.requireOpenFor(activity.getPaaLevel());

    TacticalActivity changedActivity = activity.applyApprovedChange(field, changeRequest.getProposedValue());

    activityRepository.save(changedActivity);
    ChangeRequest saved = changeRequestRepository.save(approved);

    LOGGER.info("Change Request {} aprovado: campo {} da atividade {} alterado, atividade em {}",
        saved.getId().getValor().getValor(), field.getCode(),
        changedActivity.getId().getValor().getValor(), changedActivity.getStatus().getCode());

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
