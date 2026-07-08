package cv.igrp.RH_Service.sigdi.domain.compliance.valueobject;

import lombok.Getter;

/**
 * Value Object representando a observação de uma competência comportamental no feedback intercalar.
 */
@Getter
public class CompetencyObservation {

    private final String competencyCode;
    private final String observedEvidence;

    private CompetencyObservation(String competencyCode, String observedEvidence) {
        if (competencyCode == null || competencyCode.isBlank()) {
            throw new IllegalArgumentException("competencyCode é obrigatório");
        }
        this.competencyCode = competencyCode;
        this.observedEvidence = observedEvidence;
    }

    public static CompetencyObservation create(String competencyCode, String observedEvidence) {
        return new CompetencyObservation(competencyCode, observedEvidence);
    }

    public static CompetencyObservation reconstruct(String competencyCode, String observedEvidence) {
        return new CompetencyObservation(competencyCode, observedEvidence);
    }
}
