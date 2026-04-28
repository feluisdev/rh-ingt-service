package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.RH_Service.sigdi.application.dto.BudgetAvailabilityResponseDTO;
import cv.igrp.RH_Service.sigdi.domain.budget.models.FinancialExecutionMirror;
import cv.igrp.RH_Service.sigdi.domain.budget.repository.FinancialExecutionMirrorRepository;
import cv.igrp.RH_Service.sigdi.domain.shared.valueobject.EconomicClassifier;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class GetBudgetAvailabilityQueryHandler
    implements QueryHandler<GetBudgetAvailabilityQuery, ResponseEntity<BudgetAvailabilityResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GetBudgetAvailabilityQueryHandler.class);

  private final FinancialExecutionMirrorRepository mirrorRepository;

  public GetBudgetAvailabilityQueryHandler(FinancialExecutionMirrorRepository mirrorRepository) {
    this.mirrorRepository = mirrorRepository;
  }

  @IgrpQueryHandler
  @Transactional(readOnly = true)
  public ResponseEntity<BudgetAvailabilityResponseDTO> handle(GetBudgetAvailabilityQuery query) {
    LOGGER.debug("GetBudgetAvailabilityQuery: {}", query);

    EconomicClassifier classifier = EconomicClassifier.of(query.getClassifier());

    Optional<FinancialExecutionMirror> mirrorOpt = mirrorRepository.findByKey(
        classifier, query.getOrganicUnitId(), query.getFiscalYear());

    BudgetAvailabilityResponseDTO response = new BudgetAvailabilityResponseDTO();
    response.setClassifier(query.getClassifier());
    response.setOrganicUnitId(query.getOrganicUnitId());
    response.setFiscalYear(query.getFiscalYear());

    if (mirrorOpt.isPresent()) {
      FinancialExecutionMirror mirror = mirrorOpt.get();
      response.setCommitted(mirror.getAmountCommitted());
      response.setLiquidated(mirror.getAmountLiquidated());
      response.setPaid(mirror.getAmountPaid());
      response.setBudgetAllocated(BigDecimal.ZERO);
      response.setAvailable(BigDecimal.ZERO);
      response.setExecutionRate(computeExecutionRate(mirror.getAmountLiquidated(), mirror.getAmountCommitted()));
      response.setAlertLevel(resolveAlertLevel(mirror));
      response.setDataSource("SIGOF_CACHED");
      response.setLastSyncAt(mirror.getLastSync() != null ? mirror.getLastSync().toString() : null);
    } else {
      response.setCommitted(BigDecimal.ZERO);
      response.setLiquidated(BigDecimal.ZERO);
      response.setPaid(BigDecimal.ZERO);
      response.setBudgetAllocated(BigDecimal.ZERO);
      response.setAvailable(BigDecimal.ZERO);
      response.setExecutionRate(BigDecimal.ZERO);
      response.setAlertLevel("NONE");
      response.setDataSource("SIGOF_CACHED");
    }

    if (query.getRequestedAmount() != null && !query.getRequestedAmount().isBlank()) {
      BigDecimal requested = new BigDecimal(query.getRequestedAmount());
      response.setRequestedAmount(requested);
      response.setRequestedAmountFeasible(
          response.getAvailable().compareTo(requested) >= 0);
    }

    return ResponseEntity.ok(response);
  }

  private BigDecimal computeExecutionRate(BigDecimal liquidated, BigDecimal committed) {
    if (committed == null || committed.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
    if (liquidated == null) return BigDecimal.ZERO;
    return liquidated.divide(committed, 4, RoundingMode.HALF_UP)
        .multiply(BigDecimal.valueOf(100))
        .setScale(1, RoundingMode.HALF_UP);
  }

  private String resolveAlertLevel(FinancialExecutionMirror mirror) {
    if (mirror.isHardLocked()) return "BLOCKED";
    if (mirror.isSoftLocked()) return "CRITICAL";
    BigDecimal ratio = mirror.getExecutionRatio();
    if (ratio.compareTo(new BigDecimal("0.70")) >= 0) return "WARNING";
    return "NONE";
  }
}
