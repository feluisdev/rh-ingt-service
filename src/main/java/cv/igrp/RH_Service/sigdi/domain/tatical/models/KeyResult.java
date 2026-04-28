package cv.igrp.RH_Service.sigdi.domain.tatical.models;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.KeyResultMetricUnit;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.KeyResultId;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.TacticalActivityId;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Getter
public class KeyResult {

  private static final BigDecimal RISK_THRESHOLD = new BigDecimal("0.30");

  private final KeyResultId id;
  private final UUID institutionId;
  private final TacticalActivityId activityId;
  private final String title;
  private final BigDecimal targetValue;
  private final BigDecimal currentValue;
  private final KeyResultMetricUnit metricUnit;
  private final List<KeyResultCheckin> checkins;
  private final String criteriaSuperado;
  private final String criteriaSeguranca;
  private final String criteriaAlcancado;
  private final String criteriaInsuficiente;

  private KeyResult(KeyResultId id, UUID institutionId, TacticalActivityId activityId, String title,
      BigDecimal targetValue, BigDecimal currentValue,
      KeyResultMetricUnit metricUnit,
      List<KeyResultCheckin> checkins,
      String criteriaSuperado, String criteriaSeguranca,
      String criteriaAlcancado, String criteriaInsuficiente) {
    if (title == null || title.isBlank())
      throw new IllegalArgumentException("title é obrigatório");
    if (targetValue == null || targetValue.compareTo(BigDecimal.ZERO) <= 0)
      throw new IllegalArgumentException("targetValue deve ser maior que zero");
    // activityId is null for OKR-bound key results (belong to an OKR, not a TacticalActivity)

    this.id = id;
    this.institutionId = institutionId;
    this.activityId = activityId;
    this.title = title;
    this.targetValue = targetValue;
    this.currentValue = (currentValue != null) ? currentValue : BigDecimal.ZERO;
    this.metricUnit = metricUnit;
    this.checkins = (checkins != null) ? checkins : new ArrayList<>();
    this.criteriaSuperado = criteriaSuperado;
    this.criteriaSeguranca = criteriaSeguranca;
    this.criteriaAlcancado = criteriaAlcancado;
    this.criteriaInsuficiente = criteriaInsuficiente;
  }

  public static KeyResult create(UUID institutionId, TacticalActivityId activityId, String title,
      BigDecimal targetValue, KeyResultMetricUnit metricUnit,
      String criteriaSuperado, String criteriaSeguranca,
      String criteriaAlcancado, String criteriaInsuficiente) {
    return new KeyResult(KeyResultId.gerarNovo(), institutionId, activityId, title, targetValue,
        BigDecimal.ZERO, metricUnit, new ArrayList<>(), 
        criteriaSuperado, criteriaSeguranca, criteriaAlcancado, criteriaInsuficiente);
  }
 
  public static KeyResult reconstruct(KeyResultId id, UUID institutionId,
      TacticalActivityId activityId, String title,
      BigDecimal targetValue, BigDecimal currentValue,
      KeyResultMetricUnit metricUnit,
      List<KeyResultCheckin> checkins,
      String criteriaSuperado, String criteriaSeguranca,
      String criteriaAlcancado, String criteriaInsuficiente) {
    return new KeyResult(id, institutionId, activityId, title, targetValue, currentValue,
        metricUnit, checkins, criteriaSuperado, criteriaSeguranca, criteriaAlcancado, criteriaInsuficiente);
  }

  public List<KeyResultCheckin> getCheckins() {
    return Collections.unmodifiableList(checkins);
  }

  public BigDecimal getProgressPercentage() {
    if (targetValue.compareTo(BigDecimal.ZERO) == 0)
      return BigDecimal.ZERO;
    return currentValue.divide(targetValue, 4, RoundingMode.HALF_UP)
        .multiply(BigDecimal.valueOf(100));
  }

  public boolean isCompleted() {
    return currentValue.compareTo(targetValue) >= 0;
  }

  public boolean isCriticalRisk(LocalDate periodEndDate) {
    LocalDate oneMonthBefore = periodEndDate.minusMonths(1);
    boolean isLastMonth = !LocalDate.now().isBefore(oneMonthBefore);
    boolean isBelowThreshold = getProgressPercentage()
        .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
        .compareTo(RISK_THRESHOLD) < 0;
    return isLastMonth && isBelowThreshold;
  }

  public KeyResult applyCheckin(BigDecimal valueAdded, String evidenceUrl, String comment) {
    BigDecimal newValue = this.currentValue.add(valueAdded);

    if (newValue.compareTo(this.targetValue) > 0) {
      throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, "Target exceeded");
    }

    KeyResultCheckin checkin = KeyResultCheckin.create(this.id, valueAdded, evidenceUrl, comment);

    List<KeyResultCheckin> newCheckins = new ArrayList<>(this.checkins);
    newCheckins.add(checkin);

    return new KeyResult(this.id, this.institutionId, this.activityId, this.title,
        this.targetValue, newValue, this.metricUnit, newCheckins,
        this.criteriaSuperado, this.criteriaSeguranca, this.criteriaAlcancado, this.criteriaInsuficiente);
  }

  public KeyResult updateDetails(String title, BigDecimal targetValue, KeyResultMetricUnit metricUnit) {
    if (title == null || title.isBlank())
      throw new IllegalArgumentException("title é obrigatório");
    if (targetValue == null || targetValue.compareTo(BigDecimal.ZERO) <= 0)
      throw new IllegalArgumentException("targetValue deve ser maior que zero");
    if (this.currentValue.compareTo(targetValue) > 0) {
      throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
          "Target cannot be lower than current value");
    }
    return new KeyResult(this.id, this.institutionId, this.activityId, title, targetValue,
        this.currentValue, metricUnit, this.checkins,
        this.criteriaSuperado, this.criteriaSeguranca, this.criteriaAlcancado, this.criteriaInsuficiente);
  }
}
