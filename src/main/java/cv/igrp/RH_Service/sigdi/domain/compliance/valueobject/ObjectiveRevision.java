package cv.igrp.RH_Service.sigdi.domain.compliance.valueobject;

import lombok.Getter;

/**
 * Value Object representando a revisão de um objetivo no feedback intercalar.
 */
@Getter
public class ObjectiveRevision {

    private final String currentObjectiveText;
    private final String revisionJustification;
    private final String newObjectiveSmart;
    private final String approvalStatus;

    private ObjectiveRevision(String currentObjectiveText, String revisionJustification, String newObjectiveSmart, String approvalStatus) {
        this.currentObjectiveText = currentObjectiveText;
        this.revisionJustification = revisionJustification;
        this.newObjectiveSmart = newObjectiveSmart;
        this.approvalStatus = approvalStatus;
    }

    public static ObjectiveRevision create(String currentObjectiveText, String revisionJustification, String newObjectiveSmart, String approvalStatus) {
        return new ObjectiveRevision(currentObjectiveText, revisionJustification, newObjectiveSmart, approvalStatus);
    }

    public static ObjectiveRevision reconstruct(String currentObjectiveText, String revisionJustification, String newObjectiveSmart, String approvalStatus) {
        return new ObjectiveRevision(currentObjectiveText, revisionJustification, newObjectiveSmart, approvalStatus);
    }
}
