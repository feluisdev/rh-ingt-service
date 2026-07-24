package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.RH_Service.sigdi.application.dto.BudgetSummaryResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.BudgetSummaryTotalsDTO;
import cv.igrp.RH_Service.sigdi.application.dto.DashboardSummaryResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.WrapperWorkflowInboxDTO;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.KeyResultsEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.KeyResultsEntityRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Covers the 5 behavior cases from 80-01-PLAN.md Task 2:
 * 1. executionRate is a real pass-through from GetBudgetSummaryQueryHandler.
 * 2. pendingApprovals is a real pass-through from GetWorkflowInboxQueryHandler.
 * 3. okrsAtRisk counts only KeyResults whose current/target ratio is below the 0.5 threshold.
 * 4. A KeyResult with a null or zero targetValue is excluded (no divide-by-zero, no NPE).
 * 5. availableBudget passes through the budget handler's totals.available verbatim (not recomputed).
 */
@ExtendWith(MockitoExtension.class)
class GetDashboardSummaryQueryHandlerTest {

  @Mock
  private GetBudgetSummaryQueryHandler budgetSummaryHandler;

  @Mock
  private GetWorkflowInboxQueryHandler workflowInboxHandler;

  @Mock
  private KeyResultsEntityRepository keyResultsEntityRepository;

  @InjectMocks
  private GetDashboardSummaryQueryHandler getDashboardSummaryQueryHandler;

  private static BudgetSummaryResponseDTO budgetSummaryWith(BigDecimal globalExecutionRate, BigDecimal available) {
    BudgetSummaryTotalsDTO totals = new BudgetSummaryTotalsDTO();
    totals.setAllocated(BigDecimal.ZERO);
    totals.setCommitted(BigDecimal.ZERO);
    totals.setLiquidated(BigDecimal.ZERO);
    totals.setPaid(BigDecimal.ZERO);
    totals.setAvailable(available);
    totals.setGlobalExecutionRate(globalExecutionRate);

    BudgetSummaryResponseDTO response = new BudgetSummaryResponseDTO();
    response.setTotals(totals);
    return response;
  }

  private static WrapperWorkflowInboxDTO inboxWith(long totalElements) {
    WrapperWorkflowInboxDTO inbox = new WrapperWorkflowInboxDTO();
    inbox.setTotalElements(totalElements);
    return inbox;
  }

  private static KeyResultsEntity keyResult(BigDecimal currentValue, BigDecimal targetValue) {
    KeyResultsEntity keyResultsEntity = new KeyResultsEntity();
    keyResultsEntity.setCurrentValue(currentValue);
    keyResultsEntity.setTargetValue(targetValue);
    return keyResultsEntity;
  }

  @Test
  void executionRateIsRealPassThroughFromBudgetSummary() {
    when(budgetSummaryHandler.handle(any(GetBudgetSummaryQuery.class)))
        .thenReturn(ResponseEntity.ok(budgetSummaryWith(new BigDecimal("0.42"), BigDecimal.ZERO)));
    when(workflowInboxHandler.handle(any(GetWorkflowInboxQuery.class)))
        .thenReturn(ResponseEntity.ok(inboxWith(0)));
    when(keyResultsEntityRepository.findAll()).thenReturn(List.of());

    ResponseEntity<DashboardSummaryResponseDTO> response =
        getDashboardSummaryQueryHandler.handle(new GetDashboardSummaryQuery());

    assertEquals(new BigDecimal("0.42"), response.getBody().getExecutionRate());
  }

  @Test
  void pendingApprovalsIsRealPassThroughFromWorkflowInbox() {
    when(budgetSummaryHandler.handle(any(GetBudgetSummaryQuery.class)))
        .thenReturn(ResponseEntity.ok(budgetSummaryWith(BigDecimal.ZERO, BigDecimal.ZERO)));
    when(workflowInboxHandler.handle(any(GetWorkflowInboxQuery.class)))
        .thenReturn(ResponseEntity.ok(inboxWith(7)));
    when(keyResultsEntityRepository.findAll()).thenReturn(List.of());

    ResponseEntity<DashboardSummaryResponseDTO> response =
        getDashboardSummaryQueryHandler.handle(new GetDashboardSummaryQuery());

    assertEquals(Long.valueOf(7L), response.getBody().getPendingApprovals());
  }

  @Test
  void okrsAtRiskCountsOnlyRatiosBelowHalfThreshold() {
    when(budgetSummaryHandler.handle(any(GetBudgetSummaryQuery.class)))
        .thenReturn(ResponseEntity.ok(budgetSummaryWith(BigDecimal.ZERO, BigDecimal.ZERO)));
    when(workflowInboxHandler.handle(any(GetWorkflowInboxQuery.class)))
        .thenReturn(ResponseEntity.ok(inboxWith(0)));
    when(keyResultsEntityRepository.findAll()).thenReturn(List.of(
        keyResult(new BigDecimal("10"), new BigDecimal("100")), // ratio 0.10 -> at risk
        keyResult(new BigDecimal("90"), new BigDecimal("100"))  // ratio 0.90 -> not at risk
    ));

    ResponseEntity<DashboardSummaryResponseDTO> response =
        getDashboardSummaryQueryHandler.handle(new GetDashboardSummaryQuery());

    assertEquals(Integer.valueOf(1), response.getBody().getOkrsAtRisk());
  }

  @Test
  void keyResultWithNullOrZeroTargetIsExcludedFromRiskCountWithoutThrowing() {
    when(budgetSummaryHandler.handle(any(GetBudgetSummaryQuery.class)))
        .thenReturn(ResponseEntity.ok(budgetSummaryWith(BigDecimal.ZERO, BigDecimal.ZERO)));
    when(workflowInboxHandler.handle(any(GetWorkflowInboxQuery.class)))
        .thenReturn(ResponseEntity.ok(inboxWith(0)));
    when(keyResultsEntityRepository.findAll()).thenReturn(List.of(
        keyResult(new BigDecimal("5"), null),          // null target -> excluded, no NPE
        keyResult(new BigDecimal("5"), BigDecimal.ZERO) // zero target -> excluded, no divide-by-zero
    ));

    ResponseEntity<DashboardSummaryResponseDTO> response = assertDoesNotThrow(
        () -> getDashboardSummaryQueryHandler.handle(new GetDashboardSummaryQuery()));

    assertEquals(Integer.valueOf(0), response.getBody().getOkrsAtRisk());
  }

  @Test
  void availableBudgetPassesThroughVerbatimNotRecomputed() {
    BigDecimal distinctiveAvailable = new BigDecimal("500.00");
    when(budgetSummaryHandler.handle(any(GetBudgetSummaryQuery.class)))
        .thenReturn(ResponseEntity.ok(budgetSummaryWith(BigDecimal.ZERO, distinctiveAvailable)));
    when(workflowInboxHandler.handle(any(GetWorkflowInboxQuery.class)))
        .thenReturn(ResponseEntity.ok(inboxWith(0)));
    when(keyResultsEntityRepository.findAll()).thenReturn(List.of());

    ResponseEntity<DashboardSummaryResponseDTO> response =
        getDashboardSummaryQueryHandler.handle(new GetDashboardSummaryQuery());

    assertEquals(distinctiveAvailable, response.getBody().getAvailableBudget());
  }
}
