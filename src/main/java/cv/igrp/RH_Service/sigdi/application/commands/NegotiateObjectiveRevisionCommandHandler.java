package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapInterimFeedbackDTO;
import cv.igrp.RH_Service.sigdi.application.service.SiadapObjectivesWindowPolicy;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapEvaluation;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapInterimFeedback;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapEvaluationRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapInterimFeedbackRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.SiadapEvaluationId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.compliance.SiadapInterimFeedbackMapper;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

// ACTOR-CHECK: ENFORCED -- evaluation.employeeId; only the avaliado may request negotiation of an objective revision
@Component
@RequiredArgsConstructor
public class NegotiateObjectiveRevisionCommandHandler
    implements CommandHandler<NegotiateObjectiveRevisionCommand, ResponseEntity<SiadapInterimFeedbackDTO>> {

  private final SiadapInterimFeedbackRepository feedbackRepository;
  private final SiadapEvaluationRepository evaluationRepository;
  private final SiadapObjectivesWindowPolicy windowPolicy;
  private final SiadapInterimFeedbackMapper mapper;
  private final CurrentEmployeeResolver currentEmployeeResolver;

  @IgrpCommandHandler
  @Transactional
  @Override
  public ResponseEntity<SiadapInterimFeedbackDTO> handle(NegotiateObjectiveRevisionCommand command) {
    SiadapEvaluationId evalId = SiadapEvaluationId.from(command.getEvaluationId());

    SiadapEvaluation evaluation = evaluationRepository.findById(evalId)
        .orElseThrow(() -> IgrpResponseStatusException.notFound("Avaliação não encontrada"));

    // RECONC-02 / Pitfall 5: only the avaliado (evaluation.employeeId) may request negotiation.
    // RECONC-03's scope was widened by D-47 (2026-09-10, operator decision): the gate that used
    // to cover propose only now covers propose, negotiate and accept, because the act that fixes
    // the compromisso -- accept -- is the one a partial gate left unguarded (A-132-115).
    String currentEmployeeId = currentEmployeeResolver.resolve().getStringValor();
    if (!currentEmployeeId.equals(evaluation.getEmployeeId()))
      throw IgrpResponseStatusException.of(HttpStatus.FORBIDDEN,
          "Apenas o avaliado desta avaliação pode solicitar negociação da revisão de objetivo");

    // D-47 (2026-09-10): consults the same class ProposeObjectiveRevisionCommandHandler and
    // AcceptObjectiveRevisionCommandHandler consult, so the three steps of revision can no
    // longer disagree about whether the window is open (Plano 136-05).
    windowPolicy.requireRevisionOpenFor(evaluation.getYear());

    UUID evalUuid = UUID.fromString(command.getEvaluationId());
    SiadapInterimFeedback feedback = feedbackRepository.findByEvaluationId(evalUuid)
        .orElseThrow(() -> IgrpResponseStatusException.notFound("Feedback intercalar não encontrado"));

    String comment = command.getBody() != null ? command.getBody().getComment() : null;
    UUID revisionId = UUID.fromString(command.getRevisionId());
    SiadapInterimFeedback updated = feedback.negotiateRevision(revisionId, comment);
    SiadapInterimFeedback saved = feedbackRepository.save(updated);

    return ResponseEntity.ok(mapper.toDto(saved));
  }
}
