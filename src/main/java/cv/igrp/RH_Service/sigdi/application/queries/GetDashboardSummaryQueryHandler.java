package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.security.SecurityContextHelper;
import cv.igrp.RH_Service.sigdi.application.dto.DashboardSummaryResponseDTO;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.KeyResultsEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.KeyResultsEntityRepository;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

/**
 * Composes the executive dashboard summary from 3 sources, by direct constructor injection of
 * the 2 sibling query handler beans (not the QueryBus -- see 80-RESEARCH.md Pattern 1 /
 * 80-PATTERNS.md "Shared Patterns #3") plus one institution-scoped KeyResult scan.
 *
 * <p>Institution-scoping guarantees are NOT uniform across the 3 figures composed here:
 * <ul>
 *   <li>{@code okrsAtRisk} IS scoped to the caller's institution via {@link SecurityContextHelper}
 *       (JWT {@code institution_id} claim, enforced at the application layer -- Phase 80 CR-02
 *       fix; this replaced a cross-tenant data leak where every institution's dashboard showed
 *       an OKR-at-risk count aggregated across every institution in the system).</li>
 *   <li>{@code executionRate}/{@code availableBudget} (from {@link GetBudgetSummaryQueryHandler})
 *       and {@code pendingApprovals} (from {@link GetWorkflowInboxQueryHandler}) are NOT
 *       institution-scoped today: neither sibling handler has institution-scoping capability
 *       (the underlying {@code FinancialExecutionMirror} has no {@code institutionId} field, and
 *       the tactical-activity repository backing the inbox count takes no institution
 *       parameter). This is a pre-existing, wider gap in those two handlers, tracked separately
 *       and out of scope for the CR-02 fix -- do not assume this endpoint's figures are
 *       uniformly tenant-safe.</li>
 * </ul>
 */
@Component
public class GetDashboardSummaryQueryHandler
    implements QueryHandler<GetDashboardSummaryQuery, ResponseEntity<DashboardSummaryResponseDTO>> {

  // CONTEXT.md-locked provisional heuristic: a KeyResult counts as "at risk" when its
  // currentValue/targetValue ratio falls below this threshold. Documented explicitly as an
  // initial heuristic, not a business rule validated with the user (80-CONTEXT.md ERRO-01).
  private static final BigDecimal OKR_RISK_RATIO_THRESHOLD = new BigDecimal("0.5");

  private final GetBudgetSummaryQueryHandler budgetSummaryHandler;
  private final GetWorkflowInboxQueryHandler workflowInboxHandler;
  private final KeyResultsEntityRepository keyResultsEntityRepository;
  private final SecurityContextHelper securityContextHelper;

  public GetDashboardSummaryQueryHandler(GetBudgetSummaryQueryHandler budgetSummaryHandler,
                                          GetWorkflowInboxQueryHandler workflowInboxHandler,
                                          KeyResultsEntityRepository keyResultsEntityRepository,
                                          SecurityContextHelper securityContextHelper) {
    this.budgetSummaryHandler = budgetSummaryHandler;
    this.workflowInboxHandler = workflowInboxHandler;
    this.keyResultsEntityRepository = keyResultsEntityRepository;
    this.securityContextHelper = securityContextHelper;
  }

  @IgrpQueryHandler
  @Transactional(readOnly = true)
  public ResponseEntity<DashboardSummaryResponseDTO> handle(GetDashboardSummaryQuery query) {
    var budgetSummary = budgetSummaryHandler.handle(new GetBudgetSummaryQuery(null, null)).getBody();
    // pageSize=1: only totalElements (the pending-approvals count) is read from this call.
    var inbox = workflowInboxHandler.handle(new GetWorkflowInboxQuery("0", "1")).getBody();

    // IN-01: fail loudly instead of silently 500ing if a sibling handler's contract ever
    // regresses to a null/partial response. Both are documented today to always return a
    // fully-populated body, but this composition boundary previously had no guard of its own.
    if (budgetSummary == null || budgetSummary.getTotals() == null) {
      throw IgrpResponseStatusException.internalServerError(
          "Dashboard summary composition failed: budget summary handler returned an incomplete response");
    }
    if (inbox == null) {
      throw IgrpResponseStatusException.internalServerError(
          "Dashboard summary composition failed: workflow inbox handler returned an incomplete response");
    }

    // CR-02 fix: KeyResults are institution-scoped (t_key_results.institution_id). Resolve the
    // caller's institution once via the JWT institution_id claim and filter by it so one
    // institution's dashboard never aggregates another institution's OKRs (this was a
    // cross-tenant data leak). getCurrentInstitutionId() only returns null in its own documented
    // edge case (a request with no resolvable institution claim outside dev/staging); when that
    // happens, fall back to the prior unscoped findAll() rather than inventing new
    // default-institution logic here.
    UUID institutionId = securityContextHelper.getCurrentInstitutionId();
    List<KeyResultsEntity> keyResults = institutionId != null
        ? keyResultsEntityRepository.findAllByInstitutionId(institutionId)
        : keyResultsEntityRepository.findAll();

    long okrsAtRisk = keyResults.stream()
        .filter(kr -> kr.getCurrentValue() != null
            && kr.getTargetValue() != null
            && kr.getTargetValue().compareTo(BigDecimal.ZERO) > 0)
        .filter(kr -> kr.getCurrentValue().divide(kr.getTargetValue(), 4, RoundingMode.HALF_UP)
            .compareTo(OKR_RISK_RATIO_THRESHOLD) < 0)
        .count();

    DashboardSummaryResponseDTO response = new DashboardSummaryResponseDTO();
    response.setExecutionRate(budgetSummary.getTotals().getGlobalExecutionRate());
    // NOTE: always 0 today. FinancialExecutionMirror has no allocated/dotation field, so
    // GetBudgetSummaryQueryHandler itself hardcodes totals.available to BigDecimal.ZERO
    // (80-RESEARCH.md Open Question #1). This is a pre-existing data-model limitation, NOT a
    // Phase 80 regression -- this handler faithfully passes the value through rather than
    // papering over the gap with an invented number.
    response.setAvailableBudget(budgetSummary.getTotals().getAvailable());
    response.setOkrsAtRisk((int) okrsAtRisk);
    response.setPendingApprovals(inbox.getTotalElements());
    return ResponseEntity.ok(response);
  }
}
