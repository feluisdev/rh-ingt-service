package cv.igrp.RH_Service.sigdi.domain.compliance.valueobject;

import lombok.Getter;

/**
 * Value Object representando uma ação de melhoria acordada no feedback intercalar.
 */
@Getter
public class ImprovementAction {

    private final String actionAgreed;
    private final String responsible;
    private final String deadline;
    private final String neededSupport;

    private ImprovementAction(String actionAgreed, String responsible, String deadline, String neededSupport) {
        this.actionAgreed = actionAgreed;
        this.responsible = responsible;
        this.deadline = deadline;
        this.neededSupport = neededSupport;
    }

    public static ImprovementAction create(String actionAgreed, String responsible, String deadline, String neededSupport) {
        return new ImprovementAction(actionAgreed, responsible, deadline, neededSupport);
    }

    public static ImprovementAction reconstruct(String actionAgreed, String responsible, String deadline, String neededSupport) {
        return new ImprovementAction(actionAgreed, responsible, deadline, neededSupport);
    }
}
