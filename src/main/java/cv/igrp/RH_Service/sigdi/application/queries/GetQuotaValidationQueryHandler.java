package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.SiadapConfigEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.SiadapConfigEntityRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.SiadapEvaluationEntityRepository;
import cv.igrp.RH_Service.sigdi.application.dto.QuotaUnitDTO;
import cv.igrp.RH_Service.sigdi.application.dto.QuotaValidationResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.QuotaViolationDTO;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
public class GetQuotaValidationQueryHandler
    implements QueryHandler<GetQuotaValidationQuery, ResponseEntity<QuotaValidationResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GetQuotaValidationQueryHandler.class);

  private final SiadapEvaluationEntityRepository evaluationRepository;
  private final SiadapConfigEntityRepository configRepository;

  public GetQuotaValidationQueryHandler(SiadapEvaluationEntityRepository evaluationRepository,
                                         SiadapConfigEntityRepository configRepository) {
    this.evaluationRepository = evaluationRepository;
    this.configRepository = configRepository;
  }

  @IgrpQueryHandler
  @Transactional(readOnly = true)
  public ResponseEntity<QuotaValidationResponseDTO> handle(GetQuotaValidationQuery query) {
    LOGGER.debug("GetQuotaValidationQuery: {}", query);

    String year = query.getYear().toString();
    long total = evaluationRepository.countByYear(year);
    long excellentCount = evaluationRepository.countByYearAndMeritRating(year, "EXCELLENT");
    long goodCount = evaluationRepository.countByYearAndMeritRating(year, "GOOD");

    // Load config for quota percentages (default 25% excellent, 35% good)
    BigDecimal excellentQuotaPct = configRepository.findByFiscalYear(query.getYear())
        .map(SiadapConfigEntity::getExcellentQuota)
        .filter(q -> q != null)
        .orElse(new BigDecimal("25"));

    int excellentAllowed = total > 0
        ? excellentQuotaPct.multiply(new BigDecimal(total))
            .divide(new BigDecimal("100"), 0, RoundingMode.FLOOR).intValue()
        : 0;

    // Good allowed: typically 35% of total
    int goodAllowed = total > 0
        ? new BigDecimal("35").multiply(new BigDecimal(total))
            .divide(new BigDecimal("100"), 0, RoundingMode.FLOOR).intValue()
        : 0;

    List<QuotaViolationDTO> violations = new ArrayList<>();

    if (excellentCount > excellentAllowed) {
      QuotaViolationDTO v = new QuotaViolationDTO();
      v.setType("QUOTA_EXCEEDED");
      v.setRating("EXCELLENT");
      v.setAllowed(excellentAllowed);
      v.setAssigned((int) excellentCount);
      v.setMessage("Excede a quota de Excelente em " + (excellentCount - excellentAllowed) + " colaborador(es).");
      violations.add(v);
    }

    boolean isCompliant = violations.isEmpty();

    QuotaUnitDTO unit = new QuotaUnitDTO();
    unit.setOrganicUnitId(null);
    unit.setOrganicUnitName("Global");
    unit.setTotalCollaborators((int) total);
    unit.setExcellentAllowed(excellentAllowed);
    unit.setExcellentAssigned((int) excellentCount);
    unit.setGoodAllowed(goodAllowed);
    unit.setGoodAssigned((int) goodCount);
    unit.setIsCompliant(isCompliant);
    unit.setViolations(violations);

    QuotaValidationResponseDTO response = new QuotaValidationResponseDTO();
    response.setYear(query.getYear());
    response.setIsValid(isCompliant);
    response.setUnits(List.of(unit));

    return ResponseEntity.ok(response);
  }
}
