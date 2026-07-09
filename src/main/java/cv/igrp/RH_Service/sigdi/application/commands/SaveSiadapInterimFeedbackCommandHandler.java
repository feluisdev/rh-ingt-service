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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SaveSiadapInterimFeedbackCommandHandler
    implements CommandHandler<SaveSiadapInterimFeedbackCommand, ResponseEntity<SiadapInterimFeedbackDTO>> {

    private final SiadapInterimFeedbackRepository repository;
    private final SiadapEvaluationRepository evaluationRepository;
    private final SiadapInterimFeedbackMapper mapper;
    private final CurrentEmployeeResolver currentEmployeeResolver;

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

        SiadapInterimFeedbackDTO body = command.getBody();
        body.setEvaluationId(command.getEvaluationId());

        Optional<SiadapInterimFeedback> existing = repository.findByEvaluationId(evalUuid);
        SiadapInterimFeedback domainModel = mapper.toDomain(body);
        SiadapInterimFeedback reconciled = reconcileRevisions(domainModel, existing.orElse(null));

        SiadapInterimFeedback saved = repository.save(reconciled);

        return ResponseEntity.ok(mapper.toDto(saved));
    }

    /**
     * CR-01/CR-02: the generic save endpoint must never let a client drive {@code approvalStatus}/
     * {@code lastNegotiationComment} transitions directly — those only ever happen through
     * {@code proposeRevision}/{@code acceptRevision}/{@code negotiateRevision} — and must never let
     * a client "adopt" a revision id that doesn't belong to THIS evaluation's persisted feedback,
     * since {@code SiadapInterimFeedbackRepositoryImpl}'s delete+saveAll save pattern would merge
     * such an id straight into (and reassign) whatever row it currently belongs to.
     */
    private SiadapInterimFeedback reconcileRevisions(SiadapInterimFeedback incoming, SiadapInterimFeedback existing) {
        Map<UUID, ObjectiveRevision> existingById = existing == null
                ? Map.of()
                : existing.getObjectiveRevisions().stream()
                        .collect(Collectors.toMap(ObjectiveRevision::getId, r -> r));

        List<ObjectiveRevision> reconciled = incoming.getObjectiveRevisions().stream()
                .map(r -> {
                    if (r.getId() == null) {
                        // Genuinely new draft row — no prior persisted state to protect.
                        return r;
                    }
                    ObjectiveRevision existingRevision = existingById.get(r.getId());
                    if (existingRevision == null) {
                        // CR-02: a non-null id that doesn't belong to this evaluation's feedback —
                        // reject outright rather than silently stripping/regenerating it.
                        throw IgrpResponseStatusException.badRequest(
                                "Revisão de objetivo não encontrada nesta avaliação: " + r.getId());
                    }
                    // CR-01: force the persisted approvalStatus/lastNegotiationComment, ignoring
                    // whatever the client sent for those two fields.
                    return ObjectiveRevision.create(
                            r.getId(),
                            r.getCurrentObjectiveText(),
                            r.getRevisionJustification(),
                            r.getNewObjectiveSmart(),
                            existingRevision.getApprovalStatus(),
                            r.getObjectiveCode(),
                            existingRevision.getLastNegotiationComment());
                })
                .collect(Collectors.toList());

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
