package cv.igrp.RH_Service.sigdi.domain.budget.models;

import cv.igrp.RH_Service.sigdi.domain.budget.valueobject.FinancialExecutionMirrorId;
import cv.igrp.RH_Service.sigdi.domain.shared.valueobject.EconomicClassifier;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Getter
public class FinancialExecutionMirror {

  // RN03 — thresholds de bloqueio
  private static final BigDecimal SOFT_LOCK_THRESHOLD = new BigDecimal("0.90"); // 90%
  private static final BigDecimal HARD_LOCK_THRESHOLD = new BigDecimal("1.00"); // 100%

  private final FinancialExecutionMirrorId id;
  private final EconomicClassifier classifier;
  private final String organicUnit;
  private final Integer fiscalYear;
  private final BigDecimal amountCommitted;
  private final BigDecimal amountLiquidated;
  private final BigDecimal amountPaid;
  private final LocalDateTime lastSync;

  private FinancialExecutionMirror(FinancialExecutionMirrorId id, EconomicClassifier classifier,
                                   String organicUnit, Integer fiscalYear,
                                   BigDecimal amountCommitted, BigDecimal amountLiquidated,
                                   BigDecimal amountPaid, LocalDateTime lastSync) {
    if (classifier == null) throw new IllegalArgumentException("classifier é obrigatório");
    if (organicUnit == null || organicUnit.isBlank()) throw new IllegalArgumentException("organicUnit é obrigatório");
    if (fiscalYear == null) throw new IllegalArgumentException("fiscalYear é obrigatório");

    this.id = id;
    this.classifier = classifier;
    this.organicUnit = organicUnit;
    this.fiscalYear = fiscalYear;
    this.amountCommitted = (amountCommitted != null) ? amountCommitted : BigDecimal.ZERO;
    this.amountLiquidated = (amountLiquidated != null) ? amountLiquidated : BigDecimal.ZERO;
    this.amountPaid = (amountPaid != null) ? amountPaid : BigDecimal.ZERO;
    this.lastSync = lastSync;
  }

  public static FinancialExecutionMirror create(EconomicClassifier classifier,
                                                String organicUnit, Integer fiscalYear) {
    return new FinancialExecutionMirror(
        FinancialExecutionMirrorId.gerarNovo(),
        classifier, organicUnit, fiscalYear,
        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, null
    );
  }

  public static FinancialExecutionMirror reconstruct(FinancialExecutionMirrorId id,
                                                     EconomicClassifier classifier,
                                                     String organicUnit, Integer fiscalYear,
                                                     BigDecimal amountCommitted,
                                                     BigDecimal amountLiquidated,
                                                     BigDecimal amountPaid,
                                                     LocalDateTime lastSync) {
    return new FinancialExecutionMirror(id, classifier, organicUnit, fiscalYear,
        amountCommitted, amountLiquidated, amountPaid, lastSync);
  }

  // ── Regras de negócio ─────────────────────────────────────────────

  /**
   * Retorna o rácio de execução: amountLiquidated / amountCommitted
   */
  public BigDecimal getExecutionRatio() {
    if (amountCommitted.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
    return amountLiquidated.divide(amountCommitted, 4, RoundingMode.HALF_UP);
  }

  /**
   * RN03 — Alerta visual quando liquidado >= 90% do cabimentado
   */
  public boolean isSoftLocked() {
    return getExecutionRatio().compareTo(SOFT_LOCK_THRESHOLD) >= 0;
  }

  /**
   * RN03 — Bloqueia novos pedidos quando liquidado >= 100% do cabimentado
   */
  public boolean isHardLocked() {
    return getExecutionRatio().compareTo(HARD_LOCK_THRESHOLD) >= 0;
  }

  public BigDecimal getAvailableBalance(BigDecimal totalDotation) {
    return totalDotation.subtract(amountCommitted);
  }

  public boolean hasAvailableBalance(BigDecimal totalDotation, BigDecimal requestedAmount) {
    return getAvailableBalance(totalDotation).compareTo(requestedAmount) >= 0;
  }

  public FinancialExecutionMirror sync(BigDecimal newCommitted, BigDecimal newLiquidated,
                                       BigDecimal newPaid) {
    return new FinancialExecutionMirror(
        this.id, this.classifier, this.organicUnit, this.fiscalYear,
        newCommitted, newLiquidated, newPaid, LocalDateTime.now()
    );
  }

  public boolean isStale(int maxHours) {
    if (lastSync == null) return true;
    return lastSync.isBefore(LocalDateTime.now().minusHours(maxHours));
  }
}
