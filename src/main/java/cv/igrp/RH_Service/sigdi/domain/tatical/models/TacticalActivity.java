package cv.igrp.RH_Service.sigdi.domain.tatical.models;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.KeyResultMetricUnit;
import cv.igrp.RH_Service.sigdi.application.constants.TacticalActivityStatus;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.Budget;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.DateRange;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.TacticalActivityId;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Getter
public class TacticalActivity {

  private final TacticalActivityId id;
  private final UUID institutionId;
  private final StrategicGoalId strategicGoalId;
  private final String organicUnitId;
  private final String title;
  private final String descriptionWhat;
  private final String justificationWhy;
  private final String locationWhere;
  private final String responsibleWho;
  private final String methodologyHow;
  private final DateRange dateRange;
  private final Budget budget;
  private final TacticalActivityStatus status;
  private final Integer version;
  private final List<KeyResult> keyResults;

  private TacticalActivity(TacticalActivityId id, UUID institutionId,
      StrategicGoalId strategicGoalId,
      String organicUnitId, String title, String descriptionWhat,
      String justificationWhy, String locationWhere, String responsibleWho,
      String methodologyHow, DateRange dateRange, Budget budget,
      TacticalActivityStatus status, Integer version,
      List<KeyResult> keyResults) {
    if (strategicGoalId == null)
      throw new IllegalArgumentException("strategicGoalId é obrigatório");
    if (organicUnitId == null || organicUnitId.isBlank())
      throw new IllegalArgumentException("organicUnitId é obrigatório");
    if (title == null || title.isBlank())
      throw new IllegalArgumentException("title é obrigatório");
    if (dateRange == null)
      throw new IllegalArgumentException("dateRange é obrigatório");
    if (budget == null)
      throw new IllegalArgumentException("budget é obrigatório");

    this.id = id;
    this.institutionId = institutionId;
    this.strategicGoalId = strategicGoalId;
    this.organicUnitId = organicUnitId;
    this.title = title;
    this.descriptionWhat = descriptionWhat;
    this.justificationWhy = justificationWhy;
    this.locationWhere = locationWhere;
    this.responsibleWho = responsibleWho;
    this.methodologyHow = methodologyHow;
    this.dateRange = dateRange;
    this.budget = budget;
    this.status = (status != null) ? status : TacticalActivityStatus.DRAFT;
    this.version = (version != null) ? version : 0;
    this.keyResults = (keyResults != null) ? new ArrayList<>(keyResults) : new ArrayList<>();
  }

  public static TacticalActivity create(UUID institutionId, StrategicGoalId strategicGoalId,
      String organicUnitId, String title, String descriptionWhat, String justificationWhy,
      String locationWhere, String responsibleWho,
      String methodologyHow, DateRange dateRange, Budget budget) {
    return new TacticalActivity(TacticalActivityId.gerarNovo(), institutionId, strategicGoalId,
        organicUnitId, title, descriptionWhat, justificationWhy, locationWhere,
        responsibleWho, methodologyHow, dateRange, budget,
        TacticalActivityStatus.DRAFT, 0, new ArrayList<>());
  }

  public static TacticalActivity reconstruct(TacticalActivityId id, UUID institutionId,
      StrategicGoalId strategicGoalId,
      String organicUnitId, String title, String descriptionWhat,
      String justificationWhy, String locationWhere,
      String responsibleWho, String methodologyHow,
      DateRange dateRange, Budget budget,
      TacticalActivityStatus status, Integer version,
      List<KeyResult> keyResults) {
    return new TacticalActivity(id, institutionId, strategicGoalId, organicUnitId, title,
        descriptionWhat, justificationWhy, locationWhere, responsibleWho, methodologyHow,
        dateRange, budget, status, version, keyResults);
  }

  public List<KeyResult> getKeyResults() {
    return Collections.unmodifiableList(keyResults);
  }

  public TacticalActivity submit() {
    if (!TacticalActivityStatus.DRAFT.equals(this.status))
      throw IgrpResponseStatusException.badRequest("Apenas atividades DRAFT podem ser submetidas");
    return changeStatus(TacticalActivityStatus.PENDING_TACTICAL);
  }

