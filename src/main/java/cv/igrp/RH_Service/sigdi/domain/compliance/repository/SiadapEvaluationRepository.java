package cv.igrp.RH_Service.sigdi.domain.compliance.repository;

import cv.igrp.RH_Service.sigdi.application.constants.EvaluationPhase;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapEvaluation;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.SiadapEvaluationId;

import java.util.List;
import java.util.Optional;

public interface SiadapEvaluationRepository {

  SiadapEvaluation save(SiadapEvaluation evaluation);

  Optional<SiadapEvaluation> findById(SiadapEvaluationId id);

  Optional<SiadapEvaluation> findByEmployeeAndYear(String employeeId, Integer year);

  List<SiadapEvaluation> findByYear(Integer year);

  List<SiadapEvaluation> findByYearAndOrganicUnitId(Integer year, String organicUnitId);

  List<SiadapEvaluation> saveAll(List<SiadapEvaluation> evaluations);

  /**
   * Consulta combinável por ano, unidade orgânica e fase (SIA-01, critério 4), com paginação
   * obrigatória. Ao contrário de {@link #findByYear(Integer)}, aqui cada eixo omitido (incluindo
   * {@code year}) significa "não filtrar por este eixo", nunca "devolver lista vazia" -- ver
   * {@code SiadapEvaluationRepositoryImpl} para a justificação completa (D-02).
   */
  List<SiadapEvaluation> findAll(Integer year, String organicUnitId, EvaluationPhase phase, int page, int size);

  /**
   * Contagem correspondente a {@link #findAll(Integer, String, EvaluationPhase, int, int)}, sem
   * paginação.
   */
  long countAll(Integer year, String organicUnitId, EvaluationPhase phase);
}

