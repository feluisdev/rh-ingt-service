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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RejectTacticalActivityCommandHandler
    implements CommandHandler<RejectTacticalActivityCommand, ResponseEntity<ActivityWorkflowResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(RejectTacticalActivityCommandHandler.class);

  private final TacticalActivityRepository repository;

  public RejectTacticalActivityCommandHandler(TacticalActivityRepository repository) {
    this.repository = repository;
  }

  @IgrpCommandHandler
  @Transactional
  public ResponseEntity<ActivityWorkflowResponseDTO> handle(RejectTacticalActivityCommand command) {
    LOGGER.debug("RejectTacticalActivityCommand: {}", command);

    TacticalActivityId id = TacticalActivityId.from(command.getId());

    TacticalActivity activity = repository.findByIdFull(id)
        .orElseThrow(() -> IgrpResponseStatusException.notFound("TacticalActivity não encontrada"));

    String comment = (command.getWorkflowcomment() != null) ? command.getWorkflowcomment().getComment() : null;
    if (comment == null || comment.isBlank()) {
      throw IgrpResponseStatusException.of(HttpStatus.BAD_REQUEST, "Comentário é obrigatório para rejeitar");
    }

    String previousStatus = activity.getStatus().getCode();
    TacticalActivity rejected = activity.reject();
    repository.save(rejected);

    ActivityWorkflowResponseDTO response = new ActivityWorkflowResponseDTO();
    response.setActivityId(rejected.getId().getValor().getValor());
    response.setPreviousStatus(previousStatus);
    response.setCurrentStatus(rejected.getStatus().getCode());
    response.setComment(comment);

    return ResponseEntity.ok(response);
  }
}
