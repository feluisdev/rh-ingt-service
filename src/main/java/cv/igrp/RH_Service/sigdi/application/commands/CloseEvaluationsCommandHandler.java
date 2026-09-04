package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.EvaluationPhase;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.SiadapConfigEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.SiadapConfigEntityRepository;
import cv.igrp.RH_Service.sigdi.application.dto.CloseEvaluationsRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.CloseEvaluationsResponseDTO;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapEvaluation;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapEvaluationRepository;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

// ACTOR-CHECK: ENFORCED -- siadap.avaliacoes.fecharEmLote permission, guarded by @PreAuthorize on ComplianceController#closeEvaluations
@Component
public class CloseEvaluationsCommandHandler
    implements CommandHandler<CloseEvaluationsCommand, ResponseEntity<CloseEvaluationsResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CloseEvaluationsCommandHandler.class);

  /**
   * Sentinel classifier key standing in for organicUnitId == null/blank when grouping evaluations
   * by organic unit for per-unit quota validation (WR-01 fix, 81-BACKEND-REVIEW.md). Collectors
   * .groupingBy's accumulator explicitly rejects a null classification key -- throws NPE "element
   * cannot be mapped to a null key" -- so null/blank organicUnitId values are mapped to this
   * sentinel before grouping. Mirrors GetQuotaValidationQueryHandler's UNASSIGNED_KEY pattern
   * exactly (same sentinel value). Never collides with a real organicUnitId since it is not a
   * valid UUID.
   */
  private static final String UNASSIGNED_KEY = "__UNASSIGNED__";

  private final SiadapEvaluationRepository evaluationRepository;
  // SiadapConfigEntityRepository used for cross-aggregate read-only config lookup
  private final SiadapConfigEntityRepository configRepository;

  public CloseEvaluationsCommandHandler(SiadapEvaluationRepository evaluationRepository,
                                         SiadapConfigEntityRepository configRepository) {
    this.evaluationRepository = evaluationRepository;
    this.configRepository = configRepository;
  }

  @IgrpCommandHandler
  @Transactional
  public ResponseEntity<CloseEvaluationsResponseDTO> handle(CloseEvaluationsCommand command) {
    LOGGER.debug("CloseEvaluationsCommand: {}", command);

    CloseEvaluationsRequestDTO req = command.getBody();
    Integer year = req.getYear();
    String organicUnitId = req.getOrganicUnitId();

    // Per-unit scoping (81-CONTEXT.md Open Question 1): a non-blank organicUnitId scopes the
    // close to that unit; null/blank preserves the pre-existing all-units-for-the-year behaviour.
    List<SiadapEvaluation> evaluations = (organicUnitId != null && !organicUnitId.isBlank())
        ? evaluationRepository.findByYearAndOrganicUnitId(year, organicUnitId)
        : evaluationRepository.findByYear(year);

    if (evaluations.isEmpty()) {
      throw IgrpResponseStatusException.notFound(
          "No evaluations found for year: " + year);
    }

    // Batch HARMONIZATION-phase guard (SIGDI-SIA-004, 81-CONTEXT.md Open Question 2): hoists
    // SiadapEvaluation.markQuotaValidated()'s single-evaluation phase invariant to a single,
    // batch-scoped, clearly-worded error BEFORE validateQuotas()/markQuotaValidated() ever run,
    // instead of letting that invariant bleed through as a confusing per-evaluation error.
    long notYetHarmonization = evaluations.stream()
        .filter(e -> !EvaluationPhase.HARMONIZATION.equals(e.getPhase()))
        .count();
    if (notYetHarmonization > 0) {
      throw IgrpResponseStatusException.of(
          HttpStatus.UNPROCESSABLE_ENTITY,
          "SIGDI-SIA-004: " + notYetHarmonization
              + " avaliação(ões) ainda não estão na fase de Harmonização e não podem ser fechadas.");
    }

    // Validate quotas before closing (SIGDI-SIA-001/002/003)
    validateQuotas(evaluations, year);

    // Mark all evaluations as quota-validated using domain behaviour
    List<SiadapEvaluation> closed = evaluations.stream()
        .map(SiadapEvaluation::markQuotaValidated)
        .collect(Collectors.toList());

    evaluationRepository.saveAll(closed);

    CloseEvaluationsResponseDTO response = new CloseEvaluationsResponseDTO();
    // WR-02 fix: normalize against the same isBlank() check used above to decide the fetch route,
    // so a whitespace-only organicUnitId (routed as "close all units") doesn't echo back implying a
    // specific unit was scoped when every unit's evaluations for the year were actually closed.
    String responseOrganicUnitId = (organicUnitId != null && !organicUnitId.isBlank()) ? organicUnitId : null;
    response.setOrganicUnitId(responseOrganicUnitId);
    response.setOrganicUnitName(null);
    response.setYear(year);
    response.setEvaluationsClosed(closed.size());
    response.setClosedAt(LocalDateTime.now().toString());

    return ResponseEntity.ok(response);
  }

  private void validateQuotas(List<SiadapEvaluation> evaluations, Integer year) {
    // Cross-aggregate config read — acceptable to use JPA directly here. Single lookup,
    // reused for all three checks below (min-collaborators, Excelente, Bom), across every unit.
    Optional<SiadapConfigEntity> config = configRepository.findByFiscalYear(year);

    Integer minCollaborators = config.map(SiadapConfigEntity::getMinCollaboratorsForQuota).orElse(null);
    // IN-01: no .filter(q -> q != null) here -- Optional.map() already collapses a null-returning
    // mapper into Optional.empty(), so the value reaching .orElse() is never null.
    BigDecimal excellentQuotaPct = config.map(SiadapConfigEntity::getExcellentQuota).orElse(new BigDecimal("25"));
    BigDecimal goodQuotaPct = config.map(SiadapConfigEntity::getGoodQuota).orElse(new BigDecimal("35"));

    // WR-01 fix (81-BACKEND-REVIEW.md): validate quotas PER ORGANIC UNIT, never as one pooled
    // aggregate — even when the caller closed "all units" (organicUnitId == null/blank), which is
    // the only path the current UI can reach. Previously this method computed one combined
    // total/excellentCount/goodCount across every evaluation passed in, so a small unit at 100%
    // Excelente could be silently masked when pooled with a larger compliant unit's evaluations.
    // Grouping here mirrors GetQuotaValidationQueryHandler's exact null-safe UNASSIGNED_KEY
    // sentinel pattern, since Collectors.groupingBy's accumulator rejects a null classifier key.
    Map<String, List<SiadapEvaluation>> byUnit = evaluations.stream()
        .collect(Collectors.groupingBy(e -> (e.getOrganicUnitId() != null && !e.getOrganicUnitId().isBlank())
            ? e.getOrganicUnitId() : UNASSIGNED_KEY));

    // Fail-fast on the first violating unit encountered, consistent with every other check in this
    // handler (batch HARMONIZATION guard, min-collaborators, Excelente, Bom), all of which throw a
    // single structured IgrpResponseStatusException immediately upon detecting a violation rather
    // than aggregating multiple violations into one combined message.
    for (List<SiadapEvaluation> unitEvaluations : byUnit.values()) {
      validateUnitQuotas(unitEvaluations, minCollaborators, excellentQuotaPct, goodQuotaPct);
    }
  }

  /**
   * Per-unit quota checks extracted from {@link #validateQuotas} (WR-01 fix): minCollaboratorsForQuota
   * gate (SIGDI-SIA-002), then Excelente (SIGDI-SIA-001), then Bom (SIGDI-SIA-003), in that order —
   * identical logic/messages to what previously ran once over the whole batch, now run once per
   * organic-unit group so a unit's own compliance can never be masked by another unit's evaluations.
   */
  private void validateUnitQuotas(List<SiadapEvaluation> unitEvaluations, Integer minCollaborators,
      BigDecimal excellentQuotaPct, BigDecimal goodQuotaPct) {
    long total = unitEvaluations.size();

    // 1. minCollaboratorsForQuota gate FIRST (SIGDI-SIA-002): a clearly-messaged BLOCK when the
    // configured minimum is not met. null/0 = gate disabled, preserving behaviour for
    // unconfigured tenants (81-CONTEXT.md decision #3).
    if (minCollaborators != null && minCollaborators > 0 && total < minCollaborators) {
      throw IgrpResponseStatusException.of(
          HttpStatus.UNPROCESSABLE_ENTITY,
          "SIGDI-SIA-002: Número insuficiente de avaliações para validar quotas. Mínimo: "
              + minCollaborators + ", atual: " + total + ".");
    }

    // 2. Excelente check (SIGDI-SIA-001) — unchanged.
    long excellentCount = unitEvaluations.stream()
        .filter(e -> e.getMeritRating() != null && "EXCELLENT".equals(e.getMeritRating().getCode()))
        .count();

    // IN-01: no total > 0 guard -- unitEvaluations comes from a Collectors.groupingBy value list,
    // which is never created empty, so total is always > 0 here.
    int excellentAllowed = excellentQuotaPct.multiply(new BigDecimal(total))
        .divide(new BigDecimal("100"), 0, RoundingMode.FLOOR).intValue();

    if (excellentCount > excellentAllowed) {
      throw IgrpResponseStatusException.of(
          HttpStatus.UNPROCESSABLE_ENTITY,
          "SIGDI-SIA-001: Quota de Excelente excedida. Permitido: " + excellentAllowed
              + ", Atribuído: " + excellentCount + ". Corrija antes de fechar.");
    }

    // 3. Bom check (SIGDI-SIA-003) — mirrors the Excelente check above, config-driven goodQuota
    // instead of the previous hardcoded 35%. Independent of the Excelente outcome.
    long goodCount = unitEvaluations.stream()
        .filter(e -> e.getMeritRating() != null && "GOOD".equals(e.getMeritRating().getCode()))
        .count();

    // IN-01: no total > 0 guard -- same invariant as excellentAllowed above.
    int goodAllowed = goodQuotaPct.multiply(new BigDecimal(total))
        .divide(new BigDecimal("100"), 0, RoundingMode.FLOOR).intValue();

    if (goodCount > goodAllowed) {
      throw IgrpResponseStatusException.of(
          HttpStatus.UNPROCESSABLE_ENTITY,
          "SIGDI-SIA-003: Quota de Bom excedida. Permitido: " + goodAllowed
              + ", Atribuído: " + goodCount + ". Corrija antes de fechar.");
    }
  }
}
