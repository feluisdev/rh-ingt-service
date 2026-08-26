package cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters.compliance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.sigdi.application.constants.EvaluationPhase;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.CompetencyItemEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.IndividualObjectiveEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.SiadapEvaluationEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.CompetencyItemEntityRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.IndividualObjectiveEntityRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.SiadapEvaluationEntityRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.compliance.SiadapEvaluationMapper;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

/**
 * Prova de que o adaptador passa a {@link Specification} composta por
 * {@code SiadapEvaluationSpecifications.byFilters} e o {@link Pageable} correto ao repositório
 * JPA -- nunca sobre contagem de linhas devolvidas.
 */
@ExtendWith(MockitoExtension.class)
class SiadapEvaluationRepositoryImplTest {

  @Mock
  private SiadapEvaluationEntityRepository jpaRepository;

  @Mock
  private IndividualObjectiveEntityRepository objectiveJpaRepository;

  @Mock
  private CompetencyItemEntityRepository competencyJpaRepository;

  @Mock
  private SiadapEvaluationMapper mapper;

  @InjectMocks
  private SiadapEvaluationRepositoryImpl adapter;

  @Mock
  private Root<SiadapEvaluationEntity> root;

  @Mock
  private CriteriaQuery<?> query;

  @Mock
  private CriteriaBuilder cb;

  @SuppressWarnings("unchecked")
  private void stubCriteriaMocks() {
    Path<Object> yearPath = mock(Path.class);
    Path<Object> organicUnitIdPath = mock(Path.class);
    Path<Object> evaluationPhasePath = mock(Path.class);

    Mockito.lenient().doReturn(yearPath).when(root).get("year");
    Mockito.lenient().doReturn(organicUnitIdPath).when(root).get("organicUnitId");
    Mockito.lenient().doReturn(evaluationPhasePath).when(root).get("evaluationPhase");

    Mockito.lenient().when(cb.equal(any(), any(Object.class))).thenReturn(mock(Predicate.class));
    Mockito.lenient().when(cb.and(any(Predicate[].class))).thenReturn(mock(Predicate.class));
  }

  @Test
  void findAllPassesSpecificationAndPageableToJpaRepository() {
    stubCriteriaMocks();

    SiadapEvaluationEntity entity = mock(SiadapEvaluationEntity.class);
    when(jpaRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(entity)));
    when(objectiveJpaRepository.findByEvaluationId(any())).thenReturn(List.of());
    when(competencyJpaRepository.findByEvaluationId(any())).thenReturn(List.of());

    adapter.findAll(2026, "unit-1", EvaluationPhase.HARMONIZATION, 2, 50);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Specification<SiadapEvaluationEntity>> specCaptor =
        ArgumentCaptor.forClass(Specification.class);
    ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
    verify(jpaRepository).findAll(specCaptor.capture(), pageableCaptor.capture());

    assertEquals(2, pageableCaptor.getValue().getPageNumber());
    assertEquals(50, pageableCaptor.getValue().getPageSize());

    specCaptor.getValue().toPredicate(root, query, cb);
    verify(cb, Mockito.times(3)).equal(any(), any(Object.class));

    verify(mapper).toDomain(entity, List.of(), List.of());
  }

  @Test
  void findAllWithOnlyPhaseProvesYearIsOptional() {
    stubCriteriaMocks();

    when(jpaRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of()));

    adapter.findAll(null, null, EvaluationPhase.SELF_EVALUATION, 0, 200);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Specification<SiadapEvaluationEntity>> specCaptor =
        ArgumentCaptor.forClass(Specification.class);
    verify(jpaRepository).findAll(specCaptor.capture(), any(Pageable.class));

    specCaptor.getValue().toPredicate(root, query, cb);
    verify(cb, Mockito.times(1)).equal(any(), any(Object.class));
    verify(root, never()).get("year");
  }

  @Test
  void countAllDelegatesToJpaRepositoryCountAndReturnsItUnchanged() {
    stubCriteriaMocks();

    when(jpaRepository.count(any(Specification.class))).thenReturn(7L);

    long result = adapter.countAll(2026, null, EvaluationPhase.HARMONIZATION);

    assertEquals(7L, result);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Specification<SiadapEvaluationEntity>> specCaptor =
        ArgumentCaptor.forClass(Specification.class);
    verify(jpaRepository).count(specCaptor.capture());

    specCaptor.getValue().toPredicate(root, query, cb);
    verify(root, never()).get("organicUnitId");
  }

  @Test
  void findAllNeverFallsBackToLegacyDerivedQueries() {
    stubCriteriaMocks();

    when(jpaRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of()));

    adapter.findAll(2026, "unit-1", EvaluationPhase.HARMONIZATION, 0, 20);

    verify(jpaRepository, never()).findByYear(anyString());
    verify(jpaRepository, never()).findByYearAndOrganicUnitId(anyString(), anyString());
  }
}
