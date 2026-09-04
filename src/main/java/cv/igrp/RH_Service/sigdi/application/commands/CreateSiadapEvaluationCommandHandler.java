package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.dto.CreateSiadapEvaluationRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.FuncionarioDTO;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapEvaluationDTO;
import cv.igrp.RH_Service.sigdi.application.port.FuncionarioLookupPort;
import cv.igrp.RH_Service.sigdi.application.port.OrganicaLookupPort;
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
  private final OrganicaLookupPort organicaLookupPort;
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

    String derivedEvaluatorId = deriveEvaluatorId(employeeId);

    // T-109-13 (Repudiation): a divergent evaluatorId sent by the client is ignored, never
    // trusted, but it is not swallowed in silence either -- a client still sending this field
    // is an operational fact someone will want to see (D-12). Read once, into a local, so the
    // request value is never re-read on a second path toward the aggregate.
    String requestEvaluatorId = req.getEvaluatorId();
    if (requestEvaluatorId != null && !requestEvaluatorId.isBlank()
        && !requestEvaluatorId.equals(derivedEvaluatorId)) {
      LOGGER.warn("CreateSiadapEvaluationCommand: request evaluatorId '{}' diverges from derived evaluatorId '{}' -- request value ignored",
          requestEvaluatorId, derivedEvaluatorId);
    }

    SiadapEvaluation evaluation = SiadapEvaluation.create(
        employeeId,
        year,
        organicUnitId,
        derivedEvaluatorId,
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

  // The evaluator is derived, never chosen: this is the only point where evaluatorId is
  // decided before entering the aggregate, and the five downstream commands marked
  // ACTOR-CHECK: ENFORCED (ContractualizeObjectives, EvaluateCompetencies, FinalizeEvaluation,
  // ProposeObjectiveRevision, RecordObjectiveAchievement) rely on the value written here.
  // Derivation reads an explicit pointer -- the unit's responsibleEmployeeId -- and never
  // infers from a function: the three seed employees share one unit and two of them hold
  // DIR_SERVICO, so deriving from the function would name two people (Phase 109, criterion 3).
  // A null result never blocks creation (D-05, operator decision 2026-08-24, revocable); the
  // request's evaluatorId is ignored, not rejected (D-12).
  //
  // There is a second evaluator derivation in the codebase,
  // cv.igrp.RH_Service.sigdi.application.service.EvaluatorResolver (Fase 119), used by the
  // automatic form generator on period opening. The difference is deliberate: this method
  // derives the evaluator from the employee's current organizational unit
  // (funcionarioLookupPort.findCurrentOrganizationalUnitId -- "where they are today"), while
  // EvaluatorResolver derives it from the unit the employee was assigned to during the
  // submission period's year -- "where they were that year" -- with a single climb to the
  // parent unit and a named skip for whoever leads the top unit. The T-109-13 decision above,
  // to ignore any evaluatorId sent by the client, is exactly what prevents the automatic
  // generator from reusing this manual command path: it cannot pass its derived evaluatorId
  // through here and have it honoured.
  private String deriveEvaluatorId(String employeeId) {
    Optional<UUID> unitId = funcionarioLookupPort.findCurrentOrganizationalUnitId(UUID.fromString(employeeId));
    if (unitId.isEmpty()) {
      LOGGER.debug("Evaluator derivation: employee {} has no current enquadramento -- evaluatorId stays null", employeeId);
      return null;
    }

    Optional<UUID> responsibleEmployeeId = organicaLookupPort.findResponsibleEmployeeId(unitId.get());
    if (responsibleEmployeeId.isEmpty()) {
      LOGGER.debug("Evaluator derivation: unit {} has no responsible employee -- evaluatorId stays null", unitId.get());
      return null;
    }

    return responsibleEmployeeId.get().toString();
  }
}
