package cv.igrp.RH_Service.sigdi.domain.tatical.models;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.AcceptanceStatus;
import cv.igrp.RH_Service.sigdi.application.constants.KeyResultMetricUnit;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
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
  private final UUID organicUnitId;
  private final String title;
  private final String descriptionWhat;
  private final String justificationWhy;
  private final String locationWhere;
  private final UUID responsibleWho;
  private final String methodologyHow;
  private final DateRange dateRange;
  private final Budget budget;
  private final TacticalActivityStatus status;
  private final Integer version;
  private final List<KeyResult> keyResults;
  private final PaaLevel paaLevel;
  private final AcceptanceStatus acceptanceStatus;

  private TacticalActivity(TacticalActivityId id, UUID institutionId,
      StrategicGoalId strategicGoalId,
      UUID organicUnitId, String title, String descriptionWhat,
      String justificationWhy, String locationWhere, UUID responsibleWho,
      String methodologyHow, DateRange dateRange, Budget budget,
      TacticalActivityStatus status, Integer version,
      List<KeyResult> keyResults, PaaLevel paaLevel, AcceptanceStatus acceptanceStatus) {
    if (strategicGoalId == null)
      throw new IllegalArgumentException("strategicGoalId é obrigatório");
    if (organicUnitId == null)
      throw new IllegalArgumentException("organicUnitId é obrigatório");
    if (title == null || title.isBlank())
      throw new IllegalArgumentException("title é obrigatório");
    if (dateRange == null)
      throw new IllegalArgumentException("dateRange é obrigatório");

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
    this.paaLevel = (paaLevel != null) ? paaLevel : PaaLevel.UNIT_LEVEL;
    this.acceptanceStatus = acceptanceStatus;
  }

  public static TacticalActivity create(UUID institutionId, StrategicGoalId strategicGoalId,
      UUID organicUnitId, String title, String descriptionWhat, String justificationWhy,
      String locationWhere, UUID responsibleWho,
      String methodologyHow, DateRange dateRange, Budget budget, PaaLevel paaLevel) {

    if (PaaLevel.INDIVIDUAL_LEVEL.equals(paaLevel) && responsibleWho == null)
      throw IgrpResponseStatusException.badRequest(
          "responsibleWho é obrigatório para PAA de nível Individual");

    TacticalActivityStatus initialStatus = (budget != null) ? TacticalActivityStatus.DRAFT : TacticalActivityStatus.PENDING_BUDGET;
    AcceptanceStatus initialAcceptance = PaaLevel.INDIVIDUAL_LEVEL.equals(paaLevel)
        ? AcceptanceStatus.PENDING_ACCEPTANCE : null;

    return new TacticalActivity(TacticalActivityId.gerarNovo(), institutionId, strategicGoalId,
        organicUnitId, title, descriptionWhat, justificationWhy, locationWhere,
        responsibleWho, methodologyHow, dateRange, budget,
        initialStatus, 0, new ArrayList<>(), paaLevel, initialAcceptance);
  }

  public static TacticalActivity reconstruct(TacticalActivityId id, UUID institutionId,
      StrategicGoalId strategicGoalId,
      UUID organicUnitId, String title, String descriptionWhat,
      String justificationWhy, String locationWhere,
      UUID responsibleWho, String methodologyHow,
      DateRange dateRange, Budget budget,
      TacticalActivityStatus status, Integer version,
      List<KeyResult> keyResults, PaaLevel paaLevel, AcceptanceStatus acceptanceStatus) {
    return new TacticalActivity(id, institutionId, strategicGoalId, organicUnitId, title,
        descriptionWhat, justificationWhy, locationWhere, responsibleWho, methodologyHow,
        dateRange, budget, status, version, keyResults, paaLevel, acceptanceStatus);
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
        this.version + 1, this.keyResults, this.paaLevel, this.acceptanceStatus);
  }

  public TacticalActivity assignBudget(Budget budget) {
    if (budget == null)
      throw new IllegalArgumentException("Orçamento é obrigatório para esta ação");

    TacticalActivityStatus nextStatus = this.status == TacticalActivityStatus.PENDING_BUDGET ?
        TacticalActivityStatus.DRAFT : this.status;

    return new TacticalActivity(this.id, this.institutionId, this.strategicGoalId,
        this.organicUnitId, this.title,
        this.descriptionWhat, this.justificationWhy, this.locationWhere,
        this.responsibleWho, this.methodologyHow, this.dateRange,
        budget, nextStatus, this.version, this.keyResults, this.paaLevel, this.acceptanceStatus);
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
    KeyResult kr = KeyResult.create(this.institutionId, this.id, title, targetValue, metricUnit, null, null, null, null, null, null);
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
        this.budget, newStatus, this.version, this.keyResults, this.paaLevel, this.acceptanceStatus);
  }

  private TacticalActivity changeAcceptanceStatus(AcceptanceStatus newAcceptanceStatus) {
    if (!PaaLevel.INDIVIDUAL_LEVEL.equals(this.paaLevel))
      throw IgrpResponseStatusException.badRequest(
          "Apenas atividades PAA Individual têm fluxo de aceitação");
    return new TacticalActivity(this.id, this.institutionId, this.strategicGoalId,
        this.organicUnitId, this.title,
        this.descriptionWhat, this.justificationWhy, this.locationWhere,
        this.responsibleWho, this.methodologyHow, this.dateRange,
        this.budget, this.status, this.version, this.keyResults, this.paaLevel, newAcceptanceStatus);
  }

  public TacticalActivity accept() {
    if (!AcceptanceStatus.PENDING_ACCEPTANCE.equals(this.acceptanceStatus) &&
        !AcceptanceStatus.NEGOTIATING.equals(this.acceptanceStatus))
      throw IgrpResponseStatusException.badRequest(
          "Apenas atividades pendentes de aceitação ou em negociação podem ser aceites");
    return changeAcceptanceStatus(AcceptanceStatus.ACCEPTED);
  }

  public TacticalActivity negotiate() {
    if (!AcceptanceStatus.PENDING_ACCEPTANCE.equals(this.acceptanceStatus))
      throw IgrpResponseStatusException.badRequest(
          "Apenas atividades pendentes de aceitação podem iniciar negociação");
    return changeAcceptanceStatus(AcceptanceStatus.NEGOTIATING);
  }

  public TacticalActivity applyTacitAcceptance() {
    if (!AcceptanceStatus.PENDING_ACCEPTANCE.equals(this.acceptanceStatus))
      throw IgrpResponseStatusException.badRequest(
          "Aceitação tácita apenas aplicável a atividades pendentes de aceitação");
    return changeAcceptanceStatus(AcceptanceStatus.TACITLY_ACCEPTED);
  }

  public boolean isIndividualLevel() {
    return PaaLevel.INDIVIDUAL_LEVEL.equals(this.paaLevel);
  }

  public boolean isUnitLevel() {
    return PaaLevel.UNIT_LEVEL.equals(this.paaLevel);
  }

  public TacticalActivity updateKeyResult(KeyResult updated) {
    List<KeyResult> updatedList = keyResults.stream()
        .map(kr -> kr.getId().equals(updated.getId()) ? updated : kr)
        .toList();

    return new TacticalActivity(this.id, this.institutionId, this.strategicGoalId,
        this.organicUnitId, this.title,
        this.descriptionWhat, this.justificationWhy, this.locationWhere,
        this.responsibleWho, this.methodologyHow,
        this.dateRange, this.budget, this.status, this.version, updatedList, this.paaLevel, this.acceptanceStatus);
  }

  public TacticalActivity update(StrategicGoalId strategicGoalId, UUID organicUnitId, String title, 
      String descriptionWhat, String justificationWhy, String locationWhere, UUID responsibleWho, 
      String methodologyHow, DateRange dateRange, Budget budget) {
    
    // Always reverts to DRAFT after update as per requirements
    TacticalActivityStatus nextStatus = (budget != null) ? TacticalActivityStatus.DRAFT : TacticalActivityStatus.PENDING_BUDGET;

    return new TacticalActivity(this.id, this.institutionId, strategicGoalId,
        organicUnitId, title, descriptionWhat, justificationWhy, locationWhere,
        responsibleWho, methodologyHow, dateRange, budget,
        nextStatus, this.version, this.keyResults, this.paaLevel, this.acceptanceStatus);
  }
}