  public TacticalActivity approve() {
    if (TacticalActivityStatus.PENDING_TACTICAL.equals(this.status))
      return changeStatus(TacticalActivityStatus.PENDING_STRATEGIC);
    if (TacticalActivityStatus.PENDING_STRATEGIC.equals(this.status))
      return changeStatus(TacticalActivityStatus.APPROVED);
    throw IgrpResponseStatusException.badRequest(
        "Atividade não está em estado pendente para aprovação. Status atual: " + this.status.getCode());
  }

  public TacticalActivity reject() {
    if (!TacticalActivityStatus.PENDING_TACTICAL.equals(this.status) &&
        !TacticalActivityStatus.PENDING_STRATEGIC.equals(this.status))
      throw IgrpResponseStatusException.badRequest(
          "Apenas atividades pendentes podem ser rejeitadas. Status atual: " + this.status.getCode());
    return changeStatus(TacticalActivityStatus.DRAFT);
  }

  public TacticalActivity cancel() {
    if (TacticalActivityStatus.CANCELLED.equals(this.status) ||
        TacticalActivityStatus.APPROVED.equals(this.status))
      throw IgrpResponseStatusException.badRequest(
          "Atividade não pode ser cancelada no status: " + this.status.getCode());
    return changeStatus(TacticalActivityStatus.CANCELLED);
  }

  public TacticalActivity requestChange(Budget newBudget, DateRange newDateRange,
      String changeJustification) {
    if (!TacticalActivityStatus.APPROVED.equals(this.status))
      throw IgrpResponseStatusException.badRequest(
          "Change Request só é permitido em atividades APPROVED");
    if (changeJustification == null || changeJustification.isBlank())
      throw new IllegalArgumentException("Justificativa é obrigatória para Change Request");

    return new TacticalActivity(this.id, this.institutionId, this.strategicGoalId,
        this.organicUnitId, this.title,
        this.descriptionWhat, this.justificationWhy, this.locationWhere,
        this.responsibleWho, this.methodologyHow, newDateRange,
        newBudget, TacticalActivityStatus.PENDING_TACTICAL,
        this.version + 1, this.keyResults);
  }

  public BigDecimal getWeightedProgress() {
    if (keyResults.isEmpty())
      return BigDecimal.ZERO;

    BigDecimal sum = keyResults.stream()
        .map(KeyResult::getProgressPercentage)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    return sum.divide(BigDecimal.valueOf(keyResults.size()), 2, RoundingMode.HALF_UP);
  }

  public KeyResult addKeyResult(String title, BigDecimal targetValue,
      KeyResultMetricUnit metricUnit) {
    KeyResult kr = KeyResult.create(this.institutionId, this.id, title, targetValue, metricUnit);
    keyResults.add(kr);
    return kr;
  }

  public boolean isApproved() {
    return TacticalActivityStatus.APPROVED.equals(this.status);
  }

  public boolean isDraft() {
    return TacticalActivityStatus.DRAFT.equals(this.status);
  }

  private TacticalActivity changeStatus(TacticalActivityStatus newStatus) {
    return new TacticalActivity(this.id, this.institutionId, this.strategicGoalId,
        this.organicUnitId, this.title,
        this.descriptionWhat, this.justificationWhy, this.locationWhere,
        this.responsibleWho, this.methodologyHow, this.dateRange,
        this.budget, newStatus, this.version, this.keyResults);
  }

  public TacticalActivity updateKeyResult(KeyResult updated) {
    List<KeyResult> updatedList = keyResults.stream()
        .map(kr -> kr.getId().equals(updated.getId()) ? updated : kr)
        .toList();

    return new TacticalActivity(this.id, this.institutionId, this.strategicGoalId,
        this.organicUnitId, this.title,
        this.descriptionWhat, this.justificationWhy, this.locationWhere,
        this.responsibleWho, this.methodologyHow,
        this.dateRange, this.budget, this.status, this.version, updatedList);
  }
}
