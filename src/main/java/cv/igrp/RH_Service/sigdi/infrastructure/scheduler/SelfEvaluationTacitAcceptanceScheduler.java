package cv.igrp.RH_Service.sigdi.infrastructure.scheduler;

import cv.igrp.RH_Service.sigdi.application.constants.EvaluationPhase;
import cv.igrp.RH_Service.sigdi.application.service.SelfEvaluationWindowPolicy;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapEvaluation;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapEvaluationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Daily job that gives {@link SiadapEvaluation#applyTacitSelfEvaluationAcceptance()} its first
 * caller (SIA-03). Until this scheduler existed, that domain method was unreachable.
 * <p>
 * <b>The inversion, and why it is safe.</b> {@link SelfEvaluationOpeningScheduler} (Phase 102)
 * looks at evaluations in {@link EvaluationPhase#IN_PROGRESS} and acts when
 * {@link SelfEvaluationWindowPolicy#isOpenFor(Integer)} answers {@code true} for the year. This
 * scheduler looks at evaluations in {@link EvaluationPhase#SELF_EVALUATION} and acts when the
 * very same policy answers {@code false} for the same year. The two conditions are mutually
 * exclusive for a given year, so running both jobs at the same default hour introduces no
 * coordination problem: for any one evaluation, at most one of the two schedulers can find its
 * guard satisfied. Both schedulers ask exactly one class this question -- see
 * {@link SelfEvaluationWindowPolicy}'s Javadoc for where the answer comes from.
 * <p>
 * <b>Absence and expiry are the same fact, by design.</b> A year whose evaluations are in
 * {@code SELF_EVALUATION} and whose window was closed by RH, expired naturally, or was
 * <b>never configured at all</b>, makes {@code isOpenFor} return the exact same {@code false} --
 * and is therefore eligible for tacit acceptance on the very first run after the evaluations
 * entered that phase. This is the unification success criterion 5 requires, taken to its
 * consequence: there is no "grace period" for a window that was simply never created, because
 * absence of a window and an expired window are indistinguishable for this predicate.
 * <p>
 * <b>Why this class never calls the zone-less {@code LocalDate} factory.</b> Like
 * {@link SelfEvaluationOpeningScheduler}, this scheduler performs no date comparison of its own.
 * The "what day is it" decision is made once, downstream inside the submission-period repository
 * implementation, which reads today's date with {@code AppTimeZone.CABO_VERDE} explicitly.
 */
@Component
public class SelfEvaluationTacitAcceptanceScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SelfEvaluationTacitAcceptanceScheduler.class);
    private static final int PAGE_SIZE = 100;

    private final SiadapEvaluationRepository evaluationRepository;
    private final SelfEvaluationWindowPolicy windowPolicy;

    public SelfEvaluationTacitAcceptanceScheduler(SiadapEvaluationRepository evaluationRepository,
                                                   SelfEvaluationWindowPolicy windowPolicy) {
        this.evaluationRepository = evaluationRepository;
        this.windowPolicy = windowPolicy;
    }

    /**
     * Runs daily at 1:00 AM by default; overridable via
     * {@code sigdi.siadap.self-evaluation-tacit-acceptance.cron} (env var
     * {@code SIGDI_SIADAP_SELF_EVALUATION_TACIT_ACCEPTANCE_CRON}), following the naming
     * introduced for {@code sigdi.siadap.self-evaluation-opening.cron} in Phase 102. Discovers
     * every evaluation in {@link EvaluationPhase#SELF_EVALUATION}, and for each one asks whether
     * its year's individual self-evaluation submission window is currently active. If it is
     * <b>not</b> -- closed, expired, or never configured -- transitions the evaluation to
     * {@link EvaluationPhase#MANAGER_EVALUATION} without a self-evaluation score. A failure
     * processing one evaluation is logged and does not stop the rest of the batch.
     */
    @Scheduled(cron = "${sigdi.siadap.self-evaluation-tacit-acceptance.cron:0 0 1 * * ?}")
    @Transactional
    public void processTacitSelfEvaluationAcceptances() {
        LOGGER.info("Starting automated job to apply SIADAP self-evaluation tacit acceptances...");

        Map<Integer, Boolean> windowOpenByYear = new HashMap<>();

        int page = 0;
        List<SiadapEvaluation> batch;
        int accepted = 0;
        do {
            batch = evaluationRepository.findAll(null, null, EvaluationPhase.SELF_EVALUATION, page, PAGE_SIZE);
            for (SiadapEvaluation evaluation : batch) {
                try {
                    Boolean windowOpen = windowOpenByYear.computeIfAbsent(
                            evaluation.getYear(), windowPolicy::isOpenFor);

                    // Boolean.TRUE.equals(...) instead of a direct unboxing comparison: a stray
                    // null here must fall on the safe side (do not accept tacitly) rather than
                    // throw a NullPointerException in the middle of a batch.
                    if (!Boolean.TRUE.equals(windowOpen)) {
                        SiadapEvaluation updated = evaluation.applyTacitSelfEvaluationAcceptance();
                        evaluationRepository.save(updated);
                        accepted++;
                        LOGGER.info("Applied tacit self-evaluation acceptance for SiadapEvaluation ID: {}",
                                evaluation.getId().getStringValor());
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to apply tacit self-evaluation acceptance for SiadapEvaluation ID: {}",
                            evaluation.getId().getStringValor(), e);
                }
            }
            page++;
        } while (batch.size() == PAGE_SIZE);

        LOGGER.info("Finished self-evaluation tacit acceptance job. {} evaluation(s) transitioned.", accepted);
    }
}
