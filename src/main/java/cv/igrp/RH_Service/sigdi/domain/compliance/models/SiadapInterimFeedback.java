package cv.igrp.RH_Service.sigdi.domain.compliance.models;

import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.CompetencyObservation;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.ImprovementAction;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.ObjectiveRevision;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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
}
