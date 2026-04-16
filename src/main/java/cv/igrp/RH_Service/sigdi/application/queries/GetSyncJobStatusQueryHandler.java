package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.dto.SyncJobStatusResponseDTO;
import cv.igrp.RH_Service.sigdi.infrastructure.budget.SyncJobRegistry;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class GetSyncJobStatusQueryHandler
    implements QueryHandler<GetSyncJobStatusQuery, ResponseEntity<SyncJobStatusResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GetSyncJobStatusQueryHandler.class);

  private final SyncJobRegistry syncJobRegistry;

  public GetSyncJobStatusQueryHandler(SyncJobRegistry syncJobRegistry) {
    this.syncJobRegistry = syncJobRegistry;
  }

  @IgrpQueryHandler
  public ResponseEntity<SyncJobStatusResponseDTO> handle(GetSyncJobStatusQuery query) {
    LOGGER.debug("GetSyncJobStatusQuery: {}", query);

    SyncJobRegistry.SyncJobState job = syncJobRegistry.findById(query.getJobId())
        .orElseThrow(() -> IgrpResponseStatusException.notFound("Sync job not found: " + query.getJobId()));

    SyncJobStatusResponseDTO response = new SyncJobStatusResponseDTO();
    response.setSyncJobId(job.jobId());
    response.setStatus(job.status());
    response.setRecordsUpdated(job.recordsUpdated());
    response.setStartedAt(job.startedAt() != null ? job.startedAt().toString() : null);
    response.setCompletedAt(job.completedAt() != null ? job.completedAt().toString() : null);
    response.setErrors(job.errors());

    return ResponseEntity.ok(response);
  }
}
