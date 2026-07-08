package cv.igrp.RH_Service.sigdi.domain.compliance.repository;

import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapInterimFeedback;

import java.util.Optional;
import java.util.UUID;

/**
 * Interface do repositório de domínio para o Feedback Intercalar.
 */
public interface SiadapInterimFeedbackRepository {

    Optional<SiadapInterimFeedback> findByEvaluationId(UUID evaluationId);

    SiadapInterimFeedback save(SiadapInterimFeedback feedback);
}
