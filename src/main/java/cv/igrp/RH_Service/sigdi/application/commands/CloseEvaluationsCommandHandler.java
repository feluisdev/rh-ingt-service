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
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CloseEvaluationsCommandHandler
    implements CommandHandler<CloseEvaluationsCommand, ResponseEntity<CloseEvaluationsResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CloseEvaluationsCommandHandler.class);

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
    response.setOrganicUnitId(req.getOrganicUnitId());
    response.setOrganicUnitName(null);
    response.setYear(year);
    response.setEvaluationsClosed(closed.size());
    response.setClosedAt(LocalDateTime.now().toString());

    return ResponseEntity.ok(response);
  }

  private void validateQuotas(List<SiadapEvaluation> evaluations, Integer year) {
    long total = evaluations.size();

    // Cross-aggregate config read — acceptable to use JPA directly here. Single lookup,
    // reused for all three checks below (min-collaborators, Excelente, Bom).
    Optional<SiadapConfigEntity> config = configRepository.findByFiscalYear(year);

    // 1. minCollaboratorsForQuota gate FIRST (SIGDI-SIA-002): a clearly-messaged BLOCK when the
    // configured minimum is not met. null/0 = gate disabled, preserving behaviour for
    // unconfigured tenants (81-CONTEXT.md decision #3).
    Integer minCollaborators = config.map(SiadapConfigEntity::getMinCollaboratorsForQuota).orElse(null);
    if (minCollaborators != null && minCollaborators > 0 && total < minCollaborators) {
      throw IgrpResponseStatusException.of(
          HttpStatus.UNPROCESSABLE_ENTITY,
          "SIGDI-SIA-002: Número insuficiente de avaliações para validar quotas. Mínimo: "
              + minCollaborators + ", atual: " + total + ".");
    }

    // 2. Excelente check (SIGDI-SIA-001) — unchanged.
    long excellentCount = evaluations.stream()
        .filter(e -> e.getMeritRating() != null && "EXCELLENT".equals(e.getMeritRating().getCode()))
        .count();

    BigDecimal excellentQuotaPct = config
        .map(SiadapConfigEntity::getExcellentQuota)
        .filter(q -> q != null)
        .orElse(new BigDecimal("25"));

    int excellentAllowed = total > 0
        ? excellentQuotaPct.multiply(new BigDecimal(total))
            .divide(new BigDecimal("100"), 0, RoundingMode.FLOOR).intValue()
        : 0;

    if (excellentCount > excellentAllowed) {
      throw IgrpResponseStatusException.of(
          HttpStatus.UNPROCESSABLE_ENTITY,
          "SIGDI-SIA-001: Quota de Excelente excedida. Permitido: " + excellentAllowed
              + ", Atribuído: " + excellentCount + ". Corrija antes de fechar.");
    }

    // 3. Bom check (SIGDI-SIA-003) — mirrors the Excelente check above, config-driven goodQuota
    // instead of the previous hardcoded 35%. Independent of the Excelente outcome.
    long goodCount = evaluations.stream()
        .filter(e -> e.getMeritRating() != null && "GOOD".equals(e.getMeritRating().getCode()))
        .count();

    BigDecimal goodQuotaPct = config
        .map(SiadapConfigEntity::getGoodQuota)
        .filter(q -> q != null)
        .orElse(new BigDecimal("35"));

    int goodAllowed = total > 0
        ? goodQuotaPct.multiply(new BigDecimal(total))
            .divide(new BigDecimal("100"), 0, RoundingMode.FLOOR).intValue()
        : 0;

    if (goodCount > goodAllowed) {
      throw IgrpResponseStatusException.of(
          HttpStatus.UNPROCESSABLE_ENTITY,
          "SIGDI-SIA-003: Quota de Bom excedida. Permitido: " + goodAllowed
              + ", Atribuído: " + goodCount + ". Corrija antes de fechar.");
    }
  }
}
