package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.RH_Service.sigdi.application.dto.DashboardSummaryResponseDTO;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.KeyResultsEntityRepository;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GetDashboardSummaryQueryHandler
    implements QueryHandler<GetDashboardSummaryQuery, ResponseEntity<DashboardSummaryResponseDTO>> {

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

  // RED phase placeholder (80-01 Task 2, tdd=true) -- composition intentionally NOT implemented
  // yet, so GetDashboardSummaryQueryHandlerTest fails until the real logic replaces this method body.
  @IgrpQueryHandler
  @Transactional(readOnly = true)
  public ResponseEntity<DashboardSummaryResponseDTO> handle(GetDashboardSummaryQuery query) {
    return ResponseEntity.ok(new DashboardSummaryResponseDTO());
  }
}
