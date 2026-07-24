package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.framework.core.domain.Query;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Zero-field query: the dashboard summary has no request parameters today —
 * tenant/institution scoping is handled by the BFF/gateway layer, not this query.
 *
 * NOTE: intentionally does NOT carry @AllArgsConstructor alongside @NoArgsConstructor.
 * On a zero-field class, Lombok would generate two identical no-arg constructors,
 * which is a compile error.
 */
@Data
@NoArgsConstructor
public class GetDashboardSummaryQuery implements Query {
}
