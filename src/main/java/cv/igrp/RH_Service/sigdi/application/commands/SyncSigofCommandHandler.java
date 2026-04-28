package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.sigdi.application.dto.SyncJobResponseDTO;
import cv.igrp.RH_Service.sigdi.infrastructure.budget.SyncJobRegistry;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SyncSigofCommandHandler
    implements CommandHandler<SyncSigofCommand, ResponseEntity<SyncJobResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(SyncSigofCommandHandler.class);

  private final SyncJobRegistry syncJobRegistry;

  public SyncSigofCommandHandler(SyncJobRegistry syncJobRegistry) {
    this.syncJobRegistry = syncJobRegistry;
  }

  @IgrpCommandHandler
  public ResponseEntity<SyncJobResponseDTO> handle(SyncSigofCommand command) {
    LOGGER.debug("SyncSigofCommand: {}", command);

    String jobId = UUID.randomUUID().toString();
    Integer fiscalYear = command.getBody().getFiscalYear();
    String scope = command.getBody().getScope();

    SyncJobRegistry.SyncJobState job = syncJobRegistry.register(jobId, fiscalYear, scope);

    // Simulate async processing (mark completed immediately for now)
    syncJobRegistry.markCompleted(jobId, 0);

    SyncJobResponseDTO response = new SyncJobResponseDTO();
    response.setSyncJobId(jobId);
    response.setStatus("QUEUED");
    response.setEstimatedDurationSeconds(120);
    response.setStartedAt(job.startedAt().toString());

    return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
  }
}
