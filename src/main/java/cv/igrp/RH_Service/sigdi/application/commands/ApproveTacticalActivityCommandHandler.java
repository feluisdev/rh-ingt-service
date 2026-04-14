package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.dto.ActivityWorkflowResponseDTO;
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
public class ApproveTacticalActivityCommandHandler
    implements CommandHandler<ApproveTacticalActivityCommand, ResponseEntity<ActivityWorkflowResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApproveTacticalActivityCommandHandler.class);

  private final TacticalActivityRepository repository;

  public ApproveTacticalActivityCommandHandler(TacticalActivityRepository repository) {
    this.repository = repository;
  }

  @IgrpCommandHandler
  @Transactional
  public ResponseEntity<ActivityWorkflowResponseDTO> handle(ApproveTacticalActivityCommand command) {
    LOGGER.debug("ApproveTacticalActivityCommand: {}", command);

    TacticalActivityId id = TacticalActivityId.from(command.getId());

    TacticalActivity activity = repository.findByIdFull(id)
        .orElseThrow(() -> IgrpResponseStatusException.notFound("TacticalActivity não encontrada"));

    String previousStatus = activity.getStatus().getCode();
    TacticalActivity approved = activity.approve();
    repository.save(approved);

    String comment = (command.getWorkflowcomment() != null) ? command.getWorkflowcomment().getComment() : null;

    ActivityWorkflowResponseDTO response = new ActivityWorkflowResponseDTO();
    response.setActivityId(approved.getId().getValor().getValor());
    response.setPreviousStatus(previousStatus);
    response.setCurrentStatus(approved.getStatus().getCode());
    response.setComment(comment);

    return ResponseEntity.ok(response);
  }
}
