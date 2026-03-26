package cv.igrp.RH_Service.sigdi.domain.compliance.service;

import cv.igrp.RH_Service.sigdi.application.constants.SiadapMeritRating;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapEvaluation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class SiadapQuotaPolicy {

  private SiadapQuotaPolicy() {
  }

  public static boolean isWithinExcellentQuota(List<SiadapEvaluation> evaluations, BigDecimal maxPercentExcellent) {
    if (evaluations == null || evaluations.isEmpty()) return true;
    if (maxPercentExcellent == null) throw new IllegalArgumentException("maxPercentExcellent é obrigatório");
    if (maxPercentExcellent.compareTo(BigDecimal.ZERO) < 0 || maxPercentExcellent.compareTo(new BigDecimal("100")) > 0) {
      throw new IllegalArgumentException("maxPercentExcellent deve ser >= 0 e <= 100");
    }

    long total = evaluations.size();
    long excellent = evaluations.stream()
        .filter(e -> SiadapMeritRating.EXCELLENT.equals(e.getMeritRating()))
        .count();

    long allowed = BigDecimal.valueOf(total)
        .multiply(maxPercentExcellent)
        .divide(new BigDecimal("100"), 0, RoundingMode.FLOOR)
        .longValue();

    return excellent <= allowed;
  }
}

