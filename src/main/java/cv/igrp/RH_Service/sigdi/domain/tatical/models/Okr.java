package cv.igrp.RH_Service.sigdi.domain.tatical.models;

import cv.igrp.RH_Service.sigdi.application.constants.KeyResultMetricUnit;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.InstitutionalIdentityId;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.OkrId;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class Okr {

  public record KeyResultData(String title, BigDecimal targetValue,
      KeyResultMetricUnit unit, BigDecimal weight) {}

  private final OkrId id;
  private final InstitutionalIdentityId institutionId;
  private final StrategicGoalId strategicGoalId;
  private final String title;
  private final String cycle;
  private final String status;
  private final List<OkrKeyResult> keyResults;

  private Okr(OkrId id, InstitutionalIdentityId institutionId,
      StrategicGoalId strategicGoalId, String title, String cycle,
      String status, List<OkrKeyResult> keyResults) {
    if (title == null || title.isBlank())
      throw new IllegalArgumentException("title é obrigatório");
    if (cycle == null || cycle.isBlank())
      throw new IllegalArgumentException("cycle é obrigatório");
    if (strategicGoalId == null)
      throw new IllegalArgumentException("strategicGoalId é obrigatório");

    this.id = id;
    this.institutionId = institutionId;
    this.strategicGoalId = strategicGoalId;
    this.title = title;
    this.cycle = cycle;
    this.status = (status != null) ? status : "ACTIVE";
    this.keyResults = (keyResults != null)
        ? Collections.unmodifiableList(keyResults)
        : Collections.emptyList();
  }

  public static Okr create(InstitutionalIdentityId institutionId,
      StrategicGoalId strategicGoalId, String title, String cycle,
      List<KeyResultData> keyResultsData) {
    OkrId okrId = OkrId.gerarNovo();
    List<OkrKeyResult> krs = new ArrayList<>();
    if (keyResultsData != null) {
      for (KeyResultData d : keyResultsData) {
        krs.add(OkrKeyResult.create(okrId, d.title(), d.targetValue(), d.unit(), d.weight()));
      }
    }
    return new Okr(okrId, institutionId, strategicGoalId, title, cycle, "ACTIVE", krs);
  }

  public static Okr reconstruct(OkrId id, InstitutionalIdentityId institutionId,
      StrategicGoalId strategicGoalId, String title, String cycle,
      String status, List<OkrKeyResult> keyResults) {
    return new Okr(id, institutionId, strategicGoalId, title, cycle, status, keyResults);
  }

  public BigDecimal getProgressPercentage() {
    if (keyResults.isEmpty()) return BigDecimal.ZERO;
    BigDecimal totalWeight = keyResults.stream()
        .map(OkrKeyResult::getWeight)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    if (totalWeight.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
    BigDecimal weightedSum = keyResults.stream()
        .map(kr -> kr.getProgressPercentage().multiply(kr.getWeight()))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    return weightedSum.divide(totalWeight, 1, RoundingMode.HALF_UP);
  }
}