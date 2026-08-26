package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.RH_Service.sigdi.application.config.SiadapCcaSecurityProperties;
import cv.igrp.RH_Service.sigdi.application.constants.EvaluationPhase;
import cv.igrp.RH_Service.sigdi.application.constants.SiadapMeritRating;
import cv.igrp.RH_Service.sigdi.application.dto.AssignMeritRatingRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapEvaluationDTO;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapEvaluation;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapEvaluationRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.SiadapEvaluationId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.compliance.SiadapEvaluationMapper;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * ACH-A-04 (SIA-01): assigns or corrects the qualitative merit rating (menção de mérito) of a
 * SIADAP evaluation. Until this handler existed, {@code SiadapEvaluation.assignMeritRating} had
 * no caller at all and the only way to fix a wrong mention on a finalized evaluation was a direct
 * UPDATE against the database.
 */
// ACTOR-CHECK: ENFORCED -- CCA membership list (SiadapCcaSecurityProperties); only a CCA member may correct a merit rating
@Component
@RequiredArgsConstructor
public class AssignMeritRatingCommandHandler
    implements CommandHandler<AssignMeritRatingCommand, ResponseEntity<SiadapEvaluationDTO>> {

  private final SiadapEvaluationRepository evaluationRepository;
  private final SiadapEvaluationMapper mapper;
  private final CurrentEmployeeResolver currentEmployeeResolver;
  private final SiadapCcaSecurityProperties ccaSecurityProperties;

  /** Codes accepted in the request body, listed back to the caller on an invalid value. */
  private static final String ALLOWED_RATING_CODES = Arrays.stream(SiadapMeritRating.values())
      .map(SiadapMeritRating::getCode)
      .collect(Collectors.joining(", "));

  @IgrpCommandHandler
  @Transactional
  @Override
  public ResponseEntity<SiadapEvaluationDTO> handle(AssignMeritRatingCommand command) {
    AssignMeritRatingRequestDTO req = command.getBody();
    SiadapEvaluationId evalId = SiadapEvaluationId.from(command.getEvaluationId());

    // Value validation before the lookup: an unparseable mention is a client error regardless of
    // whether the evaluation exists. Deliberately not SiadapMeritRating.fromCodeOrThrow(): its
    // message does not enumerate the accepted codes, and this screen needs them. The comment used
    // to say that message was in English too -- Phase 106 translated it, so only the enumeration
    // argument survives, and it survives on its own.
    String rawRating = req != null ? req.getMeritRating() : null;
    SiadapMeritRating rating = SiadapMeritRating.fromCode(rawRating)
        .orElseThrow(() -> IgrpResponseStatusException.badRequest(
            "Menção de mérito inválida: " + rawRating + ". Valores permitidos: " + ALLOWED_RATING_CODES));

    SiadapEvaluation evaluation = evaluationRepository.findById(evalId)
        .orElseThrow(() -> IgrpResponseStatusException.notFound("Avaliação não encontrada"));

    // SIA-04: only a CCA (Conselho Coordenador da Avaliação) member may correct a merit
    // rating. Authorization is checked before any business rule below, matching the
    // auth-before-business-rule ordering used by sibling handlers.
    String currentEmployeeId = currentEmployeeResolver.resolve().getStringValor();
    if (!ccaSecurityProperties.isCca(currentEmployeeId))
      throw IgrpResponseStatusException.of(HttpStatus.FORBIDDEN,
          "Apenas um membro do Conselho Coordenador da Avaliação (CCA) pode executar esta ação");

    // ------------------------------------------------------------------------------------
    // Business rule — which phases may have their merit rating written (ACH-A-04).
    //
    // The domain method assignMeritRating() carries NO phase guard of its own: it rebuilds the
    // aggregate with the new rating in whatever phase it happens to be, CLOSED included. The
    // requirement is precisely to correct an already finalized evaluation, so nothing here
    // closes that door: HARMONIZATION (post finalizeEvaluation) and CLOSED (post
    // markQuotaValidated) both pass through.
    //
    // What is refused is assignment BEFORE the evaluation has been finalized. In OPEN,
    // IN_PROGRESS, SELF_EVALUATION and MANAGER_EVALUATION there is no finalScore yet, so
    // writing a mention would (a) persist a qualitative mention with no score behind it and
    // (b) be silently discarded by finalizeEvaluation(), which recomputes the mention from the
    // final score via deriveMeritRating(). That is not a correction — it is a back door into
    // the scoring. The rule is enforced here, in the handler, not only by hiding a UI action.
    // ------------------------------------------------------------------------------------
    EvaluationPhase phase = evaluation.getPhase();
    if (!EvaluationPhase.HARMONIZATION.equals(phase) && !EvaluationPhase.CLOSED.equals(phase))
      throw IgrpResponseStatusException.badRequest(
          "A menção de mérito só pode ser atribuída ou corrigida depois de a avaliação ser finalizada "
              + "(fases Harmonização ou Encerrado). Fase atual: " + phase.getDescription());

    SiadapEvaluation updated = evaluation.assignMeritRating(rating);
    SiadapEvaluation saved = evaluationRepository.save(updated);

    SiadapEvaluationDTO dto = mapper.toFullDto(saved);
    dto.setPhase(saved.getPhase().getCode());
    return ResponseEntity.ok(dto);
  }
}
