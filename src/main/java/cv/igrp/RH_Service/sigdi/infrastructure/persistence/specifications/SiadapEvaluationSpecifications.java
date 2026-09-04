package cv.igrp.RH_Service.sigdi.infrastructure.persistence.specifications;

import cv.igrp.RH_Service.sigdi.application.constants.EvaluationPhase;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.SiadapEvaluationEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Composição do filtro combinável (ano, unidade orgânica, fase) sobre {@link SiadapEvaluationEntity}.
 * <p>
 * Existe como classe própria, em vez do {@code buildSpec} privado que é o molde habitual
 * deste projeto (ver {@code StrategicGoalRepositoryImpl}), porque este filtro tem dois
 * consumidores: {@code SiadapEvaluationRepositoryImpl} (este plano) e
 * {@code ListSiadapEvaluationsQueryHandler} (Plano 02). Duplicar a composição em dois
 * adaptadores era o risco concreto a eliminar — os dois caminhos divergirem em silêncio.
 */
public final class SiadapEvaluationSpecifications {

  private SiadapEvaluationSpecifications() {
  }

  /**
   * Constrói a {@link Specification} combinável. Cada eixo é opcional: {@code year == null},
   * {@code organicUnitId} em branco, ou {@code phase == null} significa "não filtrar por este
   * eixo" — nunca uma comparação com {@code null} nem com string vazia (IN-02).
   *
   * @param year          ano do ciclo, comparado como {@code String} porque a coluna {@code year}
   *                       está mapeada como {@code String} na entidade
   * @param organicUnitId unidade orgânica; string em branco é tratada como eixo ausente
   * @param phase         fase do ciclo; comparada por {@link EvaluationPhase#getCode()}, nunca por
   *                       {@code name()}, porque a coluna é {@code String}
   */
  public static Specification<SiadapEvaluationEntity> byFilters(Integer year, String organicUnitId,
      EvaluationPhase phase) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      if (year != null)
        predicates.add(cb.equal(root.get("year"), year.toString()));
      if (organicUnitId != null && !organicUnitId.isBlank())
        predicates.add(cb.equal(root.get("organicUnitId"), organicUnitId));
      if (phase != null)
        predicates.add(cb.equal(root.get("evaluationPhase"), phase.getCode()));
      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}
