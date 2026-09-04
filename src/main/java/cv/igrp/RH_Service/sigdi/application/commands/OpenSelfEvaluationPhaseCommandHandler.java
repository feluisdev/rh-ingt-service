package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapEvaluationDTO;
import cv.igrp.RH_Service.sigdi.application.service.SelfEvaluationWindowPolicy;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapEvaluation;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapEvaluationRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.SiadapEvaluationId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.compliance.SiadapEvaluationMapper;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * The second caller of {@link SiadapEvaluation#openSelfEvaluationPhase()} -- until this class
 * existed, the daily {@code SelfEvaluationOpeningScheduler} was its only caller, which made
 * SIA-06 unsatisfiable by design in v24.0: there was no way to advance a cycle from IN_PROGRESS
 * to SELF_EVALUATION except waiting for the cron job.
 *
 * <p><b>Order of checks, and why it is this order.</b> Actor and window are checked before the
 * domain's own phase guard, matching the auth-before-business-rule ordering already used by
 * sibling handlers ({@link FinalizeEvaluationCommandHandler} does exactly this with
 * {@code SIADAP_FINAL}). Declared consequence: an evaluation already in SELF_EVALUATION whose
 * year has no active window receives the window's 400 message, not the phase's 400 message --
 * the phase check never runs. This is not a bug; it is the same ordering the 13 sibling handlers
 * already use, kept here for consistency.
 *
 * <p>Authorization is no longer checked here (Phase 115/AUT-04): see the {@code ACTOR-CHECK}
 * comment below for where the guard lives now. {@link CurrentEmployeeResolver} stays as a
 * constructor dependency regardless -- unlike the sibling handlers migrated earlier in this
 * phase, it still has a second job here, feeding the repudiation-mitigation log line below.
 * This is the one place in the migration where the resolver answers both questions at once:
 * it no longer says who *may* act (the token does, via the controller's permission check),
 * but it still says who *did* act.
 */
// ACTOR-CHECK: ENFORCED -- siadap.autoavaliacao.abrir permission, guarded by @PreAuthorize on ComplianceController#openSelfEvaluationPhase
@Component
@RequiredArgsConstructor
public class OpenSelfEvaluationPhaseCommandHandler
    implements CommandHandler<OpenSelfEvaluationPhaseCommand, ResponseEntity<SiadapEvaluationDTO>> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(OpenSelfEvaluationPhaseCommandHandler.class);

  private final SiadapEvaluationRepository evaluationRepository;
  private final SiadapEvaluationMapper mapper;
  private final CurrentEmployeeResolver currentEmployeeResolver;
  private final SelfEvaluationWindowPolicy windowPolicy;

  @IgrpCommandHandler
  @Transactional
  @Override
  public ResponseEntity<SiadapEvaluationDTO> handle(OpenSelfEvaluationPhaseCommand command) {
    SiadapEvaluationId evalId = SiadapEvaluationId.from(command.getEvaluationId());

    SiadapEvaluation evaluation = evaluationRepository.findById(evalId)
        .orElseThrow(() -> IgrpResponseStatusException.notFound("Avaliação não encontrada"));

    // T-111-01 -> Phase 115/AUT-04: the actor check that used to run here now runs at the
    // controller boundary (@PreAuthorize on ComplianceController#openSelfEvaluationPhase,
    // permission siadap.autoavaliacao.abrir -- see the ACTOR-CHECK comment on this class).
    // currentEmployeeResolver.resolve() stays: it no longer feeds an authorization decision,
    // it feeds the repudiation-mitigation log line below, which needs to know who acted
    // regardless of how that actor was authorized.
    String currentEmployeeId = currentEmployeeResolver.resolve().getStringValor();

    // D-02: window check second, delegated entirely to SelfEvaluationWindowPolicy -- this
    // handler never re-queries the submission-period repository directly (the 111-01 grep
    // gate for "single owner of the window question" fails if it does).
    windowPolicy.requireOpenFor(evaluation.getYear());

    // The domain's own guard (phase must be IN_PROGRESS) runs last and is not duplicated here.
    SiadapEvaluation updated = evaluation.openSelfEvaluationPhase();
    SiadapEvaluation saved = evaluationRepository.save(updated);

    // T-111-05: minimal repudiation mitigation -- who opened this, and when. Not a substitute
    // for real audit: it is in-process log only, does not persist to the database, and does
    // not survive log rotation.
    LOGGER.info(
        "Self-evaluation phase opened manually for evaluation {} by employee {}.",
        saved.getId().getStringValor(),
        currentEmployeeId);

    SiadapEvaluationDTO dto = mapper.toFullDto(saved);
    dto.setPhase(saved.getPhase().getCode());
    return ResponseEntity.ok(dto);
  }
}
