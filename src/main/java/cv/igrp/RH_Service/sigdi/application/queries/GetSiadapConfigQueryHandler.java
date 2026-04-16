package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.SiadapConfigEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.SiadapConfigEntityRepository;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapConfigResponseDTO;
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

  private final SiadapConfigEntityRepository configRepository;

  public GetSiadapConfigQueryHandler(SiadapConfigEntityRepository configRepository) {
    this.configRepository = configRepository;
  }

  @IgrpQueryHandler
  @Transactional(readOnly = true)
  public ResponseEntity<SiadapConfigResponseDTO> handle(GetSiadapConfigQuery query) {
    LOGGER.debug("GetSiadapConfigQuery: {}", query);

    Integer year = query.getYear();

    // Try exact year first, then fall back to previous year
    Optional<SiadapConfigEntity> entityOpt = configRepository.findByFiscalYear(year);
    if (entityOpt.isEmpty()) {
      entityOpt = configRepository.findByFiscalYear(year - 1);
    }

    SiadapConfigEntity entity = entityOpt.orElseThrow(() ->
        IgrpResponseStatusException.notFound(
            "No SIADAP configuration found for year " + year + " or " + (year - 1)));

    String source = entity.getFiscalYear().equals(year) ? "MANUAL" : "INHERITED_FROM_" + entity.getFiscalYear();

    SiadapConfigResponseDTO response = new SiadapConfigResponseDTO();
    response.setYear(entity.getFiscalYear());
    response.setGoodScore(entity.getGoodScore());
    response.setExcellentScore(entity.getExcellentScore());
    response.setExcellentQuota(entity.getExcellentQuota());
    response.setGoodQuota(null);
    response.setMinimumCollaboratorsForQuota(entity.getMinCollaboratorsForQuota());
    response.setResultsWeight(entity.getResultsWeight());
    response.setCompetenciesWeight(entity.getCompetenciesWeight());
    response.setSource(source);
    response.setUpdatedAt(entity.getLastModifiedDate() != null ? entity.getLastModifiedDate().toString() : null);

    return ResponseEntity.ok(response);
  }
}
