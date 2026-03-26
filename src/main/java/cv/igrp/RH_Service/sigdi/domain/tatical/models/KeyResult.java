package cv.igrp.RH_Service.sigdi.domain.tatical.models;

import cv.igrp.RH_Service.sigdi.application.constants.KeyResultMetricUnit;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.KeyResultId;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.TacticalActivityId;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class KeyResult {

  // US004 — alerta crítico quando progresso < 30% no último mês do trimestre
  private static final BigDecimal RISK_THRESHOLD = new BigDecimal("0.30");

  private final KeyResultId id;
  private final TacticalActivityId activityId;
  private final String title;
  private final BigDecimal targetValue;
  private final BigDecimal currentValue;
  private final KeyResultMetricUnit metricUnit;
  private final List<KeyResultCheckin> checkins;

  private KeyResult(KeyResultId id, TacticalActivityId activityId, String title,
                    BigDecimal targetValue, BigDecimal currentValue,
                    KeyResultMetricUnit metricUnit,
                    List<KeyResultCheckin> checkins) {
    if (title == null || title.isBlank()) throw new IllegalArgumentException("title é obrigatório");
    if (targetValue == null || targetValue.compareTo(BigDecimal.ZERO) <= 0)
      throw new IllegalArgumentException("targetValue deve ser maior que zero");
    if (activityId == null) throw new IllegalArgumentException("activityId é obrigatório");

    this.id = id;
    this.activityId = activityId;
    this.title = title;
    this.targetValue = targetValue;
    this.currentValue = (currentValue != null) ? currentValue : BigDecimal.ZERO;
    this.metricUnit = metricUnit;
    this.checkins = (checkins != null) ? new ArrayList<>(checkins) : new ArrayList<>();
  }

  public static KeyResult create(TacticalActivityId activityId, String title,
                                 BigDecimal targetValue, KeyResultMetricUnit metricUnit) {
    return new KeyResult(KeyResultId.gerarNovo(), activityId, title, targetValue,
        BigDecimal.ZERO, metricUnit, new ArrayList<>());
  }

  public static KeyResult reconstruct(KeyResultId id, TacticalActivityId activityId, String title,
                                      BigDecimal targetValue, BigDecimal currentValue,
                                      KeyResultMetricUnit metricUnit,
                                      List<KeyResultCheckin> checkins) {
    return new KeyResult(id, activityId, title, targetValue, currentValue,
        metricUnit, checkins);
  }

  public List<KeyResultCheckin> getCheckins() {
    return Collections.unmodifiableList(checkins);
  }

  // ── Regras de negócio ─────────────────────────────────────────────

  public KeyResultCheckin addCheckin(BigDecimal valueAdded, String evidenceUrl, String comment) {
    KeyResultCheckin checkin = KeyResultCheckin.create(this.id, valueAdded, evidenceUrl, comment);
    checkins.add(checkin);
    return checkin;
  }

  /**
   * RN02 — Progresso percentual do KR: currentValue / targetValue
   */
  public BigDecimal getProgressPercentage() {
    if (targetValue.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
    return currentValue.divide(targetValue, 4, RoundingMode.HALF_UP)
        .multiply(BigDecimal.valueOf(100));
  }

  public boolean isCompleted() {
    return currentValue.compareTo(targetValue) >= 0;
  }

  /**
   * US004 — Alerta crítico: progresso < 30% no último mês do período
   */
  public boolean isCriticalRisk(LocalDate periodEndDate) {
    LocalDate oneMonthBefore = periodEndDate.minusMonths(1);
    boolean isLastMonth = !LocalDate.now().isBefore(oneMonthBefore);
    boolean isBelowThreshold = getProgressPercentage()
        .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
        .compareTo(RISK_THRESHOLD) < 0;
    return isLastMonth && isBelowThreshold;
  }

  /**
   * Retorna nova instância com currentValue atualizado
   */
  public KeyResult applyCheckin(BigDecimal valueAdded) {
    BigDecimal newValue = this.currentValue.add(valueAdded);
    return new KeyResult(this.id, this.activityId, this.title, this.targetValue,
        newValue, this.metricUnit, this.checkins);
  }
}
