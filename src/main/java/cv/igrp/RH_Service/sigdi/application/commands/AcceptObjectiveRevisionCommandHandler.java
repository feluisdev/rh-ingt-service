package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapInterimFeedbackDTO;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapEvaluation;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapInterimFeedback;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapEvaluationRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapInterimFeedbackRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.ObjectiveRevision;
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

/**
 * Primeiro handler deste código-base a carregar, mutar e gravar DOIS agregados
 * independentes ({@link SiadapInterimFeedback} e {@link SiadapEvaluation}) dentro de
 * uma única fronteira {@code @Transactional} — cumpre a metade "aceitar" da RECONC-02
 * e a promoção do objetivo revisto da RECONC-04.
 * <p>
 * Ordem de execução (deliberada, não "gravar à medida que se avança"): valida
 * identidade/guardas de fase, lê o {@code target} (objectiveCode/newObjectiveSmart) do
 * feedback ANTES da mutação, constrói AMBOS os novos agregados em variáveis locais, e só
 * depois grava ambos os repositórios — uma falha a meio nunca deixa um agregado mutado e o
 * outro por mutar.
 */
// ACTOR-CHECK: ENFORCED -- evaluation.employeeId; only the avaliado may accept an objective revision
@Component
@RequiredArgsConstructor
public class AcceptObjectiveRevisionCommandHandler
    implements CommandHandler<AcceptObjectiveRevisionCommand, ResponseEntity<SiadapInterimFeedbackDTO>> {

  private final SiadapInterimFeedbackRepository feedbackRepository;
  private final SiadapEvaluationRepository evaluationRepository;
  private final SiadapInterimFeedbackMapper mapper;
  private final CurrentEmployeeResolver currentEmployeeResolver;

  @IgrpCommandHandler
  @Transactional
  @Override
  public ResponseEntity<SiadapInterimFeedbackDTO> handle(AcceptObjectiveRevisionCommand command) {
    SiadapEvaluationId evalId = SiadapEvaluationId.from(command.getEvaluationId());

    SiadapEvaluation evaluation = evaluationRepository.findById(evalId)
        .orElseThrow(() -> IgrpResponseStatusException.notFound("Avaliação não encontrada"));

    // RECONC-02 / Pitfall 5: only the avaliado (evaluation.employeeId) may accept a revision.
    String currentEmployeeId = currentEmployeeResolver.resolve().getStringValor();
    if (!currentEmployeeId.equals(evaluation.getEmployeeId()))
      throw IgrpResponseStatusException.of(HttpStatus.FORBIDDEN,
          "Apenas o avaliado desta avaliação pode aceitar revisões de objetivos");

    UUID evalUuid = UUID.fromString(command.getEvaluationId());
    SiadapInterimFeedback feedback = feedbackRepository.findByEvaluationId(evalUuid)
        .orElseThrow(() -> IgrpResponseStatusException.notFound("Feedback intercalar não encontrado"));
    UUID revisionId = UUID.fromString(command.getRevisionId());

    // Pitfall 3: read the target BEFORE acceptRevision() mutates the feedback — acceptRevision
    // only ever changes approvalStatus, so reading pre-mutation is both safe and clearer.
    ObjectiveRevision target = feedback.getObjectiveRevisions().stream()
        .filter(r -> revisionId.equals(r.getId()))
        .findFirst()
        .orElseThrow(() -> IgrpResponseStatusException.badRequest("Revisão de objetivo não encontrada: " + revisionId));

    // Architecture Pattern 2: validate AND construct BOTH new aggregates BEFORE saving either.
    // If applyObjectiveRevision's phase guard throws (Pitfall 7), it does so HERE, before any
    // save() call, so there is no partial-write window to reason about.
    SiadapInterimFeedback acceptedFeedback = feedback.acceptRevision(revisionId);
    SiadapEvaluation revisedEvaluation = evaluation.applyObjectiveRevision(target.getObjectiveCode(), target.getNewObjectiveSmart());

    SiadapInterimFeedback savedFeedback = feedbackRepository.save(acceptedFeedback);
    evaluationRepository.save(revisedEvaluation);

    return ResponseEntity.ok(mapper.toDto(savedFeedback));
  }
}
