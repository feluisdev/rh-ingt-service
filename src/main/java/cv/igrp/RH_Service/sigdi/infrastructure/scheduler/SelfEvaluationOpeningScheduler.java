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
 * Daily job that gives {@link SiadapEvaluation#openSelfEvaluationPhase()} its first caller
 * (FUT-12). Until this scheduler existed, that domain method — and by cascade
 * {@code finalizeEvaluation()} and {@code markQuotaValidated()} — was unreachable.
 * <p>
 * <b>Where the "is the window open" decision comes from.</b> This class no longer queries the
 * submission-period repository directly — it delegates to
 * {@link SelfEvaluationWindowPolicy#isOpenFor(Integer)}, the single owner of that question shared
 * with {@link SelfEvaluationTacitAcceptanceScheduler}. See that class's Javadoc for why manual
 * closure and natural expiry are indistinguishable by design, and why the "what day is it"
 * decision is made downstream with {@code AppTimeZone.CABO_VERDE}.
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
    private final SelfEvaluationWindowPolicy windowPolicy;

    public SelfEvaluationOpeningScheduler(SiadapEvaluationRepository evaluationRepository,
                                           SelfEvaluationWindowPolicy windowPolicy) {
        this.evaluationRepository = evaluationRepository;
        this.windowPolicy = windowPolicy;
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

        Map<Integer, Boolean> windowOpenByYear = new HashMap<>();

        int page = 0;
        List<SiadapEvaluation> batch;
        int opened = 0;
        do {
            batch = evaluationRepository.findAll(null, null, EvaluationPhase.IN_PROGRESS, page, PAGE_SIZE);
            for (SiadapEvaluation evaluation : batch) {
                try {
                    Boolean windowOpen = windowOpenByYear.computeIfAbsent(
                            evaluation.getYear(), windowPolicy::isOpenFor);

                    if (Boolean.TRUE.equals(windowOpen)) {
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
