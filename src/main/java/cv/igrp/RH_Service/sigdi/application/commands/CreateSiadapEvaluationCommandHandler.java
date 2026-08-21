package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.dto.CreateSiadapEvaluationRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.FuncionarioDTO;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapEvaluationDTO;
import cv.igrp.RH_Service.sigdi.application.port.FuncionarioLookupPort;
import cv.igrp.RH_Service.sigdi.domain.admin.models.SiadapConfig;
import cv.igrp.RH_Service.sigdi.domain.admin.repository.SiadapConfigRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapEvaluation;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapEvaluationRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.compliance.SiadapEvaluationMapper;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

// ACTOR-CHECK: NOT-REQUIRED -- administrative creation command dispatched before the aggregate exists; there is no employeeId or evaluatorId on file yet to compare the caller against, and no RBAC layer (T-010) exists to restrict it further
@Component
@RequiredArgsConstructor
public class CreateSiadapEvaluationCommandHandler
    implements CommandHandler<CreateSiadapEvaluationCommand, ResponseEntity<SiadapEvaluationDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateSiadapEvaluationCommandHandler.class);

  private final SiadapEvaluationRepository evaluationRepository;
  private final SiadapConfigRepository configRepository;
  private final FuncionarioLookupPort funcionarioLookupPort;
  private final SiadapEvaluationMapper mapper;

  @IgrpCommandHandler
  @Transactional
  @Override
  public ResponseEntity<SiadapEvaluationDTO> handle(CreateSiadapEvaluationCommand command) {
    LOGGER.debug("CreateSiadapEvaluationCommand: {}", command);

    CreateSiadapEvaluationRequestDTO req = command.getBody();
    String employeeId = req.getEmployeeId();
    Integer year = req.getYear();

    // Check if already exists
    Optional<SiadapEvaluation> existing = evaluationRepository.findByEmployeeAndYear(employeeId, year);
    if (existing.isPresent()) {
      throw IgrpResponseStatusException.badRequest("Já existe uma avaliação de desempenho registada para este colaborador no ano " + year);
    }

    // Lookup employee
    FuncionarioDTO employee = funcionarioLookupPort.findById(UUID.fromString(employeeId))
        .orElseThrow(() -> IgrpResponseStatusException.badRequest("Colaborador não encontrado"));

    // Fetch config for weights
    Optional<SiadapConfig> configOpt = configRepository.findByFiscalYear(year);
    BigDecimal resultsWeight = configOpt.map(SiadapConfig::getResultsWeight).orElse(new BigDecimal("60"));
    BigDecimal competenciesWeight = configOpt.map(SiadapConfig::getCompetenciesWeight).orElse(new BigDecimal("40"));

    // Override if provided in request
    if (req.getResultsWeight() != null) {
      resultsWeight = req.getResultsWeight();
    }
    if (req.getCompetenciesWeight() != null) {
      competenciesWeight = req.getCompetenciesWeight();
    }

    // Try to resolve organic unit (department) from request, otherwise default to null or lookup if available.
    String organicUnitId = req.getOrganicUnitId();

    String evaluatorId = req.getEvaluatorId();

    SiadapEvaluation evaluation = SiadapEvaluation.create(
        employeeId,
        year,
        organicUnitId,
        evaluatorId,
        resultsWeight,
        competenciesWeight
    );

    SiadapEvaluation saved = evaluationRepository.save(evaluation);

    SiadapEvaluationDTO dto = mapper.toFullDto(saved);
    dto.setEmployeeName(employee.getNomeCompleto());
    dto.setPhase(saved.getPhase().getCode());
    dto.setStatus(saved.isValidatedQuota() ? "CLOSED" : "DRAFT");

    return ResponseEntity.ok(dto);
  }
}
