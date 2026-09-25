package cv.igrp.RH_Service.sigdi.application.service;

import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.PaaSubmissionPeriod;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PaaSubmissionPeriodRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Single owner of the question "is the individual-level SIADAP objectives window of year N
 * active?", asked in two variants: {@link Purpose#SIADAP} for contractualization
 * (propose/negotiate/accept the initial objectives) and {@link Purpose#SIADAP_INTERIM} for
 * objective revision (propose/negotiate/accept a revision). Before this class existed, only the
 * first step of each three-step flow asked the question -- {@code ContractualizeObjectivesCommandHandler}
 * and {@code ProposeObjectiveRevisionCommandHandler} each carried the query in line, and the four
 * handlers that negotiate and accept carried none at all ({@code A-132-114}, {@code A-132-115}).
 * This class exists so the criterion for each purpose lives in exactly one place, for the three
 * steps that now consult it and for any future caller.
 * <p>
 * <b>Two variants, deliberately, and not a single method taking a {@link Purpose} parameter.</b>
 * A {@code Purpose} parameter would hand back to every caller the decision of which finalidade to
 * consult -- exactly the duplication this class exists to eliminate. {@code requireContractualizationOpenFor}
 * and {@code requireRevisionOpenFor} each fix their own {@code Purpose}, so a caller cannot pass a
 * finalidade that disagrees with the flow it is in.
 * <p>
 * <b>Why this lives in {@code application.service} and not the domain.</b> The question crosses
 * aggregates -- it reads a {@link PaaSubmissionPeriod} (tactical module) to decide about a
 * {@code SiadapEvaluation} (compliance module) -- and it depends on a repository to do so, the
 * same reasoning already written on {@link SelfEvaluationWindowPolicy}.
 * <p>
 * <b>Manual closure and natural expiry are indistinguishable by design.</b> This class never asks
 * a {@code SiadapEvaluation} about its own dates. It asks
 * {@link PaaSubmissionPeriodRepository#findActiveByTypeAndYearAndPurpose} for the year in
 * question, and that query is backed by {@code PaaSubmissionPeriod.isActiveToday()}, which
 * combines {@code status == OPEN} with "today falls between {@code startDate} and
 * {@code endDate}" into a single boolean. RH closing a period by hand and the period's
 * {@code endDate} simply passing produce the exact same {@link Optional#empty()} result from that
 * query, and this class does not -- and must not -- try to tell the two cases apart.
 * <p>
 * <b>The "what day is it" decision is made downstream, and the year never is.</b>
 * {@code PaaSubmissionPeriodRepositoryImpl} reads today's date with
 * {@code AppTimeZone.CABO_VERDE} explicitly. The year, in contrast, is never read from the clock
 * here -- both variants take {@code year} as a parameter, and every caller passes
 * {@code evaluation.getYear()}. A method that reached for {@code Year.now()} instead would be
 * inventing a criterion this class does not own; that mistake is recorded, not repeated, in
 * {@code PaaActivityWindowPolicy}'s javadoc ({@code A-132-106}).
 */
@Component
public class SiadapObjectivesWindowPolicy {

    private final PaaSubmissionPeriodRepository periodRepository;

    public SiadapObjectivesWindowPolicy(PaaSubmissionPeriodRepository periodRepository) {
        this.periodRepository = periodRepository;
    }

    /**
     * Throws {@link IgrpResponseStatusException#badRequest(String)} when there is no active
     * individual-level {@link Purpose#SIADAP} window for {@code year}; does nothing otherwise.
     * Consumed by the three steps of objectives contractualization: propose, negotiate, accept.
     * A {@code null} year is not special-cased: the query simply returns empty for it, and this
     * method refuses with the same message -- a dedicated null-guard would only give the caller a
     * second message for the same fact.
     */
    public void requireContractualizationOpenFor(Integer year) {
        requireOpenFor(year, Purpose.SIADAP);
    }

    /**
     * Throws {@link IgrpResponseStatusException#badRequest(String)} when there is no active
     * individual-level {@link Purpose#SIADAP_INTERIM} window for {@code year}; does nothing
     * otherwise. Consumed by the three steps of objective revision: propose, negotiate, accept.
     * Same null-year behaviour as {@link #requireContractualizationOpenFor(Integer)}.
     */
    public void requireRevisionOpenFor(Integer year) {
        requireOpenFor(year, Purpose.SIADAP_INTERIM);
    }

    private void requireOpenFor(Integer year, Purpose purpose) {
        findActiveWindow(year, purpose)
                .orElseThrow(() -> IgrpResponseStatusException.badRequest("Prazo não configurado para este ano"));
    }

    private Optional<PaaSubmissionPeriod> findActiveWindow(Integer year, Purpose purpose) {
        return periodRepository.findActiveByTypeAndYearAndPurpose(PaaLevel.INDIVIDUAL_LEVEL, year, purpose);
    }
}
