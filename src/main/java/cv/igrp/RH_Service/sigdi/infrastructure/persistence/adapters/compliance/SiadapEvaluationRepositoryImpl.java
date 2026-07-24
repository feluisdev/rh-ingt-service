package cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters.compliance;

import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.SiadapEvaluationEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.IndividualObjectiveEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.CompetencyItemEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.SiadapEvaluationEntityRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.IndividualObjectiveEntityRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.CompetencyItemEntityRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapEvaluation;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapEvaluationRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.SiadapEvaluationId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.compliance.SiadapEvaluationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SiadapEvaluationRepositoryImpl implements SiadapEvaluationRepository {

  private final SiadapEvaluationEntityRepository jpaRepository;
  private final IndividualObjectiveEntityRepository objectiveJpaRepository;
  private final CompetencyItemEntityRepository competencyJpaRepository;
  private final SiadapEvaluationMapper mapper;

  @Transactional
  @Override
  public SiadapEvaluation save(SiadapEvaluation evaluation) {
    SiadapEvaluationEntity entity = mapper.toEntity(evaluation);
    SiadapEvaluationEntity saved = jpaRepository.save(entity);

    UUID evalId = saved.getId();
    // Delete old objectives and insert new ones
    objectiveJpaRepository.deleteByEvaluationId(evalId);
    List<IndividualObjectiveEntity> objectiveEntities = mapper.toObjectiveEntities(evaluation);
    objectiveEntities.forEach(obj -> obj.setEvaluationId(evalId)); // Ensure ID match
    objectiveJpaRepository.saveAll(objectiveEntities);

    // Delete old competencies and insert new ones
    competencyJpaRepository.deleteByEvaluationId(evalId);
    List<CompetencyItemEntity> competencyEntities = mapper.toCompetencyEntities(evaluation);
    competencyEntities.forEach(comp -> comp.setEvaluationId(evalId)); // Ensure ID match
    competencyJpaRepository.saveAll(competencyEntities);

    return mapper.toDomain(saved, objectiveEntities, competencyEntities);
  }

  @Transactional(readOnly = true)
  @Override
  public Optional<SiadapEvaluation> findById(SiadapEvaluationId id) {
    UUID evalUuid = id.getValor().getValor();
    return jpaRepository.findById(evalUuid)
        .map(entity -> {
          List<IndividualObjectiveEntity> objectives = objectiveJpaRepository.findByEvaluationId(evalUuid);
          List<CompetencyItemEntity> competencies = competencyJpaRepository.findByEvaluationId(evalUuid);
          return mapper.toDomain(entity, objectives, competencies);
        });
  }

  @Transactional(readOnly = true)
  @Override
  public Optional<SiadapEvaluation> findByEmployeeAndYear(String employeeId, Integer year) {
    if (employeeId == null || employeeId.isBlank() || year == null) return Optional.empty();
    return jpaRepository.findByEmployeeIdAndYear(employeeId, year.toString())
        .map(entity -> {
          UUID evalUuid = entity.getId();
          List<IndividualObjectiveEntity> objectives = objectiveJpaRepository.findByEvaluationId(evalUuid);
          List<CompetencyItemEntity> competencies = competencyJpaRepository.findByEvaluationId(evalUuid);
          return mapper.toDomain(entity, objectives, competencies);
        });
  }

  @Transactional(readOnly = true)
  @Override
  public List<SiadapEvaluation> findByYear(Integer year) {
    if (year == null) return List.of();
    return jpaRepository.findByYear(year.toString()).stream()
        .map(entity -> {
          UUID evalUuid = entity.getId();
          List<IndividualObjectiveEntity> objectives = objectiveJpaRepository.findByEvaluationId(evalUuid);
          List<CompetencyItemEntity> competencies = competencyJpaRepository.findByEvaluationId(evalUuid);
          return mapper.toDomain(entity, objectives, competencies);
        })
        .toList();
  }

  @Transactional(readOnly = true)
  @Override
  public List<SiadapEvaluation> findByYearAndOrganicUnitId(Integer year, String organicUnitId) {
    if (year == null) return List.of();
    return jpaRepository.findByYearAndOrganicUnitId(year.toString(), organicUnitId).stream()
        .map(entity -> {
          UUID evalUuid = entity.getId();
          List<IndividualObjectiveEntity> objectives = objectiveJpaRepository.findByEvaluationId(evalUuid);
          List<CompetencyItemEntity> competencies = competencyJpaRepository.findByEvaluationId(evalUuid);
          return mapper.toDomain(entity, objectives, competencies);
        })
        .toList();
  }

  @Transactional
  @Override
  public List<SiadapEvaluation> saveAll(List<SiadapEvaluation> evaluations) {
    return evaluations.stream()
        .map(this::save)
        .collect(Collectors.toList());
  }
}
