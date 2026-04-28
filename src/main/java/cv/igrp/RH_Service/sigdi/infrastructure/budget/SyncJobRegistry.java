package cv.igrp.RH_Service.sigdi.infrastructure.budget;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SyncJobRegistry {

  public record SyncJobState(
      String jobId,
      String status,
      Integer fiscalYear,
      String scope,
      Integer recordsUpdated,
      LocalDateTime startedAt,
      LocalDateTime completedAt,
      List<String> errors
  ) {}

  private final ConcurrentHashMap<String, SyncJobState> jobs = new ConcurrentHashMap<>();

  public SyncJobState register(String jobId, Integer fiscalYear, String scope) {
    SyncJobState state = new SyncJobState(
        jobId, "QUEUED", fiscalYear, scope, null, LocalDateTime.now(), null, new ArrayList<>());
    jobs.put(jobId, state);
    return state;
  }

  public void markCompleted(String jobId, int recordsUpdated) {
    SyncJobState existing = jobs.get(jobId);
    if (existing != null) {
      jobs.put(jobId, new SyncJobState(
          existing.jobId(), "COMPLETED", existing.fiscalYear(), existing.scope(),
          recordsUpdated, existing.startedAt(), LocalDateTime.now(), existing.errors()));
    }
  }

  public void markFailed(String jobId, String error) {
    SyncJobState existing = jobs.get(jobId);
    if (existing != null) {
      List<String> errors = new ArrayList<>(existing.errors());
      errors.add(error);
      jobs.put(jobId, new SyncJobState(
          existing.jobId(), "FAILED", existing.fiscalYear(), existing.scope(),
          existing.recordsUpdated(), existing.startedAt(), LocalDateTime.now(), errors));
    }
  }

  public Optional<SyncJobState> findById(String jobId) {
    return Optional.ofNullable(jobs.get(jobId));
  }
}
