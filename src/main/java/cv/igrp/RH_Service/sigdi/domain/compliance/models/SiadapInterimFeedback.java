package cv.igrp.RH_Service.sigdi.domain.compliance.models;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.AcceptanceStatus;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.CompetencyObservation;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.ImprovementAction;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.ObjectiveRevision;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Agregado de domínio para o Feedback Intercalar do SIADAP.
 */
@Getter
public class SiadapInterimFeedback {

    private final UUID evaluationId;
    private final String objectivesSynthesis;
    private final String observedFactsStar;
    private final String difficultiesObstacles;
    private final String feedbackAndAction;
    private final List<CompetencyObservation> competencyObservations;
    private final List<ImprovementAction> improvementActions;
    private final List<ObjectiveRevision> objectiveRevisions;

    private SiadapInterimFeedback(UUID evaluationId, String objectivesSynthesis, String observedFactsStar,
                                  String difficultiesObstacles, String feedbackAndAction,
                                  List<CompetencyObservation> competencyObservations,
                                  List<ImprovementAction> improvementActions,
                                  List<ObjectiveRevision> objectiveRevisions) {
        if (evaluationId == null) {
            throw new IllegalArgumentException("evaluationId é obrigatório");
        }
        this.evaluationId = evaluationId;
        this.objectivesSynthesis = objectivesSynthesis;
        this.observedFactsStar = observedFactsStar;
        this.difficultiesObstacles = difficultiesObstacles;
        this.feedbackAndAction = feedbackAndAction;
        this.competencyObservations = competencyObservations != null ? new ArrayList<>(competencyObservations) : new ArrayList<>();
        this.improvementActions = improvementActions != null ? new ArrayList<>(improvementActions) : new ArrayList<>();
        this.objectiveRevisions = objectiveRevisions != null ? new ArrayList<>(objectiveRevisions) : new ArrayList<>();
    }

    public static SiadapInterimFeedback create(UUID evaluationId, String objectivesSynthesis, String observedFactsStar,
                                              String difficultiesObstacles, String feedbackAndAction,
                                              List<CompetencyObservation> competencyObservations,
                                              List<ImprovementAction> improvementActions,
                                              List<ObjectiveRevision> objectiveRevisions) {
        return new SiadapInterimFeedback(evaluationId, objectivesSynthesis, observedFactsStar,
                difficultiesObstacles, feedbackAndAction, competencyObservations,
                improvementActions, objectiveRevisions);
    }

    public static SiadapInterimFeedback reconstruct(UUID evaluationId, String objectivesSynthesis, String observedFactsStar,
                                                    String difficultiesObstacles, String feedbackAndAction,
                                                    List<CompetencyObservation> competencyObservations,
                                                    List<ImprovementAction> improvementActions,
                                                    List<ObjectiveRevision> objectiveRevisions) {
        return new SiadapInterimFeedback(evaluationId, objectivesSynthesis, observedFactsStar,
                difficultiesObstacles, feedbackAndAction, competencyObservations,
                improvementActions, objectiveRevisions);
    }

    // ============================================================
    // Behavioral Methods — Revisão de Objetivos (RECONC-01/02)
    // ============================================================

    /**
     * O avaliador propõe a revisão de um objetivo ao avaliado: transita o estado de
     * aceitação dessa revisão (e só dessa) de rascunho (null) para PENDING_ACCEPTANCE.
     * Exige que a revisão já tenha objetivo selecionado e novo texto SMART definidos.
     */
    public SiadapInterimFeedback proposeRevision(UUID revisionId) {
        ObjectiveRevision target = findRevisionOrThrow(revisionId);
        if (target.getApprovalStatus() != null)
            throw IgrpResponseStatusException.badRequest("Esta revisão já foi proposta ou já teve resposta do avaliado.");
        if (target.getObjectiveCode() == null || target.getObjectiveCode().isBlank()
                || target.getNewObjectiveSmart() == null || target.getNewObjectiveSmart().isBlank())
            throw IgrpResponseStatusException.badRequest(
                    "A revisão precisa de um objetivo selecionado e de um novo objetivo SMART antes de ser proposta.");

        return replaceRevision(revisionId, target.withProposed());
    }

    /**
     * O avaliado aceita a revisão de objetivo proposta: transita o estado de aceitação
     * dessa revisão para ACCEPTED. Válido a partir de PENDING_ACCEPTANCE ou NEGOTIATING.
     */
    public SiadapInterimFeedback acceptRevision(UUID revisionId) {
        ObjectiveRevision target = findRevisionOrThrow(revisionId);
        if (target.getApprovalStatus() != AcceptanceStatus.PENDING_ACCEPTANCE
                && target.getApprovalStatus() != AcceptanceStatus.NEGOTIATING)
            throw IgrpResponseStatusException.badRequest(
                    "Apenas revisões pendentes de aceitação ou em negociação podem ser aceites.");

        return replaceRevision(revisionId, target.withAccepted());
    }

    /**
     * O avaliado solicita negociação da revisão de objetivo proposta: transita o estado de
     * aceitação dessa revisão para NEGOTIATING e regista o comentário/justificação apenas
     * nessa revisão. Válido apenas a partir de PENDING_ACCEPTANCE.
     */
    public SiadapInterimFeedback negotiateRevision(UUID revisionId, String comment) {
        ObjectiveRevision target = findRevisionOrThrow(revisionId);
        if (target.getApprovalStatus() != AcceptanceStatus.PENDING_ACCEPTANCE)
            throw IgrpResponseStatusException.badRequest(
                    "Apenas revisões pendentes de aceitação podem iniciar negociação.");

        return replaceRevision(revisionId, target.withNegotiated(comment));
    }

    // ============================================================
    // Helpers
    // ============================================================

    private ObjectiveRevision findRevisionOrThrow(UUID revisionId) {
        return this.objectiveRevisions.stream()
                .filter(r -> revisionId.equals(r.getId()))
                .findFirst()
                .orElseThrow(() -> IgrpResponseStatusException.badRequest("Revisão de objetivo não encontrada: " + revisionId));
    }

    private SiadapInterimFeedback replaceRevision(UUID revisionId, ObjectiveRevision updated) {
        List<ObjectiveRevision> updatedList = this.objectiveRevisions.stream()
                .map(r -> revisionId.equals(r.getId()) ? updated : r)
                .collect(Collectors.toList());

        return new SiadapInterimFeedback(this.evaluationId, this.objectivesSynthesis, this.observedFactsStar,
                this.difficultiesObstacles, this.feedbackAndAction, this.competencyObservations,
                this.improvementActions, updatedList);
    }
}
