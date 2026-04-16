package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.SiadapConfigEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.SiadapEvaluationEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.SiadapConfigEntityRepository;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.SiadapEvaluationEntityRepository;
import cv.igrp.RH_Service.sigdi.application.dto.CloseEvaluationsRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.CloseEvaluationsResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.QuotaViolationDTO;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class CloseEvaluationsCommandHandler
    implements CommandHandler<CloseEvaluationsCommand, ResponseEntity<CloseEvaluationsResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CloseEvaluationsCommandHandler.class);

  private final SiadapEvaluationEntityRepository evaluationRepository;
  private final SiadapConfigEntityRepository configRepository;

  public CloseEvaluationsCommandHandler(SiadapEvaluationEntityRepository evaluationRepository,
                                         SiadapConfigEntityRepository configRepository) {
    this.evaluationRepository = evaluationRepository;
    this.configRepository = configRepository;
  }

  @IgrpCommandHandler
  @Transactional
  public ResponseEntity<CloseEvaluationsResponseDTO> handle(CloseEvaluationsCommand command) {
    LOGGER.debug("CloseEvaluationsCommand: {}", command);

    CloseEvaluationsRequestDTO req = command.getBody();
    String year = req.getYear().toString();

    List<SiadapEvaluationEntity> evaluations = evaluationRepository.findByYear(year);

    if (evaluations.isEmpty()) {
      throw IgrpResponseStatusException.notFound(
          "No evaluations found for year: " + year);
    }

    // Validate quotas before closing (SIGDI-SIA-001)
    validateQuotas(evaluations, req.getYear());

    // Mark all evaluations as validated (close them)
    for (SiadapEvaluationEntity eval : evaluations) {
      eval.setValidatedQuota(true);
    }
    evaluationRepository.saveAll(evaluations);

    CloseEvaluationsResponseDTO response = new CloseEvaluationsResponseDTO();
    response.setOrganicUnitId(req.getOrganicUnitId());
    response.setOrganicUnitName(null);
    response.setYear(req.getYear());
    response.setEvaluationsClosed(evaluations.size());
    response.setClosedAt(LocalDateTime.now().toString());

    return ResponseEntity.ok(response);
  }

  private void validateQuotas(List<SiadapEvaluationEntity> evaluations, Integer year) {
    long total = evaluations.size();
    long excellentCount = evaluations.stream()
        .filter(e -> "EXCELLENT".equals(e.getMeritRating()))
        .count();

    BigDecimal excellentQuotaPct = configRepository.findByFiscalYear(year)
        .map(SiadapConfigEntity::getExcellentQuota)
        .filter(q -> q != null)
        .orElse(new BigDecimal("25"));

    int excellentAllowed = total > 0
        ? excellentQuotaPct.multiply(new BigDecimal(total))
            .divide(new BigDecimal("100"), 0, RoundingMode.FLOOR).intValue()
        : 0;

    if (excellentCount > excellentAllowed) {
      throw IgrpResponseStatusException.of(
          org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,
          "SIGDI-SIA-001: Quota de Excelente excedida. Permitido: " + excellentAllowed
              + ", Atribuído: " + excellentCount + ". Corrija antes de fechar.");
    }
  }
}
