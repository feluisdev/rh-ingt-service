package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.application.dto.FinalizeEvaluationRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapEvaluationDTO;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapEvaluation;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapEvaluationRepository;
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

@Component
@RequiredArgsConstructor
public class FinalizeEvaluationCommandHandler
    implements CommandHandler<FinalizeEvaluationCommand, ResponseEntity<SiadapEvaluationDTO>> {

  private final SiadapEvaluationRepository evaluationRepository;
  private final SiadapEvaluationMapper mapper;
  private final PaaSubmissionPeriodRepository periodRepository;
  private final CurrentEmployeeResolver currentEmployeeResolver;

  @IgrpCommandHandler
  @Transactional
  @Override
  public ResponseEntity<SiadapEvaluationDTO> handle(FinalizeEvaluationCommand command) {
    FinalizeEvaluationRequestDTO req = command.getBody();
    SiadapEvaluationId evalId = SiadapEvaluationId.from(req.getEvaluationId());

    SiadapEvaluation evaluation = evaluationRepository.findById(evalId)
        .orElseThrow(() -> IgrpResponseStatusException.notFound("Avaliação não encontrada"));

    // WR-01: only the avaliador desta avaliação pode finalizar — previously any authenticated
    // caller who knew an evaluationId could finalize any employee's SIADAP evaluation.
    String currentEmployeeId = currentEmployeeResolver.resolve().getStringValor();
    if (!currentEmployeeId.equals(evaluation.getEvaluatorId()))
      throw IgrpResponseStatusException.of(HttpStatus.FORBIDDEN,
          "Apenas o avaliador desta avaliação pode finalizar a avaliação");

    // PRAZO-03: fail-closed deadline enforcement — no active SIADAP_FINAL individual period
    // for the evaluation's fiscal year blocks finalization. Inserted AFTER the actor check
    // above, matching the auth-before-business-rule ordering used by sibling handlers.
    periodRepository.findActiveByTypeAndYearAndPurpose(
                    PaaLevel.INDIVIDUAL_LEVEL, evaluation.getYear(), Purpose.SIADAP_FINAL)
            .orElseThrow(() -> IgrpResponseStatusException.badRequest(
                    "Prazo não configurado para a finalização de avaliações SIADAP"));

    SiadapEvaluation updated = evaluation.finalizeEvaluation();
    SiadapEvaluation saved = evaluationRepository.save(updated);

    SiadapEvaluationDTO dto = mapper.toFullDto(saved);
    dto.setPhase(saved.getPhase().getCode());
    return ResponseEntity.ok(dto);
  }
}
