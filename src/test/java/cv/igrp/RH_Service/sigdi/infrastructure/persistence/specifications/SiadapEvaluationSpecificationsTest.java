package cv.igrp.RH_Service.sigdi.infrastructure.persistence.specifications;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import cv.igrp.RH_Service.sigdi.application.constants.EvaluationPhase;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.SiadapEvaluationEntity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

/**
 * Prova da matriz de eixos de {@link SiadapEvaluationSpecifications#byFilters}: qual predicado
 * é montado e qual não é, para cada combinação de ano, unidade orgânica e fase. Todas as
 * asserções incidem sobre as chamadas feitas ao {@link CriteriaBuilder}/{@link Root} — nenhuma
 * é sobre contagem de linhas devolvidas, porque o defeito que esta fase corrige é um parâmetro
 * aceite e descartado em silêncio, invisível a um teste que só conte resultados.
 */
@ExtendWith(MockitoExtension.class)
class SiadapEvaluationSpecificationsTest {

  @Mock
  private Root<SiadapEvaluationEntity> root;

  @Mock
  private CriteriaQuery<?> query;

  @Mock
  private CriteriaBuilder cb;

  private Path<Object> yearPath;
  private Path<Object> organicUnitIdPath;
  private Path<Object> evaluationPhasePath;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUp() {
    yearPath = mock(Path.class);
    organicUnitIdPath = mock(Path.class);
    evaluationPhasePath = mock(Path.class);

    Mockito.lenient().doReturn(yearPath).when(root).get("year");
    Mockito.lenient().doReturn(organicUnitIdPath).when(root).get("organicUnitId");
    Mockito.lenient().doReturn(evaluationPhasePath).when(root).get("evaluationPhase");

    // Nota: CriteriaBuilder.equal tem duas sobrecargas -- equal(Expression, Expression) e
    // equal(Expression, Object). O código de produção invoca sempre a segunda (o valor é sempre
    // String), por isso o segundo matcher é any(Object.class) e não any() puro: any() sozinho
    // resolveria, em tempo de compilação, para a sobrecarga Expression/Expression -- uma
    // sobrecarga que nunca é chamada em runtime -- e a verificação falharia por engano.
    Mockito.lenient().when(cb.equal(any(), any(Object.class))).thenReturn(mock(Predicate.class));
    Mockito.lenient().when(cb.and(any(Predicate[].class))).thenReturn(mock(Predicate.class));
  }

  @Test
  void allThreeAxesFilledProduceThreeEqualPredicates() {
    Specification<SiadapEvaluationEntity> spec =
        SiadapEvaluationSpecifications.byFilters(2026, "unit-1", EvaluationPhase.HARMONIZATION);

    spec.toPredicate(root, query, cb);

    verify(cb).equal(yearPath, "2026");
    verify(cb).equal(organicUnitIdPath, "unit-1");
    verify(cb).equal(evaluationPhasePath, "HARMONIZATION");
    verify(cb, times(3)).equal(any(), any(Object.class));
  }

  @Test
  void onlyYearProducesSingleEqualPredicateAndNeverTouchesOtherAxes() {
    Specification<SiadapEvaluationEntity> spec =
        SiadapEvaluationSpecifications.byFilters(2026, null, null);

    spec.toPredicate(root, query, cb);

    verify(cb, times(1)).equal(any(), any(Object.class));
    verify(cb).equal(yearPath, "2026");
    verify(root, never()).get("organicUnitId");
    verify(root, never()).get("evaluationPhase");
  }

  @Test
  void onlyPhaseWithYearNullProducesSingleEqualPredicateAndNeverTouchesYear() {
    Specification<SiadapEvaluationEntity> spec =
        SiadapEvaluationSpecifications.byFilters(null, null, EvaluationPhase.SELF_EVALUATION);

    spec.toPredicate(root, query, cb);

    verify(cb, times(1)).equal(any(), any(Object.class));
    verify(cb).equal(evaluationPhasePath, "SELF_EVALUATION");
    verify(root, never()).get("year");
  }

  @Test
  void blankOrganicUnitIdIsTreatedAsAbsentAxis() {
    Specification<SiadapEvaluationEntity> spec =
        SiadapEvaluationSpecifications.byFilters(null, "   ", null);

    spec.toPredicate(root, query, cb);

    verify(root, never()).get("organicUnitId");
  }

  @Test
  void allThreeAxesNullProduceNoPredicatesAndTouchNoAttribute() {
    Specification<SiadapEvaluationEntity> spec =
        SiadapEvaluationSpecifications.byFilters(null, null, null);

    spec.toPredicate(root, query, cb);

    verify(cb, never()).equal(any(), any(Object.class));
    verify(root, never()).get(anyString());
  }

  @Test
  void yearIsComparedAsStringNotInteger() {
    Specification<SiadapEvaluationEntity> spec =
        SiadapEvaluationSpecifications.byFilters(2026, null, null);

    spec.toPredicate(root, query, cb);

    verify(cb).equal(yearPath, "2026");
    verify(cb, never()).equal(yearPath, 2026);
  }
}
