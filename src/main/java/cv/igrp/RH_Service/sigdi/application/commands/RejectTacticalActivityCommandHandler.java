package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.dto.ActivityWorkflowResponseDTO;
import cv.igrp.RH_Service.sigdi.application.service.ActivityApprovalHistoryRecorder;
import cv.igrp.RH_Service.sigdi.application.service.PaaActivityWindowPolicy;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.TacticalActivity;
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
public class RejectTacticalActivityCommandHandler
    implements CommandHandler<RejectTacticalActivityCommand, ResponseEntity<ActivityWorkflowResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(RejectTacticalActivityCommandHandler.class);

  private final TacticalActivityRepository repository;
  private final PaaActivityWindowPolicy windowPolicy;
  private final ActivityApprovalHistoryRecorder historyRecorder;

  public RejectTacticalActivityCommandHandler(TacticalActivityRepository repository,
      PaaActivityWindowPolicy windowPolicy,
      ActivityApprovalHistoryRecorder historyRecorder) {
    this.repository = repository;
    this.windowPolicy = windowPolicy;
    this.historyRecorder = historyRecorder;
  }

  @IgrpCommandHandler
  @Transactional
  public ResponseEntity<ActivityWorkflowResponseDTO> handle(RejectTacticalActivityCommand command) {
    LOGGER.debug("RejectTacticalActivityCommand: {}", command);

    TacticalActivityId id = TacticalActivityId.from(command.getId());

    TacticalActivity activity = repository.findByIdFull(id)
        .orElseThrow(() -> IgrpResponseStatusException.notFound("TacticalActivity não encontrada"));

    // A-132-111 / POR-02 (Phase 134): the window gate comes before the comment validation
    // below, on purpose -- outside the window, the reason the user needs to hear is the
    // deadline, and rejecting for a missing comment first would send them to fix the wrong
    // thing (the A-132-114 finding is the same problem in another handler).
    windowPolicy.requireOpenFor(activity.getPaaLevel());

    String comment = (command.getWorkflowcomment() != null) ? command.getWorkflowcomment().getComment() : null;
    if (comment == null || comment.isBlank()) {
      throw IgrpResponseStatusException.of(HttpStatus.BAD_REQUEST, "Comentário é obrigatório para rejeitar");
    }

    String previousStatus = activity.getStatus().getCode();
    TacticalActivity rejected = activity.reject();
    repository.save(rejected);

    // A-135-2AB (Phase 136, plano 136-10): rasto de auditoria escrito depois do save, dentro
    // da mesma fronteira @Transactional -- nunca antes, porque gravaria histórico de uma
    // transição que ainda podia falhar.
    historyRecorder.record(rejected.getId(), previousStatus, rejected.getStatus().getCode(),
        rejected.getStatus().getCode(), comment);

    ActivityWorkflowResponseDTO response = new ActivityWorkflowResponseDTO();
    response.setActivityId(rejected.getId().getValor().getValor());
    response.setPreviousStatus(previousStatus);
    response.setCurrentStatus(rejected.getStatus().getCode());
    response.setComment(comment);

    return ResponseEntity.ok(response);
  }
}
