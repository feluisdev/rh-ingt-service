package cv.igrp.RH_Service.sigdi.domain.tatical.models;

import cv.igrp.RH_Service.sigdi.application.constants.KeyResultMetricUnit;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.KeyResultId;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.OkrId;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
public class OkrKeyResult {

  private final KeyResultId id;
  private final OkrId okrId;
  private final String title;
  private final BigDecimal targetValue;
  private final BigDecimal currentValue;
  private final KeyResultMetricUnit unit;
  private final BigDecimal weight;

  private OkrKeyResult(KeyResultId id, OkrId okrId, String title,
      BigDecimal targetValue, BigDecimal currentValue,
      KeyResultMetricUnit unit, BigDecimal weight) {
    if (title == null || title.isBlank())
      throw new IllegalArgumentException("title é obrigatório");
    if (targetValue == null || targetValue.compareTo(BigDecimal.ZERO) <= 0)
      throw new IllegalArgumentException("targetValue deve ser maior que zero");
    if (unit == null)
      throw new IllegalArgumentException("unit é obrigatório");

    this.id = id;
    this.okrId = okrId;
    this.title = title;
    this.targetValue = targetValue;
    this.currentValue = (currentValue != null) ? currentValue : BigDecimal.ZERO;
    this.unit = unit;
    this.weight = (weight != null) ? weight : BigDecimal.ONE;
  }

  public static OkrKeyResult create(OkrId okrId, String title,
      BigDecimal targetValue, KeyResultMetricUnit unit, BigDecimal weight) {
    return new OkrKeyResult(KeyResultId.gerarNovo(), okrId, title,
        targetValue, BigDecimal.ZERO, unit, weight);
  }

  public static OkrKeyResult reconstruct(KeyResultId id, OkrId okrId, String title,
      BigDecimal targetValue, BigDecimal currentValue,
      KeyResultMetricUnit unit, BigDecimal weight) {
    return new OkrKeyResult(id, okrId, title, targetValue, currentValue, unit, weight);
  }

  public BigDecimal getProgressPercentage() {
    if (targetValue.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
    return currentValue.divide(targetValue, 4, RoundingMode.HALF_UP)
        .multiply(BigDecimal.valueOf(100))
        .setScale(1, RoundingMode.HALF_UP);
  }
}