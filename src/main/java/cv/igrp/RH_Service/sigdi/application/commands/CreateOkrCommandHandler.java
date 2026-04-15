package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.KeyResultsEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.OkrEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.KeyResultsEntityRepository;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.OkrEntityRepository;
import cv.igrp.RH_Service.sigdi.application.dto.CreateOkrDTO;
import cv.igrp.RH_Service.sigdi.application.dto.OkrKeyResultRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.OkrKeyResultResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.OkrResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class CreateOkrCommandHandler
    implements CommandHandler<CreateOkrCommand, ResponseEntity<OkrResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateOkrCommandHandler.class);

  private final OkrEntityRepository okrRepository;
  private final KeyResultsEntityRepository keyResultsRepository;

  public CreateOkrCommandHandler(OkrEntityRepository okrRepository,
                                  KeyResultsEntityRepository keyResultsRepository) {
    this.okrRepository = okrRepository;
    this.keyResultsRepository = keyResultsRepository;
  }

  @IgrpCommandHandler
  @Transactional
  public ResponseEntity<OkrResponseDTO> handle(CreateOkrCommand command) {
    LOGGER.debug("CreateOkrCommand: {}", command);

    CreateOkrDTO dto = command.getData();

    OkrEntity okr = new OkrEntity();
    okr.setId(UUID.randomUUID());
    okr.setStrategicGoalId(UUID.fromString(dto.getStrategicGoalId()));
    okr.setTitle(dto.getTitle());
    okr.setCycle(dto.getCycle());
    okr.setStatus("ACTIVE");
    okrRepository.save(okr);

    List<KeyResultsEntity> savedKrs = new ArrayList<>();
    for (OkrKeyResultRequestDTO krDto : dto.getKeyResults()) {
      KeyResultsEntity kr = new KeyResultsEntity();
      kr.setId(UUID.randomUUID());
      kr.setTitle(krDto.getTitle());
      kr.setTargetValue(krDto.getTargetValue());
      kr.setCurrentValue(BigDecimal.ZERO);
      kr.setMetricUnit(krDto.getUnit());
      kr.setWeight(krDto.getWeight());
      kr.setOkrId(okr);
      savedKrs.add(keyResultsRepository.save(kr));
    }

    OkrResponseDTO response = toResponseDTO(okr, savedKrs);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  private OkrResponseDTO toResponseDTO(OkrEntity okr, List<KeyResultsEntity> krs) {
    OkrResponseDTO dto = new OkrResponseDTO();
    dto.setId(okr.getId().toString());
    dto.setInstitutionId(okr.getInstitutionId() != null ? okr.getInstitutionId().toString() : null);
    dto.setStrategicGoalId(okr.getStrategicGoalId() != null ? okr.getStrategicGoalId().toString() : null);
    dto.setTitle(okr.getTitle());
    dto.setCycle(okr.getCycle());
    dto.setStatus(okr.getStatus());
    dto.setProgress(BigDecimal.ZERO);
    dto.setCreatedAt(okr.getCreatedDate() != null ? okr.getCreatedDate().toString() : null);

    List<OkrKeyResultResponseDTO> krResponses = krs.stream().map(kr -> {
      OkrKeyResultResponseDTO krDto = new OkrKeyResultResponseDTO();
      krDto.setId(kr.getId().toString());
      krDto.setTitle(kr.getTitle());
      krDto.setTargetValue(kr.getTargetValue());
      krDto.setCurrentValue(kr.getCurrentValue());
      krDto.setProgress(computeProgress(kr.getCurrentValue(), kr.getTargetValue()));
      krDto.setUnit(kr.getMetricUnit());
      krDto.setWeight(kr.getWeight());
      krDto.setRiskLevel("NONE");
      return krDto;
    }).toList();

    dto.setKeyResults(krResponses);
    return dto;
  }

  private BigDecimal computeProgress(BigDecimal current, BigDecimal target) {
    if (target == null || target.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
    if (current == null) return BigDecimal.ZERO;
    return current.divide(target, 4, RoundingMode.HALF_UP)
        .multiply(BigDecimal.valueOf(100))
        .setScale(1, RoundingMode.HALF_UP);
  }
}