package cv.igrp.RH_Service.sigdi.application.service;

import cv.igrp.RH_Service.shared.config.AppTimeZone;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PaaSubmissionPeriodRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Year;

/**
 * Single owner of the question "is the {@link Purpose#PAA} submission window of the current
 * year open for acts on a {@code TacticalActivity}?". Before this class existed, five of the six
 * handlers that transition a {@code TacticalActivity}'s state answered that question by not
 * asking it at all -- {@code A-132-111} found none of them held a
 * {@link PaaSubmissionPeriodRepository} in their constructor. This class exists so the criterion
 * lives in exactly one place, for every caller, present and future.
 * <p>
 * <b>The criterion is deliberately identical to
 * {@code CreateTacticalActivityCommandHandler} and {@code UpdateTacticalActivityCommandHandler}.</b>
 * Both queried {@link PaaSubmissionPeriodRepository#findActiveByTypeAndYearAndPurpose} with
 * the pair {@code (paaLevel, Purpose.PAA)} and the current year -- this is the
 * {@code D-27} decision of Phase 134: the pair and the year criterion must not diverge between
 * handlers, because divergence is precisely the defect this class prevents. Since Phase 136
 * ({@code 136-11}), the two handlers no longer query the repository at all -- they call
 * {@link #requireOpenFor(PaaLevel)} instead, so the pair and the year are no longer merely
 * "identical in three places", they exist in exactly one. The year is deliberately absent from
 * {@link #requireOpenFor(PaaLevel)}'s signature -- a caller cannot pass a year that disagrees
 * with the mould, because there is nowhere to pass it.
 * <p>
 * <b>"Active" means {@code status == OPEN} and today between {@code startDate} and
 * {@code endDate}, both boundaries inclusive.</b> That predicate is evaluated entirely inside
 * {@code findActiveByTypeAndYearAndPurpose}; manual closure (writing {@code status = CLOSED}) and
 * natural expiry (the {@code endDate} passing) are indistinguishable from this call, and this
 * class does not -- and must not -- try to tell them apart.
 * <p>
 * <b>Remaining limitation (not what was fixed here).</b> The year consulted is always the
 * <em>current</em> year in Cape Verde -- {@code TacticalActivity} has no exercise-year field of
 * its own, unlike {@code StrategicGoal} ({@code goal.getYear()}) or {@code SiadapEvaluation}
 * ({@code evaluation.getYear()}). Planning, in 2026, the 2027 PAA still consults the 2026 window,
 * and there is no field through which a caller could say otherwise. This is a named, already
 * declared future requirement ("O ano de exercício da atividade" in
 * {@code .planning/REQUIREMENTS.md} §Requisitos futuros) -- {@link #requireOpenFor(PaaLevel)}'s
 * signature deliberately still has no year parameter, because a caller cannot pass a year that
 * the model does not have. Fixing that here would mean adding a field to the aggregate, which is
 * a schema and contract change {@code 136-11} is explicitly forbidden from making.
 */
@Component
public class PaaActivityWindowPolicy {

    private final PaaSubmissionPeriodRepository periodRepository;
    private final Clock clock;

    // @Autowired is required here (Rule 1): this class now declares a second, package-private
    // constructor for tests (below). Spring's constructor-resolution only auto-selects a
    // single unannotated constructor; with two present, an unannotated bean would fail to
    // start ("no default constructor found") the moment this class is wired into any handler.
    @Autowired
    public PaaActivityWindowPolicy(PaaSubmissionPeriodRepository periodRepository) {
        this(periodRepository, Clock.system(AppTimeZone.CABO_VERDE));
    }

    /**
     * Test-only constructor. {@code Year.now(ZoneId)} alone can be pinned to a zone but not to
     * an instant -- proving that this class reads the Cape Verde year, and not the system's,
     * requires fixing both at once, at the exact instant where the two zones disagree (the last
     * hour of 31 December in Cape Verde, already 1 January in UTC). A {@link Clock} is the only
     * way to do that without the test racing the real clock.
     */
    PaaActivityWindowPolicy(PaaSubmissionPeriodRepository periodRepository, Clock clock) {
        this.periodRepository = periodRepository;
        this.clock = clock;
    }

    /**
     * Throws {@link IgrpResponseStatusException#badRequest(String)} when there is no active
     * {@link Purpose#PAA} submission window for {@code paaLevel} in the current year (read in
     * Cape Verde's time zone, per regra 7 do CLAUDE.md); does nothing otherwise. A {@code null}
     * {@code paaLevel} is not special-cased: the query simply returns empty for it, and this
     * method refuses with the same message -- a dedicated null-guard would only give the caller
     * a second message for the same fact.
     */
    public void requireOpenFor(PaaLevel paaLevel) {
        periodRepository.findActiveByTypeAndYearAndPurpose(
                        paaLevel, Year.now(clock).getValue(), Purpose.PAA)
                .orElseThrow(() -> IgrpResponseStatusException.badRequest(
                        "Prazo não configurado para a submissão de atividades do PAA"));
    }
}
