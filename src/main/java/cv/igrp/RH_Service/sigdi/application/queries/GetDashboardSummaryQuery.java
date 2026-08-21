package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Zero-field query: the dashboard summary has no request parameters today. Institution scoping
 * is NOT handled by a BFF/gateway layer -- {@link GetDashboardSummaryQueryHandler} resolves the
 * caller's institution itself, at the application layer, via
 * {@code SecurityContextHelper.getCurrentInstitutionId()} (JWT {@code institution_id} claim).
 * See {@link GetDashboardSummaryQueryHandler}'s class docstring for which of the composed
 * figures are actually institution-scoped today (not all of them are, as of Phase 80 CR-02).
 *
 * NOTE: intentionally does NOT carry @AllArgsConstructor alongside @NoArgsConstructor.
 * On a zero-field class, Lombok would generate two identical no-arg constructors,
 * which is a compile error.
 */
@Data
@NoArgsConstructor
public class GetDashboardSummaryQuery implements Query {
}
