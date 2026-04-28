package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.KeyResultMetricUnit;
import cv.igrp.RH_Service.sigdi.application.dto.CreateOkrDTO;
import cv.igrp.RH_Service.sigdi.application.dto.OkrKeyResultResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.OkrResponseDTO;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.InstitutionalIdentityRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.StrategicGoalRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.Okr;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.OkrRepository;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CreateOkrCommandHandler
    implements CommandHandler<CreateOkrCommand, ResponseEntity<OkrResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateOkrCommandHandler.class);

  private final InstitutionalIdentityRepository identityRepository;
  private final StrategicGoalRepository goalRepository;
  private final OkrRepository okrRepository;

  public CreateOkrCommandHandler(InstitutionalIdentityRepository identityRepository,
      StrategicGoalRepository goalRepository,
      OkrRepository okrRepository) {
    this.identityRepository = identityRepository;
    this.goalRepository = goalRepository;
    this.okrRepository = okrRepository;
  }

  @IgrpCommandHandler
  public ResponseEntity<OkrResponseDTO> handle(CreateOkrCommand command) {
    LOGGER.debug("CreateOkrCommand: {}", command);

    CreateOkrDTO request = command.getData();

    var activeIdentity = identityRepository.findActive()
        .orElseThrow(() -> IgrpResponseStatusException.notFound("Identidade Institucional ativa não encontrada"));

    StrategicGoalId strategicGoalId = StrategicGoalId.from(request.getStrategicGoalId());
    goalRepository.findById(strategicGoalId)
        .orElseThrow(() -> IgrpResponseStatusException.badRequest("strategicGoalId inválido"));

    List<Okr.KeyResultData> krData = request.getKeyResults().stream()
        .map(dto -> new Okr.KeyResultData(
            dto.getTitle(),
            dto.getTargetValue(),
            KeyResultMetricUnit.fromCodeOrThrow(dto.getUnit()),
            dto.getWeight()))
        .toList();

    Okr okr = Okr.create(
        activeIdentity.getId(),
        strategicGoalId,
        request.getTitle(),
        request.getCycle(),
        krData);

    Okr saved = okrRepository.save(okr);

    OkrResponseDTO response = new OkrResponseDTO();
    response.setId(saved.getId().getStringValor());
    response.setInstitutionId(saved.getInstitutionId() != null
        ? saved.getInstitutionId().getStringValor()
        : null);
    response.setStrategicGoalId(saved.getStrategicGoalId() != null
        ? saved.getStrategicGoalId().getStringValor()
        : null);
    response.setTitle(saved.getTitle());
    response.setCycle(saved.getCycle());
    response.setStatus(saved.getStatus());
    response.setProgress(saved.getProgressPercentage());
    response.setCreatedAt(null);

    List<OkrKeyResultResponseDTO> krResponses = saved.getKeyResults().stream()
        .map(kr -> {
          OkrKeyResultResponseDTO krDto = new OkrKeyResultResponseDTO();
          krDto.setId(kr.getId().getStringValor());
          krDto.setTitle(kr.getTitle());
          krDto.setTargetValue(kr.getTargetValue());
          krDto.setCurrentValue(kr.getCurrentValue());
          krDto.setProgress(kr.getProgressPercentage());
          krDto.setUnit(kr.getUnit().getCode());
          krDto.setWeight(kr.getWeight());
          krDto.setRiskLevel("NONE");
          return krDto;
        })
        .toList();

    response.setKeyResults(krResponses);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}