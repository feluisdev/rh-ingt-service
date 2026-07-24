package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.security.SecurityContextHelper;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

  @Mock
  private SecurityContextHelper securityContextHelper;

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
  void keyResultWithNullCurrentValueIsExcludedFromRiskCountWithoutThrowing() {
    // Deviation (Rule 2): KeyResultsEntity.currentValue has no @NotNull/NOT NULL constraint,
    // so a KR with no recorded check-in yet is a real, reachable state -- guard it the same
    // way as the null/zero-target case rather than let it NPE the whole dashboard endpoint.
    when(budgetSummaryHandler.handle(any(GetBudgetSummaryQuery.class)))
        .thenReturn(ResponseEntity.ok(budgetSummaryWith(BigDecimal.ZERO, BigDecimal.ZERO)));
    when(workflowInboxHandler.handle(any(GetWorkflowInboxQuery.class)))
        .thenReturn(ResponseEntity.ok(inboxWith(0)));
    when(keyResultsEntityRepository.findAll()).thenReturn(List.of(
        keyResult(null, new BigDecimal("100")) // valid positive target, but no current value recorded yet
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

  @Test
  void okrsAtRiskIsScopedToCallersInstitutionNotAggregatedAcrossAllInstitutions() {
    // CR-02 fix: two institutions both have an at-risk KeyResult, but the dashboard for
    // callerInstitutionId must only count its own.
    UUID callerInstitutionId = UUID.randomUUID();
    UUID otherInstitutionId = UUID.randomUUID();

    when(budgetSummaryHandler.handle(any(GetBudgetSummaryQuery.class)))
        .thenReturn(ResponseEntity.ok(budgetSummaryWith(BigDecimal.ZERO, BigDecimal.ZERO)));
    when(workflowInboxHandler.handle(any(GetWorkflowInboxQuery.class)))
        .thenReturn(ResponseEntity.ok(inboxWith(0)));
    when(securityContextHelper.getCurrentInstitutionId()).thenReturn(callerInstitutionId);

    KeyResultsEntity callerAtRisk = keyResult(new BigDecimal("10"), new BigDecimal("100")); // ratio 0.10 -> at risk
    callerAtRisk.setInstitutionId(callerInstitutionId);
    KeyResultsEntity otherInstitutionAtRisk = keyResult(new BigDecimal("5"), new BigDecimal("100")); // ratio 0.05 -> at risk
    otherInstitutionAtRisk.setInstitutionId(otherInstitutionId);

    when(keyResultsEntityRepository.findAllByInstitutionId(callerInstitutionId))
        .thenReturn(List.of(callerAtRisk));
    // lenient: this is a regression trap, only invoked if the handler ever falls back to the
    // unscoped finder despite a resolvable institutionId -- it would then wrongly count both
    // institutions' at-risk KeyResults (2) instead of just the caller's (1).
    lenient().when(keyResultsEntityRepository.findAll())
        .thenReturn(List.of(callerAtRisk, otherInstitutionAtRisk));

    ResponseEntity<DashboardSummaryResponseDTO> response =
        getDashboardSummaryQueryHandler.handle(new GetDashboardSummaryQuery());

    assertEquals(Integer.valueOf(1), response.getBody().getOkrsAtRisk(),
        "must count only the caller's institution's at-risk Key Results, not every institution's");
    verify(keyResultsEntityRepository).findAllByInstitutionId(callerInstitutionId);
    verify(keyResultsEntityRepository, never()).findAll();
  }

  @Test
  void nullBudgetSummaryBodyThrowsStructuredInternalErrorInsteadOfSilentNpe() {
    when(budgetSummaryHandler.handle(any(GetBudgetSummaryQuery.class)))
        .thenReturn(ResponseEntity.<BudgetSummaryResponseDTO>ok(null));
    when(workflowInboxHandler.handle(any(GetWorkflowInboxQuery.class)))
        .thenReturn(ResponseEntity.ok(inboxWith(0)));

    IgrpResponseStatusException ex = assertThrows(IgrpResponseStatusException.class,
        () -> getDashboardSummaryQueryHandler.handle(new GetDashboardSummaryQuery()));

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode(),
        "a null budget summary body must fail loudly with a structured 500, not an unhandled NPE (IN-01)");
  }

  @Test
  void nullWorkflowInboxBodyThrowsStructuredInternalErrorInsteadOfSilentNpe() {
    when(budgetSummaryHandler.handle(any(GetBudgetSummaryQuery.class)))
        .thenReturn(ResponseEntity.ok(budgetSummaryWith(BigDecimal.ZERO, BigDecimal.ZERO)));
    when(workflowInboxHandler.handle(any(GetWorkflowInboxQuery.class)))
        .thenReturn(ResponseEntity.<WrapperWorkflowInboxDTO>ok(null));

    IgrpResponseStatusException ex = assertThrows(IgrpResponseStatusException.class,
        () -> getDashboardSummaryQueryHandler.handle(new GetDashboardSummaryQuery()));

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode(),
        "a null workflow inbox body must fail loudly with a structured 500, not an unhandled NPE (IN-01)");
  }
}
