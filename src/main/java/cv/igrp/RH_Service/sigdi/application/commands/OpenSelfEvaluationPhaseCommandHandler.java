package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.RH_Service.sigdi.application.config.SiadapSelfEvaluationOpenerSecurityProperties;
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
import org.springframework.http.HttpStatus;
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
 */
// ACTOR-CHECK: ENFORCED -- SiadapSelfEvaluationOpenerSecurityProperties configured list; only a configured opener may open the self-evaluation phase by hand
@Component
@RequiredArgsConstructor
public class OpenSelfEvaluationPhaseCommandHandler
    implements CommandHandler<OpenSelfEvaluationPhaseCommand, ResponseEntity<SiadapEvaluationDTO>> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(OpenSelfEvaluationPhaseCommandHandler.class);

  private final SiadapEvaluationRepository evaluationRepository;
  private final SiadapEvaluationMapper mapper;
  private final CurrentEmployeeResolver currentEmployeeResolver;
  private final SiadapSelfEvaluationOpenerSecurityProperties openerProperties;
  private final SelfEvaluationWindowPolicy windowPolicy;

  @IgrpCommandHandler
  @Transactional
  @Override
  public ResponseEntity<SiadapEvaluationDTO> handle(OpenSelfEvaluationPhaseCommand command) {
    SiadapEvaluationId evalId = SiadapEvaluationId.from(command.getEvaluationId());

    SiadapEvaluation evaluation = evaluationRepository.findById(evalId)
        .orElseThrow(() -> IgrpResponseStatusException.notFound("Avaliação não encontrada"));

    // T-111-01: actor check first. The 403 message deliberately does not name the
    // configuration property or environment variable -- that would disclose configuration
    // detail to the client. The operator who needs to know it reads the WARN this same
    // refusal logs in SiadapSelfEvaluationOpenerSecurityProperties, exactly like
    // SiadapCcaSecurityProperties already does.
    String currentEmployeeId = currentEmployeeResolver.resolve().getStringValor();
    if (!openerProperties.isSelfEvaluationOpener(currentEmployeeId))
      throw IgrpResponseStatusException.of(HttpStatus.FORBIDDEN,
          "Apenas os funcionários autorizados podem abrir a autoavaliação à mão. Contacte o RH.");

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
