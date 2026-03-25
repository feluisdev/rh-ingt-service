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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class TacticalActivity {

  private final TacticalActivityId id;
  private final StrategicGoalId strategicGoalId;  // referência por ID (domínio strategy)
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

  private TacticalActivity(TacticalActivityId id, StrategicGoalId strategicGoalId,
                           String organicUnitId, String title, String descriptionWhat,
                           String justificationWhy, String locationWhere, String responsibleWho,
                           String methodologyHow, DateRange dateRange, Budget budget,
                           TacticalActivityStatus status, Integer version,
                           List<KeyResult> keyResults) {
    if (strategicGoalId == null) {
      throw new IllegalArgumentException("strategicGoalId é obrigatório");
    }
    if (organicUnitId == null || organicUnitId.isBlank()) {
      throw new IllegalArgumentException("organicUnitId é obrigatório");
    }
    if (title == null || title.isBlank()) {
      throw new IllegalArgumentException("title é obrigatório");
    }
    if (dateRange == null) {
      throw new IllegalArgumentException("dateRange é obrigatório");
    }
    if (budget == null) {
      throw new IllegalArgumentException("budget é obrigatório");
    }
    this.id = id;
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

  public static TacticalActivity create(StrategicGoalId strategicGoalId, String organicUnitId,
                                        String title, String descriptionWhat, String justificationWhy,
                                        String locationWhere, String responsibleWho,
                                        String methodologyHow, DateRange dateRange, Budget budget) {
    return new TacticalActivity(
        TacticalActivityId.gerarNovo(),
        strategicGoalId,
        organicUnitId,
        title,
        descriptionWhat,
        justificationWhy,
        locationWhere,
        responsibleWho,
        methodologyHow,
        dateRange,
        budget,
        TacticalActivityStatus.DRAFT,
        0,
        new ArrayList<>()
    );
  }

  public static TacticalActivity reconstruct(TacticalActivityId id, StrategicGoalId strategicGoalId,
                                             String organicUnitId, String title, String descriptionWhat,
                                             String justificationWhy, String locationWhere,
                                             String responsibleWho, String methodologyHow,
                                             DateRange dateRange, Budget budget,
                                             TacticalActivityStatus status, Integer version,
                                             List<KeyResult> keyResults) {
    return new TacticalActivity(id, strategicGoalId, organicUnitId, title, descriptionWhat,
        justificationWhy, locationWhere, responsibleWho, methodologyHow,
        dateRange, budget, status, version, keyResults);
  }

  public List<KeyResult> getKeyResults() {
    return Collections.unmodifiableList(keyResults);
  }

  // ── Regras de negócio ─────────────────────────────────────────────

  public KeyResult addKeyResult(String title, BigDecimal targetValue, KeyResultMetricUnit metricUnit) {
    KeyResult kr = KeyResult.create(this.id, title, targetValue, metricUnit);
    keyResults.add(kr);
    return kr;
  }

  public TacticalActivity approve() {
    if (!TacticalActivityStatus.PENDING.equals(this.status)) {
      throw IgrpResponseStatusException.badRequest(
          "Apenas atividades com status PENDING podem ser aprovadas");
    }
    return changeStatus(TacticalActivityStatus.APPROVED);
  }

  public TacticalActivity reject() {
    if (!TacticalActivityStatus.PENDING.equals(this.status)) {
      throw IgrpResponseStatusException.badRequest(
          "Apenas atividades com status PENDING podem ser rejeitadas");
    }
    return changeStatus(TacticalActivityStatus.REJECTED);
  }

  public TacticalActivity submit() {
    if (!TacticalActivityStatus.DRAFT.equals(this.status)) {
      throw IgrpResponseStatusException.badRequest(
          "Apenas atividades com status DRAFT podem ser submetidas");
    }
    return changeStatus(TacticalActivityStatus.PENDING);
  }

  public TacticalActivity cancel() {
    if (TacticalActivityStatus.CANCELLED.equals(this.status) ||
        TacticalActivityStatus.APPROVED.equals(this.status)) {
      throw IgrpResponseStatusException.badRequest(
          "Atividade não pode ser cancelada no status atual: " + this.status.getCode());
    }
    return changeStatus(TacticalActivityStatus.CANCELLED);
  }

  public boolean isApproved() {
    return TacticalActivityStatus.APPROVED.equals(this.status);
  }

  public boolean isDraft() {
    return TacticalActivityStatus.DRAFT.equals(this.status);
  }

  private TacticalActivity changeStatus(TacticalActivityStatus newStatus) {
    return new TacticalActivity(this.id, this.strategicGoalId, this.organicUnitId, this.title,
        this.descriptionWhat, this.justificationWhy, this.locationWhere,
        this.responsibleWho, this.methodologyHow, this.dateRange,
        this.budget, newStatus, this.version, this.keyResults);
  }
}
