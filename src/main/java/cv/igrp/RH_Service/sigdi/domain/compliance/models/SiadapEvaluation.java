package cv.igrp.RH_Service.sigdi.domain.compliance.models;

import cv.igrp.RH_Service.sigdi.application.constants.SiadapMeritRating;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.SiadapEvaluationId;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
public class SiadapEvaluation {

  private final SiadapEvaluationId id;
  private final String employeeId;
  private final Integer year;
  private final BigDecimal objectivesScore;
  private final BigDecimal competenciesScore;
  private final BigDecimal finalScore;
  private final SiadapMeritRating meritRating;
  private final boolean validatedQuota;

  private SiadapEvaluation(SiadapEvaluationId id, String employeeId, Integer year,
                           BigDecimal objectivesScore, BigDecimal competenciesScore,
                           BigDecimal finalScore, SiadapMeritRating meritRating,
                           boolean validatedQuota) {
    if (id == null) throw new IllegalArgumentException("id é obrigatório");
    if (employeeId == null || employeeId.isBlank()) throw new IllegalArgumentException("employeeId é obrigatório");
    if (year == null) throw new IllegalArgumentException("year é obrigatório");
    this.id = id;
    this.employeeId = employeeId;
    this.year = year;
    this.objectivesScore = objectivesScore;
    this.competenciesScore = competenciesScore;
    this.finalScore = finalScore;
    this.meritRating = meritRating;
    this.validatedQuota = validatedQuota;
  }

  public static SiadapEvaluation create(String employeeId, Integer year,
                                        BigDecimal objectivesScore, BigDecimal competenciesScore) {
    BigDecimal finalScore = calculateFinalScore(objectivesScore, competenciesScore);
    return new SiadapEvaluation(SiadapEvaluationId.gerarNovo(), employeeId, year,
        objectivesScore, competenciesScore, finalScore, null, false);
  }

  public static SiadapEvaluation reconstruct(SiadapEvaluationId id, String employeeId, Integer year,
                                             BigDecimal objectivesScore, BigDecimal competenciesScore,
                                             BigDecimal finalScore, SiadapMeritRating meritRating,
                                             boolean validatedQuota) {
    return new SiadapEvaluation(id, employeeId, year, objectivesScore, competenciesScore,
        finalScore, meritRating, validatedQuota);
  }

  public SiadapEvaluation assignMeritRating(SiadapMeritRating rating) {
    return new SiadapEvaluation(this.id, this.employeeId, this.year,
        this.objectivesScore, this.competenciesScore, this.finalScore,
        rating, this.validatedQuota);
  }

  public SiadapEvaluation markQuotaValidated() {
    return new SiadapEvaluation(this.id, this.employeeId, this.year,
        this.objectivesScore, this.competenciesScore, this.finalScore,
        this.meritRating, true);
  }

  private static BigDecimal calculateFinalScore(BigDecimal objectivesScore, BigDecimal competenciesScore) {
    if (objectivesScore == null && competenciesScore == null) return null;
    BigDecimal o = (objectivesScore != null) ? objectivesScore : BigDecimal.ZERO;
    BigDecimal c = (competenciesScore != null) ? competenciesScore : BigDecimal.ZERO;
    return o.add(c).divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
  }
}

