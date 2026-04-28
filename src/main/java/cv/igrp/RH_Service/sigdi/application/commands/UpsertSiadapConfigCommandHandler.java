package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.sigdi.application.dto.SiadapConfigRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapConfigResponseDTO;
import cv.igrp.RH_Service.sigdi.domain.admin.models.SiadapConfig;
import cv.igrp.RH_Service.sigdi.domain.admin.repository.SiadapConfigRepository;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UpsertSiadapConfigCommandHandler
    implements CommandHandler<UpsertSiadapConfigCommand, ResponseEntity<SiadapConfigResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(UpsertSiadapConfigCommandHandler.class);

  private final SiadapConfigRepository configRepository;

  public UpsertSiadapConfigCommandHandler(SiadapConfigRepository configRepository) {
    this.configRepository = configRepository;
  }

  @IgrpCommandHandler
  @Transactional
  public ResponseEntity<SiadapConfigResponseDTO> handle(UpsertSiadapConfigCommand command) {
    LOGGER.debug("UpsertSiadapConfigCommand: {}", command);

    Integer year = command.getYear();
    SiadapConfigRequestDTO req = command.getBody();

    SiadapConfig config = configRepository.findByFiscalYear(year)
        .map(existing -> existing.update(
            req.getGoodScore(),
            req.getExcellentScore(),
            req.getExcellentQuota(),
            req.getGoodQuota(),
            req.getMinimumCollaboratorsForQuota(),
            req.getResultsWeight(),
            req.getCompetenciesWeight()))
        .orElseGet(() -> SiadapConfig.create(
            year,
            req.getGoodScore(),
            req.getExcellentScore(),
            req.getExcellentQuota(),
            req.getGoodQuota(),
            req.getMinimumCollaboratorsForQuota(),
            req.getResultsWeight(),
            req.getCompetenciesWeight()));

    SiadapConfig saved = configRepository.save(config);

    SiadapConfigResponseDTO response = new SiadapConfigResponseDTO();
    response.setYear(saved.getFiscalYear());
    response.setGoodScore(saved.getGoodScore());
    response.setExcellentScore(saved.getExcellentScore());
    response.setExcellentQuota(saved.getExcellentQuota());
    response.setGoodQuota(saved.getGoodQuota());
    response.setMinimumCollaboratorsForQuota(saved.getMinCollaboratorsForQuota());
    response.setResultsWeight(saved.getResultsWeight());
    response.setCompetenciesWeight(saved.getCompetenciesWeight());
    response.setSource("MANUAL");
    response.setUpdatedAt(null);

    return ResponseEntity.ok(response);
  }
}
