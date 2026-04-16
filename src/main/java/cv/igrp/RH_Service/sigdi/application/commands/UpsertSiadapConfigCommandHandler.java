package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.SiadapConfigEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.SiadapConfigEntityRepository;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapConfigRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapConfigResponseDTO;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class UpsertSiadapConfigCommandHandler
    implements CommandHandler<UpsertSiadapConfigCommand, ResponseEntity<SiadapConfigResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(UpsertSiadapConfigCommandHandler.class);

  private final SiadapConfigEntityRepository configRepository;

  public UpsertSiadapConfigCommandHandler(SiadapConfigEntityRepository configRepository) {
    this.configRepository = configRepository;
  }

  @IgrpCommandHandler
  @Transactional
  public ResponseEntity<SiadapConfigResponseDTO> handle(UpsertSiadapConfigCommand command) {
    LOGGER.debug("UpsertSiadapConfigCommand: {}", command);

    Integer year = command.getYear();
    SiadapConfigRequestDTO req = command.getBody();

    SiadapConfigEntity entity = configRepository.findByFiscalYear(year)
        .orElseGet(() -> {
          SiadapConfigEntity e = new SiadapConfigEntity();
          e.setId(UUID.randomUUID());
          e.setFiscalYear(year);
          return e;
        });

    entity.setGoodScore(req.getGoodScore());
    entity.setExcellentScore(req.getExcellentScore());
    entity.setExcellentQuota(req.getExcellentQuota());
    entity.setMinCollaboratorsForQuota(req.getMinimumCollaboratorsForQuota());
    entity.setResultsWeight(req.getResultsWeight());
    entity.setCompetenciesWeight(req.getCompetenciesWeight());

    SiadapConfigEntity saved = configRepository.save(entity);

    SiadapConfigResponseDTO response = new SiadapConfigResponseDTO();
    response.setYear(saved.getFiscalYear());
    response.setGoodScore(saved.getGoodScore());
    response.setExcellentScore(saved.getExcellentScore());
    response.setExcellentQuota(saved.getExcellentQuota());
    response.setGoodQuota(null);
    response.setMinimumCollaboratorsForQuota(saved.getMinCollaboratorsForQuota());
    response.setResultsWeight(saved.getResultsWeight());
    response.setCompetenciesWeight(saved.getCompetenciesWeight());
    response.setSource("MANUAL");
    response.setUpdatedAt(saved.getLastModifiedDate() != null ? saved.getLastModifiedDate().toString() : null);

    return ResponseEntity.ok(response);
  }
}
