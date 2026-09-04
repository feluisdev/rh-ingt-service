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
import org.springframework.data.repository.CrudRepository;
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

  /**
   * Fase 120, plano 02 ({@code PRZ-04}). Apaga a avaliação ao nível da entidade, depois de
   * apagar primeiro as suas filhas de objetivos e de competências.
   *
   * <p><b>1. Porque {@code delete(entity)} e nunca um JPQL de remoção.</b>
   * {@code SiadapEvaluationEntity} é {@code @Audited}; o Envers só escreve a revisão de remoção
   * ({@code revtype = 2}) se a remoção passar pelo ciclo de vida da entidade (o
   * {@code EntityManager} a processar um {@code remove()} de facto). Uma consulta anotada como
   * escrita directa, ou um apagar em bloco de várias linhas de uma vez, saltam esse ciclo de vida
   * em silêncio -- a linha desaparece da tabela, mas a auditoria nunca fica a saber que isso
   * aconteceu, e não há erro nenhum a avisar disso.
   *
   * <p><b>2. O que fica na auditoria depois de apagar.</b>
   * {@code audit_schema.t_siadap_evaluations_aud} tem {@code PRIMARY KEY (id, rev)}, todas as
   * colunas de negócio anuláveis, e nenhuma {@code FOREIGN KEY} para a tabela viva ({@code V20},
   * linhas 48-70) -- por isso apagar escreve uma revisão nova com todos os campos a nulo, e as
   * revisões anteriores sobrevivem a apontar para uma linha que já não existe. É o resultado
   * esperado, não um defeito: é o mesmo precedente que levou a {@code V32} a recusar um
   * {@code CHECK} na tabela-sombra do Envers, precisamente porque uma linha histórica tem de
   * sobreviver à remoção da linha que referencia.
   *
   * <p><b>3. Porque não se tocam as quatro tabelas <i>interim</i>.</b>
   * ({@code SiadapInterimFeedbackEntity}, {@code SiadapInterimObjectiveRevisionEntity},
   * {@code SiadapInterimImprovementActionEntity}, {@code SiadapInterimCompetencyObservationEntity})
   * só podem receber linhas a partir de {@code IN_PROGRESS} -- ver
   * {@code SiadapEvaluation.recordObjectiveAchievement} e {@code applyObjectiveRevision}. A
   * fronteira do apagável (Fase 120, plano 02) só permite apagar avaliações em {@code OPEN} e sem
   * objetivos nem competências, portanto nunca podem ter linhas nessas quatro tabelas. Nenhuma
   * delas tem {@code FOREIGN KEY} para {@code t_siadap_evaluations}, logo a remoção também não
   * falharia por causa delas -- só ficariam órfãs se algum dia essa fronteira mudasse, o que este
   * método não faz por conta própria.
   */
  @Transactional
  @Override
  public boolean deleteById(SiadapEvaluationId id) {
    UUID evalUuid = id.getValor().getValor();
    Optional<SiadapEvaluationEntity> found = jpaRepository.findById(evalUuid);
    if (found.isEmpty()) {
      return false;
    }

    SiadapEvaluationEntity entity = found.get();
    // Mesma ordem que o save() já usa: filhas primeiro, entidade-pai depois.
    objectiveJpaRepository.deleteByEvaluationId(evalUuid);
    competencyJpaRepository.deleteByEvaluationId(evalUuid);

    // Estreitamento deliberado ao tipo CrudRepository: a partir do Spring Data JPA 3.5,
    // JpaSpecificationExecutor#delete(Specification<T>) e CrudRepository#delete(T) tornam
    // jpaRepository.delete(entity) ambíguo em tempo de compilação, porque
    // SiadapEvaluationEntityRepository estende os dois. O estreitamento resolve a ambiguidade sem
    // mudar qual delete() corre -- continua a ser a remoção ao nível da entidade, nunca a
    // remoção em bloco por especificação.
    CrudRepository<SiadapEvaluationEntity, UUID> crud = jpaRepository;
    crud.delete(entity);
    return true;
  }
}
