package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.application.dto.ObjectiveRevisionDTO;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapInterimFeedbackDTO;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapEvaluation;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapInterimFeedback;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapEvaluationRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapInterimFeedbackRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.ObjectiveRevision;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

// ACTOR-CHECK: ENFORCED -- evaluatorId OR employeeId; shared form, either side of the evaluation may save the interim feedback
@Component
@RequiredArgsConstructor
public class SaveSiadapInterimFeedbackCommandHandler
    implements CommandHandler<SaveSiadapInterimFeedbackCommand, ResponseEntity<SiadapInterimFeedbackDTO>> {

    private final SiadapInterimFeedbackRepository repository;
    private final SiadapEvaluationRepository evaluationRepository;
    private final SiadapInterimFeedbackMapper mapper;
    private final CurrentEmployeeResolver currentEmployeeResolver;
    private final PaaSubmissionPeriodRepository periodRepository;

    @IgrpCommandHandler
    @Transactional
    @Override
    public ResponseEntity<SiadapInterimFeedbackDTO> handle(SaveSiadapInterimFeedbackCommand command) {
        UUID evalUuid = UUID.fromString(command.getEvaluationId());
        SiadapEvaluationId evalId = SiadapEvaluationId.from(evalUuid);

        // Ensure evaluation exists
        SiadapEvaluation evaluation = evaluationRepository.findById(evalId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Avaliação de desempenho não encontrada"));

        // WR-02: unlike the 3 dedicated propose/accept/negotiate endpoints (each restricted to a
        // single actor), both sides of the evaluation legitimately save the free-text sections of
        // this generic form — restrict to the evaluator OR the employee, reject anyone else.
        String currentEmployeeId = currentEmployeeResolver.resolve().getStringValor();
        if (!currentEmployeeId.equals(evaluation.getEvaluatorId()) && !currentEmployeeId.equals(evaluation.getEmployeeId()))
            throw IgrpResponseStatusException.of(HttpStatus.FORBIDDEN,
                    "Apenas o avaliador ou o avaliado desta avaliação podem guardar o feedback intercalar");

        // PRAZO-03: fail-closed deadline enforcement — no active SIADAP_INTERIM individual period
        // for the evaluation's fiscal year blocks saving the Interim Feedback sheet. Inserted
        // AFTER the actor check above, never replacing it.
        periodRepository.findActiveByTypeAndYearAndPurpose(
                        PaaLevel.INDIVIDUAL_LEVEL, evaluation.getYear(), Purpose.SIADAP_INTERIM)
                .orElseThrow(() -> IgrpResponseStatusException.badRequest(
                        "Prazo não configurado para a gravação do feedback intercalar SIADAP"));

        SiadapInterimFeedbackDTO body = command.getBody();
        body.setEvaluationId(command.getEvaluationId());

        Optional<SiadapInterimFeedback> existing = repository.findByEvaluationId(evalUuid);
        SiadapInterimFeedback domainModel = mapper.toDomain(body);
        List<ObjectiveRevisionDTO> incomingDtos = body.getObjectiveRevisions() != null
                ? body.getObjectiveRevisions()
                : List.of();
        SiadapInterimFeedback reconciled = reconcileRevisions(incomingDtos, domainModel, existing.orElse(null));

        SiadapInterimFeedback saved = repository.save(reconciled);

        return ResponseEntity.ok(mapper.toDto(saved));
    }

    /**
     * CR-01/CR-02/Issue-1-regression/Issue-3: the generic save endpoint must never let a client
     * drive {@code approvalStatus}/{@code lastNegotiationComment} transitions directly — those
     * only ever happen through {@code proposeRevision}/{@code acceptRevision}/
     * {@code negotiateRevision} — and must never let a client "adopt" a revision id that doesn't
     * belong to THIS evaluation's persisted feedback, since
     * {@code SiadapInterimFeedbackRepositoryImpl}'s delete+saveAll save pattern would merge such an
     * id straight into (and reassign) whatever row it currently belongs to. It must also never let
     * an already-persisted revision (proposed/negotiating/accepted, or otherwise) silently vanish
     * just because the client's request happens to omit it (Issue 3: {@code
     * SiadapInterimFeedbackRepositoryImpl.save} does a full delete-then-recreate of only what's
     * submitted, so an omitted revision id would otherwise be deleted server-side).
     *
     * <p><b>Regression fix (re-review, 2026-07-09):</b> "genuinely new" rows MUST be detected from
     * the RAW incoming {@link ObjectiveRevisionDTO#getId()} string (null/blank) BEFORE mapping to
     * the domain {@link ObjectiveRevision}, not by calling {@code .getId()} on the already-mapped
     * domain object — {@code ObjectiveRevision}'s private constructor unconditionally back-fills a
     * random {@code UUID} whenever the constructor-supplied id is {@code null} (see
     * {@code ObjectiveRevision.create}'s javadoc: "id nunca vem do cliente — se null, é gerado
     * agora, no primeiro save"), so by the time the mapped domain object reaches this method,
     * {@code getId()} is NEVER null and the previous {@code r.getId() == null} check here was dead
     * code — every brand-new revision (the only shape the frontend ever sends for a row it hasn't
     * saved yet) fell through to the "unknown foreign id" branch and was rejected with a 400,
     * making it impossible to ever create a new revision. {@code body.getObjectiveRevisions()} (the
     * raw DTO list) and {@code domainModel.getObjectiveRevisions()} (the mapped domain list) are
     * built from the exact same source list via a single {@code .stream().map(...)} in
     * {@code SiadapInterimFeedbackMapper.toDomain(SiadapInterimFeedbackDTO)}, with no filtering, so
     * they are guaranteed to be the same size and in the same order — safe to zip by index.
     *
     * <p><b>3rd fix round (2026-07-09) — Bug A (regression from the Issue-3 fix):</b> the
     * omitted-revision retention loop below used to retain EVERY existing revision not mentioned
     * in the incoming request, with no check on {@code approvalStatus}. That silently resurrected
     * DRAFT rows (approvalStatus {@code null}) that a user had deliberately deleted via the "✕"
     * button, which deletes a draft row by simply omitting it from the next save. Only LOCKED rows
     * (non-null {@code approvalStatus}) are protected from omission-deletion — that protection is
     * the actual CR-02/Issue-3 intent; drafts must remain freely deletable by omission.
     *
     * <p><b>3rd fix round (2026-07-09) — Bug B (residual gap in the original CR-01 fix):</b> for an
     * incoming revision whose id matches an existing LOCKED revision, forcing back only
     * {@code approvalStatus}/{@code lastNegotiationComment} left {@code currentObjectiveText},
     * {@code revisionJustification}, {@code newObjectiveSmart} and {@code objectiveCode} passing
     * through from the client's DTO unchanged — either party could silently rewrite the CONTENT of
     * an already-proposed/negotiating/accepted revision through this generic endpoint, even though
     * its status could no longer be tampered with. Once a revision is LOCKED, the entire row is now
     * immutable via this endpoint: the existing persisted revision is re-added verbatim, and every
     * field the client sent for that id is ignored. A DRAFT row (approvalStatus still null) is
     * unaffected by this change — its content continues to pass through as-is, exactly as before.
     */
    private SiadapInterimFeedback reconcileRevisions(List<ObjectiveRevisionDTO> incomingDtos,
                                                      SiadapInterimFeedback incoming,
                                                      SiadapInterimFeedback existing) {
        Map<UUID, ObjectiveRevision> existingById = existing == null
                ? Map.of()
                : existing.getObjectiveRevisions().stream()
                        .collect(Collectors.toMap(ObjectiveRevision::getId, r -> r));

        List<ObjectiveRevision> incomingRevisions = incoming.getObjectiveRevisions();
        Set<UUID> seenIds = new HashSet<>();
        List<ObjectiveRevision> reconciled = new ArrayList<>();

        for (int i = 0; i < incomingRevisions.size(); i++) {
            ObjectiveRevisionDTO dto = incomingDtos.get(i);
            ObjectiveRevision r = incomingRevisions.get(i);
            boolean isGenuinelyNew = dto.getId() == null || dto.getId().isBlank();

            if (isGenuinelyNew) {
                // Genuinely new draft row (no client-supplied id in the DTO) — no prior persisted
                // state to protect. `r` already carries the id ObjectiveRevision's constructor
                // back-filled for it (first-save minting), which is exactly what should be persisted.
                reconciled.add(r);
                continue;
            }

            ObjectiveRevision existingRevision = existingById.get(r.getId());
            if (existingRevision == null) {
                // CR-02: a non-null id that doesn't belong to this evaluation's feedback —
                // reject outright rather than silently stripping/regenerating it.
                throw IgrpResponseStatusException.badRequest(
                        "Revisão de objetivo não encontrada nesta avaliação: " + r.getId());
            }

            seenIds.add(r.getId());

            if (existingRevision.getApprovalStatus() == null) {
                // Existing revision is still a DRAFT — the client is legitimately still editing
                // it via this generic endpoint (the only path drafts are ever saved through).
                // Content passes through as-is; approvalStatus/lastNegotiationComment are forced
                // back to the persisted (null) values regardless of what the client sent, since
                // state transitions only ever happen through
                // proposeRevision/acceptRevision/negotiateRevision.
                reconciled.add(ObjectiveRevision.create(
                        r.getId(),
                        r.getCurrentObjectiveText(),
                        r.getRevisionJustification(),
                        r.getNewObjectiveSmart(),
                        existingRevision.getApprovalStatus(),
                        r.getObjectiveCode(),
                        existingRevision.getLastNegotiationComment()));
            } else {
                // Bug B fix: existing revision is LOCKED (PENDING_ACCEPTANCE/NEGOTIATING/
                // ACCEPTED/TACITLY_ACCEPTED) — the ENTIRE row is immutable via this endpoint, not
                // just its status/comment. Ignore every field the client sent for this id and
                // re-persist the existing revision verbatim; only the dedicated
                // propose/accept/negotiate endpoints may ever change a locked row's content or
                // status.
                reconciled.add(existingRevision);
            }
        }

        // Issue 3 / Bug A: retain any existing LOCKED persisted revision the client's request
        // omitted entirely — without this, SiadapInterimFeedbackRepositoryImpl's
        // delete-then-saveAll pattern would silently delete it, even if it was already
        // PROPOSED/NEGOTIATING/ACCEPTED. DRAFT rows (approvalStatus null) are deliberately NOT
        // retained here — omitting a draft row is exactly how the "✕" button deletes it, and
        // resurrecting it here would silently undo that deletion (Bug A regression).
        existingById.forEach((id, existingRevision) -> {
            if (!seenIds.contains(id) && existingRevision.getApprovalStatus() != null) {
                reconciled.add(existingRevision);
            }
        });

        return SiadapInterimFeedback.create(
                incoming.getEvaluationId(),
                incoming.getObjectivesSynthesis(),
                incoming.getObservedFactsStar(),
                incoming.getDifficultiesObstacles(),
                incoming.getFeedbackAndAction(),
                incoming.getCompetencyObservations(),
                incoming.getImprovementActions(),
                reconciled);
    }
}
