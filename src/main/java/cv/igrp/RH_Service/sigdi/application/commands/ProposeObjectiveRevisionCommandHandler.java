package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapInterimFeedbackDTO;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapEvaluation;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapInterimFeedback;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapEvaluationRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapInterimFeedbackRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.SiadapEvaluationId;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PaaSubmissionPeriodRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.compliance.SiadapInterimFeedbackMapper;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProposeObjectiveRevisionCommandHandler
    implements CommandHandler<ProposeObjectiveRevisionCommand, ResponseEntity<SiadapInterimFeedbackDTO>> {

  private final SiadapInterimFeedbackRepository feedbackRepository;
  private final SiadapEvaluationRepository evaluationRepository;
  private final PaaSubmissionPeriodRepository periodRepository;
  private final SiadapInterimFeedbackMapper mapper;
  private final CurrentEmployeeResolver currentEmployeeResolver;

  @IgrpCommandHandler
  @Transactional
  @Override
  public ResponseEntity<SiadapInterimFeedbackDTO> handle(ProposeObjectiveRevisionCommand command) {
    SiadapEvaluationId evalId = SiadapEvaluationId.from(command.getEvaluationId());

    SiadapEvaluation evaluation = evaluationRepository.findById(evalId)
        .orElseThrow(() -> IgrpResponseStatusException.notFound("Avaliação não encontrada"));

    // Pitfall 5 / WR-01 pattern: only the avaliador desta avaliação pode propor revisões.
    String currentEmployeeId = currentEmployeeResolver.resolve().getStringValor();
    if (!currentEmployeeId.equals(evaluation.getEvaluatorId()))
      throw IgrpResponseStatusException.of(HttpStatus.FORBIDDEN,
          "Apenas o avaliador desta avaliação pode propor revisões de objetivos");

    // RECONC-03: fail-closed deadline enforcement — verbatim ContractualizeObjectivesCommandHandler pattern.
    periodRepository.findActiveByTypeAndYearAndPurpose(
            PaaLevel.INDIVIDUAL_LEVEL, evaluation.getYear(), Purpose.SIADAP)
        .orElseThrow(() -> IgrpResponseStatusException.badRequest("Prazo não configurado para este ano"));

    UUID evalUuid = UUID.fromString(command.getEvaluationId());
    SiadapInterimFeedback feedback = feedbackRepository.findByEvaluationId(evalUuid)
        .orElseThrow(() -> IgrpResponseStatusException.notFound("Feedback intercalar não encontrado"));

    UUID revisionId = UUID.fromString(command.getRevisionId());
    SiadapInterimFeedback updated = feedback.proposeRevision(revisionId);
    SiadapInterimFeedback saved = feedbackRepository.save(updated);

    return ResponseEntity.ok(mapper.toDto(saved));
  }
}
