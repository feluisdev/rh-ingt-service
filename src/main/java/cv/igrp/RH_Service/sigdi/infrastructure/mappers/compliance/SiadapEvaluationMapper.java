package cv.igrp.RH_Service.sigdi.infrastructure.mappers.compliance;

import cv.igrp.RH_Service.sigdi.application.constants.AcceptanceStatus;
import cv.igrp.RH_Service.sigdi.application.constants.CompetencyCategory;
import cv.igrp.RH_Service.sigdi.application.constants.EvaluationPhase;
import cv.igrp.RH_Service.sigdi.application.constants.SiadapMeritRating;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapEvaluation;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.CompetencyItem;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.IndividualObjective;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.SiadapEvaluationId;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.CompetencyItemEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.IndividualObjectiveEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.SiadapEvaluationEntity;
import org.springframework.stereotype.Component;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapEvaluationDTO;
import cv.igrp.RH_Service.sigdi.application.dto.IndividualObjectiveDTO;
import cv.igrp.RH_Service.sigdi.application.dto.CompetencyItemDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class SiadapEvaluationMapper {

  public SiadapEvaluation toDomain(SiadapEvaluationEntity entity,
                                   List<IndividualObjectiveEntity> objectiveEntities,
                                   List<CompetencyItemEntity> competencyEntities) {
    if (entity == null) return null;

    Integer year = parseYearOrThrow(entity.getYear());

    List<IndividualObjective> objectives = objectiveEntities != null ? objectiveEntities.stream()
        .map(this::toDomainObjective)
        .collect(Collectors.toList()) : new ArrayList<>();

    List<CompetencyItem> competencies = competencyEntities != null ? competencyEntities.stream()
        .map(this::toDomainCompetency)
        .collect(Collectors.toList()) : new ArrayList<>();

    EvaluationPhase phase = entity.getEvaluationPhase() != null
        ? EvaluationPhase.fromCodeOrThrow(entity.getEvaluationPhase())
        : EvaluationPhase.OPEN;

    AcceptanceStatus acceptanceStatus = entity.getAcceptanceStatus() != null
        ? AcceptanceStatus.fromCodeOrThrow(entity.getAcceptanceStatus())
        : null;

    return SiadapEvaluation.reconstruct(
        SiadapEvaluationId.from(entity.getId()),
        entity.getEmployeeId(),
        year,
        entity.getOrganicUnitId(),
        entity.getEvaluatorId(),
        objectives,
        competencies,
        entity.getResultsWeight(),
        entity.getCompetenciesWeight(),
        entity.getSelfEvaluationScore(),
        entity.getFinalScore(),
        entity.getMeritRating() != null ? SiadapMeritRating.fromCodeOrThrow(entity.getMeritRating()) : null,
        entity.isValidatedQuota(),
        phase,
        acceptanceStatus,
        entity.getLastNegotiationComment(),
        entity.isSelfEvaluationTacitlyAccepted()
    );
  }

  // Deprecated fallback or simple delegator if needed
  public SiadapEvaluation toDomain(SiadapEvaluationEntity entity) {
    return toDomain(entity, new ArrayList<>(), new ArrayList<>());
  }

  public SiadapEvaluationEntity toEntity(SiadapEvaluation domain) {
    if (domain == null) return null;

    SiadapEvaluationEntity entity = new SiadapEvaluationEntity();
    entity.setId(domain.getId().getValor().getValor());
    entity.setEmployeeId(domain.getEmployeeId());
    entity.setYear(domain.getYear().toString());
    entity.setOrganicUnitId(domain.getOrganicUnitId());
    entity.setEvaluatorId(domain.getEvaluatorId());
    entity.setEvaluationPhase(domain.getPhase() != null ? domain.getPhase().getCode() : null);
    entity.setAcceptanceStatus(domain.getAcceptanceStatus() != null ? domain.getAcceptanceStatus().getCode() : null);
    entity.setLastNegotiationComment(domain.getLastNegotiationComment());
    entity.setSelfEvaluationTacitlyAccepted(domain.isSelfEvaluationTacitlyAccepted());
    entity.setSelfEvaluationScore(domain.getSelfEvaluationScore());
    entity.setFinalScore(domain.getFinalScore());
    entity.setResultsWeight(domain.getResultsWeight());
    entity.setCompetenciesWeight(domain.getCompetenciesWeight());
    entity.setMeritRating(domain.getMeritRating() != null ? domain.getMeritRating().getCode() : null);
    entity.setValidatedQuota(domain.isValidatedQuota());

    // Legacy scores computed on demand/for backwards compatibility if requested
    entity.setObjectivesScore(domain.calculateResultsScore());
    entity.setCompetenciesScore(domain.calculateCompetenciesScore());

    return entity;
  }

  public SiadapEvaluationDTO toDto(SiadapEvaluationEntity e) {
    if (e == null) return null;
    SiadapEvaluationDTO dto = new SiadapEvaluationDTO();
    dto.setId(e.getId().toString());
    dto.setEmployeeId(e.getEmployeeId());
    dto.setYear(e.getYear());
    dto.setObjectivesScore(e.getObjectivesScore());
    dto.setCompetenciesScore(e.getCompetenciesScore());
    dto.setFinalScore(e.getFinalScore());
    dto.setMeritRating(e.getMeritRating());
    dto.setQuotaValidated(e.isValidatedQuota());
    dto.setStatus(e.isValidatedQuota() ? "CLOSED" : "DRAFT");
    dto.setLastUpdatedAt(e.getLastModifiedDate() != null ? e.getLastModifiedDate().toString() : null);

    dto.setOrganicUnitId(e.getOrganicUnitId());
    dto.setEvaluatorId(e.getEvaluatorId());
    dto.setPhase(e.getEvaluationPhase());
    dto.setAcceptanceStatus(e.getAcceptanceStatus());
    dto.setAcceptanceStatusDesc(AcceptanceStatus.fromCode(e.getAcceptanceStatus())
        .map(AcceptanceStatus::getDescription).orElse(null));
    dto.setLastNegotiationComment(e.getLastNegotiationComment());
    dto.setSelfEvaluationTacitlyAccepted(e.isSelfEvaluationTacitlyAccepted());
    dto.setSelfEvaluationScore(e.getSelfEvaluationScore());
    dto.setResultsWeight(e.getResultsWeight());
    dto.setCompetenciesWeight(e.getCompetenciesWeight());
    return dto;
  }

  /**
   * WR-03: assembles the full DTO — scalar fields plus {@code objectives}/{@code competencies} —
   * from the domain aggregate directly, mirroring the manual population previously duplicated
   * only in {@code GetEvaluationDetailQueryHandler}. Command handlers should call this instead
   * of the incomplete {@code toDto(toEntity(domain))} round-trip so mutation responses are
   * usable for rendering the objectives/competencies list without a follow-up GET.
   */
  public SiadapEvaluationDTO toFullDto(SiadapEvaluation domain) {
    SiadapEvaluationDTO dto = toDto(toEntity(domain));

    List<IndividualObjectiveDTO> objectives = domain.getObjectives().stream()
        .map(obj -> new IndividualObjectiveDTO(
            obj.getCode(),
            obj.getDescription(),
            obj.getIndicator(),
            obj.getTargetValue(),
            obj.getAchievedValue(),
            obj.getScore(),
            obj.getWeight()
        ))
        .collect(Collectors.toList());
    dto.setObjectives(objectives);

    List<CompetencyItemDTO> competencies = domain.getCompetencies().stream()
        .map(comp -> new CompetencyItemDTO(
            comp.getCompetencyCode(),
            comp.getCompetencyName(),
            comp.getCategory().getCode(),
            comp.getScore()
        ))
        .collect(Collectors.toList());
    dto.setCompetencies(competencies);

    return dto;
  }

  public List<IndividualObjectiveEntity> toObjectiveEntities(SiadapEvaluation domain) {
    if (domain == null || domain.getObjectives() == null) return new ArrayList<>();
    UUID evalId = domain.getId().getValor().getValor();
    return domain.getObjectives().stream().map(obj -> {
      IndividualObjectiveEntity entity = new IndividualObjectiveEntity();
      entity.setId(UUID.randomUUID());
      entity.setEvaluationId(evalId);
      entity.setObjectiveCode(obj.getCode());
      entity.setDescription(obj.getDescription());
      entity.setIndicator(obj.getIndicator());
      entity.setTargetValue(obj.getTargetValue());
      entity.setAchievedValue(obj.getAchievedValue());
      entity.setScore(obj.getScore());
      entity.setWeight(obj.getWeight());
      return entity;
    }).collect(Collectors.toList());
  }

  public List<CompetencyItemEntity> toCompetencyEntities(SiadapEvaluation domain) {
    if (domain == null || domain.getCompetencies() == null) return new ArrayList<>();
    UUID evalId = domain.getId().getValor().getValor();
    return domain.getCompetencies().stream().map(comp -> {
      CompetencyItemEntity entity = new CompetencyItemEntity();
      entity.setId(UUID.randomUUID());
      entity.setEvaluationId(evalId);
      entity.setCompetencyCode(comp.getCompetencyCode());
      entity.setCompetencyName(comp.getCompetencyName());
      entity.setCategory(comp.getCategory().getCode());
      entity.setScore(comp.getScore());
      return entity;
    }).collect(Collectors.toList());
  }

  private IndividualObjective toDomainObjective(IndividualObjectiveEntity entity) {
    return IndividualObjective.reconstruct(
        entity.getObjectiveCode(),
        entity.getDescription(),
        entity.getIndicator(),
        entity.getTargetValue(),
        entity.getAchievedValue(),
        entity.getScore(),
        entity.getWeight()
    );
  }

  private CompetencyItem toDomainCompetency(CompetencyItemEntity entity) {
    return CompetencyItem.reconstruct(
        entity.getCompetencyCode(),
        entity.getCompetencyName(),
        CompetencyCategory.fromCodeOrThrow(entity.getCategory()),
        entity.getScore()
    );
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
