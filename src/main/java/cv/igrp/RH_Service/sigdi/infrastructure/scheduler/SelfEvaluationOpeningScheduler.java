package cv.igrp.RH_Service.sigdi.infrastructure.scheduler;

import cv.igrp.RH_Service.sigdi.application.constants.EvaluationPhase;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapEvaluation;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapEvaluationRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.PaaSubmissionPeriod;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PaaSubmissionPeriodRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Daily job that gives {@link SiadapEvaluation#openSelfEvaluationPhase()} its first caller
 * (FUT-12). Until this scheduler existed, that domain method — and by cascade
 * {@code finalizeEvaluation()} and {@code markQuotaValidated()} — was unreachable.
 * <p>
 * <b>Where the "is the window open" decision comes from, and why manual closure and natural
 * expiry are indistinguishable by design.</b> This class never asks a {@link SiadapEvaluation}
 * about its own dates. It asks {@link PaaSubmissionPeriodRepository#findActiveByTypeAndYearAndPurpose}
 * for the year in question, and that query is backed by
 * {@code PaaSubmissionPeriod.isActiveToday()}, which combines {@code status == OPEN} with
 * "today falls between {@code startDate} and {@code endDate}" into a single boolean. The RH
 * closing a period by hand (writing {@code status = CLOSED}) and the period's {@code endDate}
 * simply passing produce the exact same {@link Optional#empty()} result from that query. This
 * scheduler does not — and must not — try to tell the two cases apart: the predicate already
 * unifies them, and that unification is what satisfies success criterion 5 of Phase 102.
 * <p>
 * <b>Why this class never calls the zone-less {@code LocalDate} factory.</b> Unlike its closest
 * precedent, {@link TacitAcceptanceScheduler}, which at line 45 compares an entity's own
 * {@code endDate} against today's date read with no explicit zone (JVM/container default zone
 * — a known, uncorrected defect, see {@code CLAUDE.md} rule 7), this scheduler performs no date
 * comparison of its own at all. The "what day is it" decision is made once, downstream, inside
 * {@code PaaSubmissionPeriodRepositoryImpl}, which already reads today's date with
 * {@code AppTimeZone.CABO_VERDE} explicitly. Delegating the decision is how success criterion 6
 * is satisfied — by never introducing the problem, not by fixing it afterwards.
 * <p>
 * <b>What happens when the window closes, and when this job does not run.</b> If a day's run
 * is skipped, nothing is lost while the window stays open: the next run picks up whatever
 * evaluations are still waiting, because the query is against current state, not a diff since
 * last run. If the window closes before the next run happens, the affected evaluations stay in
 * {@link EvaluationPhase#IN_PROGRESS} and are <b>not</b> opened — a declared consequence, not a
 * hidden defect. What to do about evaluations that never self-evaluated is the scope of Phase
 * 103, not this scheduler.
 */
@Component
public class SelfEvaluationOpeningScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SelfEvaluationOpeningScheduler.class);
    private static final int PAGE_SIZE = 100;

    private final SiadapEvaluationRepository evaluationRepository;
    private final PaaSubmissionPeriodRepository periodRepository;

    public SelfEvaluationOpeningScheduler(SiadapEvaluationRepository evaluationRepository,
                                           PaaSubmissionPeriodRepository periodRepository) {
        this.evaluationRepository = evaluationRepository;
        this.periodRepository = periodRepository;
    }

    /**
     * Runs daily at 1:00 AM by default; overridable via
     * {@code sigdi.siadap.self-evaluation-opening.cron} (env var
     * {@code SIGDI_SIADAP_SELF_EVALUATION_OPENING_CRON}), following the naming introduced for
     * {@code sigdi.siadap.cca-employee-ids} in Phase 101. Discovers every evaluation in
     * {@link EvaluationPhase#IN_PROGRESS}, and for each one asks whether its year's individual
     * self-evaluation submission window is currently active. If it is, transitions the
     * evaluation to {@link EvaluationPhase#SELF_EVALUATION}. A failure processing one evaluation
     * is logged and does not stop the rest of the batch.
     */
    @Scheduled(cron = "${sigdi.siadap.self-evaluation-opening.cron:0 0 1 * * ?}")
    @Transactional
    public void processSelfEvaluationOpenings() {
        LOGGER.info("Starting automated job to open SIADAP self-evaluation phase...");

        Map<Integer, Optional<PaaSubmissionPeriod>> activePeriodByYear = new HashMap<>();

        int page = 0;
        List<SiadapEvaluation> batch;
        int opened = 0;
        do {
            batch = evaluationRepository.findAll(null, null, EvaluationPhase.IN_PROGRESS, page, PAGE_SIZE);
            for (SiadapEvaluation evaluation : batch) {
                try {
                    Optional<PaaSubmissionPeriod> activePeriod = activePeriodByYear.computeIfAbsent(
                            evaluation.getYear(),
                            year -> periodRepository.findActiveByTypeAndYearAndPurpose(
                                    PaaLevel.INDIVIDUAL_LEVEL, year, Purpose.SIADAP_SELF_EVAL));

                    if (activePeriod.isPresent()) {
                        SiadapEvaluation updated = evaluation.openSelfEvaluationPhase();
                        evaluationRepository.save(updated);
                        opened++;
                        LOGGER.info("Opened self-evaluation phase for SiadapEvaluation ID: {}",
                                evaluation.getId().getStringValor());
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to open self-evaluation phase for SiadapEvaluation ID: {}",
                            evaluation.getId().getStringValor(), e);
                }
            }
            page++;
        } while (batch.size() == PAGE_SIZE);

        LOGGER.info("Finished self-evaluation opening job. {} evaluation(s) transitioned.", opened);
    }
}
