package cv.igrp.RH_Service.sigdi.domain.tatical.repository;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Read projection of a pending {@code ChangeRequest}, for the workflow inbox listing.
 *
 * <p>This is not the domain aggregate: {@code ChangeRequest} deliberately has no
 * {@code createdBy}/{@code createdDate} (D-R, {@code 110-01-PLAN.md}), so this record reads
 * those audit fields directly off the entity. The long free-text reason field on the request
 * is intentionally omitted: D-B limits the DTO this projection feeds to four new fields, and
 * no consumer reads it.
 */
public record PendingChangeRequestRow(
    UUID id,
    UUID activityId,
    String activityTitle,
    String fieldName,
    String currentValue,
    String proposedValue,
    String requestedBy,
    LocalDateTime requestedAt
) {
}
