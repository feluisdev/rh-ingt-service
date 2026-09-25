package cv.igrp.RH_Service.sigdi.application.service;

import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PaaSubmissionPeriodRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.springframework.stereotype.Component;

/**
 * Single owner of the question "is the {@link Purpose#PAA_BSC_OBJECTIVES} submission window of
 * {@code UNIT_LEVEL} open for the given year?". Before this class existed, three handlers that
 * mutate a {@code StrategicGoal} -- {@code CreateStrategicGoalCommandHandler},
 * {@code UpdateStrategicGoalsCommandHandler} and {@code CancelStrategicGoalCommandHandler} --
 * answered that question by querying {@link PaaSubmissionPeriodRepository} in line, each on its
 * own, with the identical pair {@code (PaaLevel.UNIT_LEVEL, Purpose.PAA_BSC_OBJECTIVES)}. Adding
 * {@code UpdateGoalPositionCommandHandler} without extracting this class first would have made
 * four copies of the same criterion (Fase 136-06). This class exists so the criterion lives in
 * exactly one place, for every caller, present and future.
 * <p>
 * <b>Why this lives in {@code application.service} and not {@code domain.strategy.service}.</b>
 * The question crosses aggregates -- it reads a {@code PaaSubmissionPeriod} (tactical module) to
 * decide about a {@code StrategicGoal} (strategy module) -- and it depends on a repository to do
 * so, exactly the same reasoning that placed {@link SelfEvaluationWindowPolicy} here rather than
 * in a domain-service package.
 * <p>
 * <b>Manual closure and natural expiry are indistinguishable by design.</b> This class never
 * asks a {@code StrategicGoal} about its own dates. It asks
 * {@link PaaSubmissionPeriodRepository#findActiveByTypeAndYearAndPurpose} for the year in
 * question, and that query combines {@code status == OPEN} with "today falls between
 * {@code startDate} and {@code endDate}" into a single boolean. RH closing a period by hand
 * (writing {@code status = CLOSED}) and the period's {@code endDate} simply passing produce the
 * exact same {@code Optional.empty()} result from that query, and this class does not -- and
 * must not -- try to tell the two cases apart.
 * <p>
 * <b>The year always comes from the goal, never from the clock.</b> Unlike
 * {@link PaaActivityWindowPolicy}, which reads {@code Year.now()} because a
 * {@code TacticalActivity} carries no year of its own, a {@code StrategicGoal} does: its
 * {@code year} field is the effective year the three existing callers already used before this
 * extraction. {@link #requireOpenFor(Integer)} takes that year as a parameter for exactly that
 * reason -- there is no clock read here to replicate.
 * <p>
 * <b>The refusal message is deliberately identical to the one the three handlers already used</b>
 * -- "Prazo não configurado para a submissão de objetivos estratégicos PAA/BSC". It is not
 * improved here: the frontend's {@code GoalDrawer.tsx} and {@code PeriodGateAlert} were built
 * against that exact string, and changing it would silently break the client-side explanation
 * without any test failing on either side of the boundary.
 */
@Component
public class StrategicGoalWindowPolicy {

    private static final String REFUSAL_MESSAGE =
            "Prazo não configurado para a submissão de objetivos estratégicos PAA/BSC";

    private final PaaSubmissionPeriodRepository periodRepository;

    public StrategicGoalWindowPolicy(PaaSubmissionPeriodRepository periodRepository) {
        this.periodRepository = periodRepository;
    }

    /**
     * Throws {@link IgrpResponseStatusException#badRequest(String)} when there is no active
     * {@link Purpose#PAA_BSC_OBJECTIVES} submission window for {@code PaaLevel.UNIT_LEVEL} in
     * {@code year}; does nothing otherwise. A {@code null} year is not special-cased here -- the
     * query simply returns empty for it, and this method refuses with the same message. Callers
     * that must distinguish "year is missing" from "window is closed" with a dedicated message
     * (as all four callers of this class do) perform that check themselves before calling this
     * method.
     */
    public void requireOpenFor(Integer year) {
        periodRepository.findActiveByTypeAndYearAndPurpose(
                        PaaLevel.UNIT_LEVEL, year, Purpose.PAA_BSC_OBJECTIVES)
                .orElseThrow(() -> IgrpResponseStatusException.badRequest(REFUSAL_MESSAGE));
    }
}
