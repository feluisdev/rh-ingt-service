package cv.igrp.RH_Service.sigdi.infrastructure.mappers.compliance;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.SiadapEvaluationEntity;
import cv.igrp.RH_Service.sigdi.application.constants.SiadapMeritRating;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapEvaluation;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.SiadapEvaluationId;
import org.springframework.stereotype.Component;

@Component
public class SiadapEvaluationMapper {

  public SiadapEvaluation toDomain(SiadapEvaluationEntity entity) {
    if (entity == null) return null;

    Integer year = parseYearOrThrow(entity.getYear());

    return SiadapEvaluation.reconstruct(
        SiadapEvaluationId.from(entity.getId()),
        entity.getEmployeeId(),
        year,
        entity.getObjectivesScore(),
        entity.getCompetenciesScore(),
        entity.getFinalScore(),
        entity.getMeritRating() != null ? SiadapMeritRating.fromCodeOrThrow(entity.getMeritRating()) : null,
        entity.isValidatedQuota()
    );
  }

  public SiadapEvaluationEntity toEntity(SiadapEvaluation domain) {
    if (domain == null) return null;

    SiadapEvaluationEntity entity = new SiadapEvaluationEntity();
    entity.setId(domain.getId().getValor().getValor());
    entity.setEmployeeId(domain.getEmployeeId());
    entity.setYear(domain.getYear().toString());
    entity.setObjectivesScore(domain.getObjectivesScore());
    entity.setCompetenciesScore(domain.getCompetenciesScore());
    entity.setFinalScore(domain.getFinalScore());
    entity.setMeritRating(domain.getMeritRating() != null ? domain.getMeritRating().getCode() : null);
    entity.setValidatedQuota(domain.isValidatedQuota());
    return entity;
  }

  private Integer parseYearOrThrow(String yearRaw) {
    if (yearRaw == null || yearRaw.isBlank()) throw new IllegalArgumentException("year é obrigatório");
    try {
      return Integer.valueOf(yearRaw);
    } catch (Exception e) {
      throw new IllegalArgumentException("year inválido: " + yearRaw, e);
    }
  }
}

