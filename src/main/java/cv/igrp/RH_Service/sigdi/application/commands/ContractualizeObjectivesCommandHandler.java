package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.application.dto.ContractualizeObjectivesRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapEvaluationDTO;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapEvaluation;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapEvaluationRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.IndividualObjective;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.SiadapEvaluationId;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PaaSubmissionPeriodRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.compliance.SiadapEvaluationMapper;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

// ACTOR-CHECK: ENFORCED -- evaluation.evaluatorId; only the avaliador may contractualize objectives
@Component
@RequiredArgsConstructor
public class ContractualizeObjectivesCommandHandler
    implements CommandHandler<ContractualizeObjectivesCommand, ResponseEntity<SiadapEvaluationDTO>> {

  private final SiadapEvaluationRepository evaluationRepository;
  private final SiadapEvaluationMapper mapper;
  private final PaaSubmissionPeriodRepository periodRepository;
  private final CurrentEmployeeResolver currentEmployeeResolver;

  @IgrpCommandHandler
  @Transactional
  @Override
  public ResponseEntity<SiadapEvaluationDTO> handle(ContractualizeObjectivesCommand command) {
    ContractualizeObjectivesRequestDTO req = command.getBody();
    SiadapEvaluationId evalId = SiadapEvaluationId.from(req.getEvaluationId());

    SiadapEvaluation evaluation = evaluationRepository.findById(evalId)
        .orElseThrow(() -> IgrpResponseStatusException.notFound("Avaliação não encontrada"));

    // WR-01: only the avaliador desta avaliação pode contratualizar os objetivos.
    String currentEmployeeId = currentEmployeeResolver.resolve().getStringValor();
    if (!currentEmployeeId.equals(evaluation.getEvaluatorId()))
      throw IgrpResponseStatusException.of(HttpStatus.FORBIDDEN,
          "Apenas o avaliador desta avaliação pode contratualizar os objetivos");

    // PRAZO-03: fail-closed deadline enforcement — no active SIADAP individual period
    // for the evaluation's fiscal year blocks contractualization (59-RESEARCH.md Pitfall 4).
    // Inserted AFTER the actor check above, matching the auth-before-business-rule
    // ordering used by sibling handlers (FinalizeEvaluationCommandHandler.java:49-51).
    periodRepository.findActiveByTypeAndYearAndPurpose(
            PaaLevel.INDIVIDUAL_LEVEL, evaluation.getYear(), Purpose.SIADAP)
        .orElseThrow(() -> IgrpResponseStatusException.badRequest("Prazo não configurado para este ano"));

    List<IndividualObjective> objectives = req.getObjectives().stream()
        .map(dto -> IndividualObjective.create(
            dto.getCode(),
            dto.getDescription(),
            dto.getIndicator(),
            dto.getTargetValue(),
            dto.getWeight()
        ))
        .collect(Collectors.toList());

    SiadapEvaluation updated = evaluation.contractualizeObjectives(objectives);
    SiadapEvaluation saved = evaluationRepository.save(updated);

    SiadapEvaluationDTO dto = mapper.toFullDto(saved);
    dto.setPhase(saved.getPhase().getCode());
    return ResponseEntity.ok(dto);
  }
}
