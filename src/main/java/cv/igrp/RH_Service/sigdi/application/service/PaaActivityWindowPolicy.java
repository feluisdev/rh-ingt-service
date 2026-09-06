package cv.igrp.RH_Service.sigdi.application.service;

import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PaaSubmissionPeriodRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.springframework.stereotype.Component;

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
 * Both already query {@link PaaSubmissionPeriodRepository#findActiveByTypeAndYearAndPurpose} with
 * the pair {@code (paaLevel, Purpose.PAA)} and {@code Year.now().getValue()} -- this is the
 * {@code D-27} decision of Phase 134: the pair and the year criterion must not diverge between
 * handlers, because divergence is precisely the defect this class prevents. The year is
 * deliberately absent from {@link #requireOpenFor(PaaLevel)}'s signature -- a caller cannot pass a
 * year that disagrees with the mould, because there is nowhere to pass it.
 * <p>
 * <b>"Active" means {@code status == OPEN} and today between {@code startDate} and
 * {@code endDate}, both boundaries inclusive.</b> That predicate is evaluated entirely inside
 * {@code findActiveByTypeAndYearAndPurpose}; manual closure (writing {@code status = CLOSED}) and
 * natural expiry (the {@code endDate} passing) are indistinguishable from this call, and this
 * class does not -- and must not -- try to tell them apart.
 * <p>
 * <b>Inherited limitation.</b> {@code Year.now()} reads the system's time zone, not Cape Verde's,
 * exactly as it already does in the two handlers this class mirrors ({@code A-132-106}). Fixing
 * that here would mean inventing a new criterion, which {@code D-27} forbids -- it is recorded as
 * a named limitation, not fixed by this class.
 */
@Component
public class PaaActivityWindowPolicy {

    private final PaaSubmissionPeriodRepository periodRepository;

    public PaaActivityWindowPolicy(PaaSubmissionPeriodRepository periodRepository) {
        this.periodRepository = periodRepository;
    }

    /**
     * Throws {@link IgrpResponseStatusException#badRequest(String)} when there is no active
     * {@link Purpose#PAA} submission window for {@code paaLevel} in the current year; does
     * nothing otherwise. A {@code null} {@code paaLevel} is not special-cased: the query simply
     * returns empty for it, and this method refuses with the same message -- a dedicated
     * null-guard would only give the caller a second message for the same fact.
     */
    public void requireOpenFor(PaaLevel paaLevel) {
        periodRepository.findActiveByTypeAndYearAndPurpose(
                        paaLevel, java.time.Year.now().getValue(), Purpose.PAA)
                .orElseThrow(() -> IgrpResponseStatusException.badRequest(
                        "Prazo não configurado para a submissão de atividades do PAA"));
    }
}
