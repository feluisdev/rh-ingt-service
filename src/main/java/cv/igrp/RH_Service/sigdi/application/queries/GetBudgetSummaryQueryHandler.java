package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.RH_Service.sigdi.application.dto.BudgetAlertItemDTO;
import cv.igrp.RH_Service.sigdi.application.dto.BudgetClassifierSummaryDTO;
import cv.igrp.RH_Service.sigdi.application.dto.BudgetSummaryResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.BudgetSummaryTotalsDTO;
import cv.igrp.RH_Service.sigdi.application.dto.SigofStatusDTO;
import cv.igrp.RH_Service.sigdi.domain.budget.models.FinancialExecutionMirror;
import cv.igrp.RH_Service.sigdi.domain.budget.repository.FinancialExecutionMirrorRepository;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class GetBudgetSummaryQueryHandler
    implements QueryHandler<GetBudgetSummaryQuery, ResponseEntity<BudgetSummaryResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GetBudgetSummaryQueryHandler.class);

  private final FinancialExecutionMirrorRepository mirrorRepository;

  public GetBudgetSummaryQueryHandler(FinancialExecutionMirrorRepository mirrorRepository) {
    this.mirrorRepository = mirrorRepository;
  }

  @IgrpQueryHandler
  @Transactional(readOnly = true)
  public ResponseEntity<BudgetSummaryResponseDTO> handle(GetBudgetSummaryQuery query) {
    LOGGER.debug("GetBudgetSummaryQuery: {}", query);

    int fiscalYear = (query.getFiscalYear() != null && !query.getFiscalYear().isBlank())
        ? Integer.parseInt(query.getFiscalYear())
        : LocalDate.now().getYear();

    List<FinancialExecutionMirror> mirrors;
    if (query.getOrganicUnitId() != null && !query.getOrganicUnitId().isBlank()) {
      mirrors = mirrorRepository.findAllByFiscalYearAndOrganicUnit(fiscalYear, query.getOrganicUnitId());
    } else {
      mirrors = mirrorRepository.findAllByFiscalYear(fiscalYear);
    }

    // Aggregate by classifier
    Map<String, BudgetClassifierSummaryDTO> byClassifier = new LinkedHashMap<>();
    BigDecimal totalCommitted = BigDecimal.ZERO;
    BigDecimal totalLiquidated = BigDecimal.ZERO;
    BigDecimal totalPaid = BigDecimal.ZERO;
    LocalDateTime lastSyncTime = null;

    for (FinancialExecutionMirror m : mirrors) {
      String code = m.getClassifier().getCode();
      BudgetClassifierSummaryDTO entry = byClassifier.computeIfAbsent(code, k -> {
        BudgetClassifierSummaryDTO dto = new BudgetClassifierSummaryDTO();
        dto.setClassifier(code);
        dto.setAllocated(BigDecimal.ZERO);
        dto.setCommitted(BigDecimal.ZERO);
        dto.setLiquidated(BigDecimal.ZERO);
        dto.setPaid(BigDecimal.ZERO);
        dto.setAvailable(BigDecimal.ZERO);
        dto.setActivitiesCount(0);
        return dto;
      });

      entry.setCommitted(entry.getCommitted().add(m.getAmountCommitted()));
      entry.setLiquidated(entry.getLiquidated().add(m.getAmountLiquidated()));
      entry.setPaid(entry.getPaid().add(m.getAmountPaid()));

      totalCommitted = totalCommitted.add(m.getAmountCommitted());
      totalLiquidated = totalLiquidated.add(m.getAmountLiquidated());
      totalPaid = totalPaid.add(m.getAmountPaid());

      if (m.getLastSync() != null && (lastSyncTime == null || m.getLastSync().isAfter(lastSyncTime))) {
        lastSyncTime = m.getLastSync();
      }
    }

    // Compute per-classifier execution rate and alert level
    List<BudgetAlertItemDTO> alerts = new ArrayList<>();
    for (BudgetClassifierSummaryDTO entry : byClassifier.values()) {
      BigDecimal rate = computeRate(entry.getLiquidated(), entry.getCommitted());
      entry.setExecutionRate(rate);
      String alertLevel = resolveAlertLevel(rate);
      entry.setAlertLevel(alertLevel);

      if (!"NONE".equals(alertLevel)) {
        BudgetAlertItemDTO alert = new BudgetAlertItemDTO();
        alert.setClassifier(entry.getClassifier());
        alert.setAlertLevel(alertLevel);
        alert.setLiquidatedPct(rate);
        alert.setMessage(rate.toPlainString() + "% do cabimento liquidado.");
        alerts.add(alert);
      }
    }

    // Sort alerts by severity descending
    alerts.sort(Comparator.comparingInt(a -> -severityRank(a.getAlertLevel())));

    BudgetSummaryTotalsDTO totals = new BudgetSummaryTotalsDTO();
    totals.setAllocated(BigDecimal.ZERO);
    totals.setCommitted(totalCommitted);
    totals.setLiquidated(totalLiquidated);
    totals.setPaid(totalPaid);
    totals.setAvailable(BigDecimal.ZERO);
    totals.setGlobalExecutionRate(computeRate(totalLiquidated, totalCommitted));

    SigofStatusDTO sigofStatus = new SigofStatusDTO();
    sigofStatus.setStatus(lastSyncTime != null ? "ONLINE" : "UNKNOWN");
    sigofStatus.setLastSyncAt(lastSyncTime != null ? lastSyncTime.toString() : null);
    sigofStatus.setNextSyncAt(null);

    BudgetSummaryResponseDTO response = new BudgetSummaryResponseDTO();
    response.setFiscalYear(fiscalYear);
    response.setTotals(totals);
    response.setByClassifier(new ArrayList<>(byClassifier.values()));
    response.setAlerts(alerts);
    response.setSigofStatus(sigofStatus);

    return ResponseEntity.ok(response);
  }

  private BigDecimal computeRate(BigDecimal liquidated, BigDecimal committed) {
    if (committed == null || committed.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
    if (liquidated == null) return BigDecimal.ZERO;
    return liquidated.divide(committed, 4, RoundingMode.HALF_UP)
        .multiply(BigDecimal.valueOf(100))
        .setScale(1, RoundingMode.HALF_UP);
  }

  private String resolveAlertLevel(BigDecimal rate) {
    if (rate.compareTo(new BigDecimal("100")) >= 0) return "BLOCKED";
    if (rate.compareTo(new BigDecimal("90")) >= 0) return "CRITICAL";
    if (rate.compareTo(new BigDecimal("70")) >= 0) return "WARNING";
    return "NONE";
  }

  private int severityRank(String level) {
    return switch (level) {
      case "BLOCKED" -> 4;
      case "CRITICAL" -> 3;
      case "WARNING" -> 2;
      default -> 1;
    };
  }
}
