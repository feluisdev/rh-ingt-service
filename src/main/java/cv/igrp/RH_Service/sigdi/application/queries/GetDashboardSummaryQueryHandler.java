package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.RH_Service.sigdi.application.dto.DashboardSummaryResponseDTO;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.KeyResultsEntityRepository;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Composes the executive dashboard summary from 3 already-correct sibling sources, by direct
 * constructor injection of the 2 sibling query handler beans (not the QueryBus -- see
 * 80-RESEARCH.md Pattern 1 / 80-PATTERNS.md "Shared Patterns #3") plus one unscoped KeyResult scan.
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

  public GetDashboardSummaryQueryHandler(GetBudgetSummaryQueryHandler budgetSummaryHandler,
                                          GetWorkflowInboxQueryHandler workflowInboxHandler,
                                          KeyResultsEntityRepository keyResultsEntityRepository) {
    this.budgetSummaryHandler = budgetSummaryHandler;
    this.workflowInboxHandler = workflowInboxHandler;
    this.keyResultsEntityRepository = keyResultsEntityRepository;
  }

  @IgrpQueryHandler
  @Transactional(readOnly = true)
  public ResponseEntity<DashboardSummaryResponseDTO> handle(GetDashboardSummaryQuery query) {
    var budgetSummary = budgetSummaryHandler.handle(new GetBudgetSummaryQuery(null, null)).getBody();
    // pageSize=1: only totalElements (the pending-approvals count) is read from this call.
    var inbox = workflowInboxHandler.handle(new GetWorkflowInboxQuery("0", "1")).getBody();

    // Intentionally unscoped findAll(): no fiscal-year filter infrastructure exists yet for
    // KeyResults (80-RESEARCH.md Open Question #3). Matches this codebase's own established
    // aggregate-in-Java pattern (GetBudgetSummaryQueryHandler) -- KeyResult volume is small and
    // bounded in this domain today.
    long okrsAtRisk = keyResultsEntityRepository.findAll().stream()
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
