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

  /**
   * Apaga a avaliação identificada por {@code id}, se existir. Fase 120, plano 02 ({@code
   * PRZ-04}).
   *
   * <p><b>Porque isto entra pelo repositório e não pelo agregado {@link SiadapEvaluation}.</b>
   * (a) Apagar <strong>não é uma transição de domínio</strong> -- {@link SiadapEvaluation} tem
   * dezanove métodos públicos e nenhum apaga, porque uma transição de domínio leva o objeto de um
   * estado válido a outro, e a remoção não faz isso: anula um facto que não devia ter existido.
   * (b) Por isso a operação entra pelo repositório, e não por um método novo no agregado. (c) O
   * único chamador legítimo é a reversão de um lote de geração de formulários ({@code PRZ-04},
   * Fase 120) -- e só invoca isto sobre avaliações que esse mesmo lote criou e que continuam
   * exatamente como nasceram; ver {@code FormGenerationBatchRevertService} para a fronteira
   * completa do que é apagável.
   *
   * @return {@code true} se a avaliação existia e foi apagada; {@code false} se não existia. A
   *     ausência não lança exceção -- a idempotência de quem chama duas vezes é do chamador, não
   *     uma exceção a apanhar aqui.
   */
  boolean deleteById(SiadapEvaluationId id);
}

