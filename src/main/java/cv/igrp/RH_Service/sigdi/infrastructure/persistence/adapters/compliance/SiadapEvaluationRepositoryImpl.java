package cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters.compliance;

import cv.igrp.RH_Service.sigdi.application.constants.EvaluationPhase;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.SiadapEvaluationEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.IndividualObjectiveEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.CompetencyItemEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.SiadapEvaluationEntityRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.IndividualObjectiveEntityRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.CompetencyItemEntityRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.specifications.SiadapEvaluationSpecifications;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapEvaluation;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapEvaluationRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.SiadapEvaluationId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.compliance.SiadapEvaluationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
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
    // IN-02: guard organicUnitId consistent with findByEmployeeAndYear's own-parameter guard
    // above. Without this, Spring Data would silently rewrite the derived query's equality
    // predicate to "organic_unit_id IS NULL" for a null organicUnitId -- the opposite of what a
    // method named "filter by unit" suggests. Not live today (the only caller already filters
    // before calling), but delegates to findByYear(year) for defensive safety, consistent with how
    // CloseEvaluationsCommandHandler.validateQuotas() already treats blank-as-null.
    if (organicUnitId == null || organicUnitId.isBlank()) return findByYear(year);
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

  // D-02: ao contrário de findByYear(Integer), que devolve lista vazia para ano nulo, aqui um
  // eixo nulo (incluindo year) significa "não filtrar por este eixo" -- os três eixos são
  // opcionais por desenho (SIA-01 critério 4). O que limita o resultado não é o ano ser
  // obrigatório, é a paginação ser obrigatória na assinatura. A Fase 102 depende exatamente do
  // caso year=null, phase=<fase> para encontrar candidatos sem um pedido HTTP -- não
  // "harmonizar" esta guarda com findByYear numa leitura futura.
  @Transactional(readOnly = true)
  @Override
  public List<SiadapEvaluation> findAll(Integer year, String organicUnitId, EvaluationPhase phase,
      int page, int size) {
    Specification<SiadapEvaluationEntity> spec =
        SiadapEvaluationSpecifications.byFilters(year, organicUnitId, phase);
    return jpaRepository.findAll(spec, PageRequest.of(page, size))
        .stream()
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
  public long countAll(Integer year, String organicUnitId, EvaluationPhase phase) {
    Specification<SiadapEvaluationEntity> spec =
        SiadapEvaluationSpecifications.byFilters(year, organicUnitId, phase);
    return jpaRepository.count(spec);
  }
}
