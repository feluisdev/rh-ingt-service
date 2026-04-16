package cv.igrp.RH_Service.sigdi.domain.admin.models;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class SiadapConfig {

  private final UUID id;
  private final Integer fiscalYear;
  private final BigDecimal goodScore;
  private final BigDecimal excellentScore;
  private final BigDecimal excellentQuota;
  private final Integer minCollaboratorsForQuota;
  private final BigDecimal resultsWeight;
  private final BigDecimal competenciesWeight;

  private SiadapConfig(UUID id, Integer fiscalYear, BigDecimal goodScore, BigDecimal excellentScore,
                       BigDecimal excellentQuota, Integer minCollaboratorsForQuota,
                       BigDecimal resultsWeight, BigDecimal competenciesWeight) {
    if (fiscalYear == null) throw new IllegalArgumentException("fiscalYear é obrigatório");
    this.id = id;
    this.fiscalYear = fiscalYear;
    this.goodScore = goodScore;
    this.excellentScore = excellentScore;
    this.excellentQuota = excellentQuota;
    this.minCollaboratorsForQuota = minCollaboratorsForQuota;
    this.resultsWeight = resultsWeight;
    this.competenciesWeight = competenciesWeight;
  }

  public static SiadapConfig create(Integer fiscalYear, BigDecimal goodScore, BigDecimal excellentScore,
                                    BigDecimal excellentQuota, Integer minCollaboratorsForQuota,
                                    BigDecimal resultsWeight, BigDecimal competenciesWeight) {
    return new SiadapConfig(UUID.randomUUID(), fiscalYear, goodScore, excellentScore,
        excellentQuota, minCollaboratorsForQuota, resultsWeight, competenciesWeight);
  }

  public static SiadapConfig reconstruct(UUID id, Integer fiscalYear, BigDecimal goodScore,
                                         BigDecimal excellentScore, BigDecimal excellentQuota,
                                         Integer minCollaboratorsForQuota, BigDecimal resultsWeight,
                                         BigDecimal competenciesWeight) {
    return new SiadapConfig(id, fiscalYear, goodScore, excellentScore, excellentQuota,
        minCollaboratorsForQuota, resultsWeight, competenciesWeight);
  }

  public SiadapConfig update(BigDecimal goodScore, BigDecimal excellentScore,
                              BigDecimal excellentQuota, Integer minCollaboratorsForQuota,
                              BigDecimal resultsWeight, BigDecimal competenciesWeight) {
    return new SiadapConfig(this.id, this.fiscalYear, goodScore, excellentScore,
        excellentQuota, minCollaboratorsForQuota, resultsWeight, competenciesWeight);
  }
}
