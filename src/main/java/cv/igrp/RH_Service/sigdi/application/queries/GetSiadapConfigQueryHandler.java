package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapConfigResponseDTO;
import cv.igrp.RH_Service.sigdi.domain.admin.models.SiadapConfig;
import cv.igrp.RH_Service.sigdi.domain.admin.repository.SiadapConfigRepository;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class GetSiadapConfigQueryHandler
    implements QueryHandler<GetSiadapConfigQuery, ResponseEntity<SiadapConfigResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GetSiadapConfigQueryHandler.class);

  private final SiadapConfigRepository configRepository;

  public GetSiadapConfigQueryHandler(SiadapConfigRepository configRepository) {
    this.configRepository = configRepository;
  }

  @IgrpQueryHandler
  @Transactional(readOnly = true)
  public ResponseEntity<SiadapConfigResponseDTO> handle(GetSiadapConfigQuery query) {
    LOGGER.debug("GetSiadapConfigQuery: {}", query);

    Integer year = query.getYear();

    // Try exact year first, then fall back to previous year
    Optional<SiadapConfig> configOpt = configRepository.findByFiscalYear(year);
    if (configOpt.isEmpty()) {
      configOpt = configRepository.findByFiscalYear(year - 1);
    }

    SiadapConfig config = configOpt.orElseThrow(() ->
        IgrpResponseStatusException.notFound(
            "No SIADAP configuration found for year " + year + " or " + (year - 1)));

    String source = config.getFiscalYear().equals(year)
        ? "CONFIGURED"
        : "FALLBACK_FROM_PREVIOUS_YEAR";

    SiadapConfigResponseDTO response = new SiadapConfigResponseDTO();
    response.setYear(config.getFiscalYear());
    response.setGoodScore(config.getGoodScore());
    response.setExcellentScore(config.getExcellentScore());
    response.setExcellentQuota(config.getExcellentQuota());
    response.setGoodQuota(null);
    response.setMinimumCollaboratorsForQuota(config.getMinCollaboratorsForQuota());
    response.setResultsWeight(config.getResultsWeight());
    response.setCompetenciesWeight(config.getCompetenciesWeight());
    response.setSource(source);
    response.setUpdatedAt(null);

    return ResponseEntity.ok(response);
  }
}
