package cv.igrp.RH_Service.sigdi.application.service;

import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.PaaSubmissionPeriod;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PaaSubmissionPeriodRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Single owner of the question "is the {@link Purpose#SIADAP_SELF_EVAL} submission window of
 * year N currently active?". Before this class existed, that question was asked in two places
 * that answered it in opposite senses: {@code SelfEvaluationOpeningScheduler} (present-ness
 * opens the phase) and {@code SelfEvaluationTacitAcceptanceScheduler} (absence closes it). A
 * third caller -- a manual command -- would have made it three. This class exists so the answer
 * lives in exactly one place, for every caller, present and future.
 * <p>
 * <b>Why this lives in {@code application.service} and not {@code domain.compliance.service}.</b>
 * The closest precedent, {@link cv.igrp.RH_Service.sigdi.domain.compliance.service.SiadapQuotaPolicy},
 * is static and dependency-free. This class cannot follow that shape: the question it answers
 * crosses aggregates -- it reads a {@link PaaSubmissionPeriod} (tactical module) to decide about a
 * {@code SiadapEvaluation} (compliance module) -- and it depends on a repository to do so.
 * Imitating {@code SiadapQuotaPolicy}'s static, pure shape here would force the period to be
 * passed in by every caller, which would hand the "which period is the right one" decision back
 * to the callers -- exactly the duplication this class exists to eliminate.
 * <p>
 * <b>Where the "is the window open" decision comes from, and why manual closure and natural
 * expiry are indistinguishable by design.</b> This class never asks a {@code SiadapEvaluation}
 * about its own dates. It asks {@link PaaSubmissionPeriodRepository#findActiveByTypeAndYearAndPurpose}
 * for the year in question, and that query is backed by {@code PaaSubmissionPeriod.isActiveToday()},
 * which combines {@code status == OPEN} with "today falls between {@code startDate} and
 * {@code endDate}" into a single boolean. RH closing a period by hand (writing
 * {@code status = CLOSED}) and the period's {@code endDate} simply passing produce the exact same
 * {@link Optional#empty()} result from that query. This class does not -- and must not -- try to
 * tell the two cases apart: the predicate already unifies them, and that unification is what
 * satisfies success criterion 5 of Phase 102. The "what day is it" decision itself is made
 * downstream, inside {@code PaaSubmissionPeriodRepositoryImpl}, which reads today's date with
 * {@code AppTimeZone.CABO_VERDE} explicitly.
 */
@Component
public class SelfEvaluationWindowPolicy {

    private final PaaSubmissionPeriodRepository periodRepository;

    public SelfEvaluationWindowPolicy(PaaSubmissionPeriodRepository periodRepository) {
        this.periodRepository = periodRepository;
    }

    /**
     * Returns {@code true} when the individual-level {@code SIADAP_SELF_EVAL} submission window
     * is active for the given year. This is the form the schedulers consume: both branch on the
     * boolean and neither needs the period itself.
     */
    public boolean isOpenFor(Integer year) {
        return findActiveWindow(year).isPresent();
    }

    /**
     * Throws {@link IgrpResponseStatusException#badRequest(String)} when the individual-level
     * {@code SIADAP_SELF_EVAL} submission window is not active for the given year; does nothing
     * otherwise. This is the form a manual command consumes -- it needs a readable HTTP refusal,
     * not a silent branch.
     */
    public void requireOpenFor(Integer year) {
        if (!isOpenFor(year)) {
            throw IgrpResponseStatusException.badRequest(
                    "Não existe uma janela de autoavaliação SIADAP ativa para o ano " + year
                            + ". Abra o período de submissão de Autoavaliação SIADAP antes de abrir a autoavaliação.");
        }
    }

    private Optional<PaaSubmissionPeriod> findActiveWindow(Integer year) {
        return periodRepository.findActiveByTypeAndYearAndPurpose(
                PaaLevel.INDIVIDUAL_LEVEL, year, Purpose.SIADAP_SELF_EVAL);
    }
}
