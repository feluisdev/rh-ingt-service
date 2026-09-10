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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SubmitTacticalActivityCommandHandler
    implements CommandHandler<SubmitTacticalActivityCommand, ResponseEntity<ActivityWorkflowResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(SubmitTacticalActivityCommandHandler.class);

  private final TacticalActivityRepository repository;
  private final PaaActivityWindowPolicy windowPolicy;
  private final ActivityApprovalHistoryRecorder historyRecorder;

  public SubmitTacticalActivityCommandHandler(TacticalActivityRepository repository,
      PaaActivityWindowPolicy windowPolicy,
      ActivityApprovalHistoryRecorder historyRecorder) {
    this.repository = repository;
    this.windowPolicy = windowPolicy;
    this.historyRecorder = historyRecorder;
  }

  @IgrpCommandHandler
  @Transactional
  public ResponseEntity<ActivityWorkflowResponseDTO> handle(SubmitTacticalActivityCommand command) {
    LOGGER.debug("SubmitTacticalActivityCommand: {}", command);

    TacticalActivityId id = TacticalActivityId.from(command.getId());

    TacticalActivity activity = repository.findByIdFull(id)
        .orElseThrow(() -> IgrpResponseStatusException.notFound("TacticalActivity não encontrada"));

    // A-132-111 / POR-01 (Phase 134): submission window checked before the state transition,
    // sourced from the loaded entity's paaLevel -- the client cannot pick it (D-27).
    windowPolicy.requireOpenFor(activity.getPaaLevel());

    String previousStatus = activity.getStatus().getCode();
    TacticalActivity submitted = activity.submit();
    repository.save(submitted);

    // A-135-2AB (Phase 136, plano 136-10): rasto de auditoria escrito depois do save, dentro
    // da mesma fronteira @Transactional -- nunca antes, porque gravaria histórico de uma
    // transição que ainda podia falhar.
    historyRecorder.record(submitted.getId(), previousStatus, submitted.getStatus().getCode(),
        submitted.getStatus().getCode(), null);

    ActivityWorkflowResponseDTO response = new ActivityWorkflowResponseDTO();
    response.setActivityId(submitted.getId().getValor().getValor());
    response.setPreviousStatus(previousStatus);
    response.setCurrentStatus(submitted.getStatus().getCode());

    return ResponseEntity.ok(response);
  }
}
