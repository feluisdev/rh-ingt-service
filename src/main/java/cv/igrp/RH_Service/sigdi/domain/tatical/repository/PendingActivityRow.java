package cv.igrp.RH_Service.sigdi.domain.tatical.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Read projection of a pending {@code TacticalActivity}, for the workflow inbox listing.
 *
 * <p>This is not the domain aggregate: {@code TacticalActivity} deliberately has no
 * {@code createdBy}/{@code createdDate} (D-R, {@code 110-01-PLAN.md}), so this record reads
 * those audit fields directly off the entity to serve the inbox without touching the aggregate.
 */
public record PendingActivityRow(
    UUID id,
    String title,
    String status,
    BigDecimal budgetEstimated,
    String economicClassifier,
    String requestedBy,
    LocalDateTime requestedAt
) {
}
