package cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters.compliance;

import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapInterimFeedback;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapInterimFeedbackRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.compliance.SiadapInterimFeedbackMapper;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.SiadapInterimCompetencyObservationEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.SiadapInterimFeedbackEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.SiadapInterimImprovementActionEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.SiadapInterimObjectiveRevisionEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.SiadapInterimCompetencyObservationEntityRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.SiadapInterimFeedbackEntityRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.SiadapInterimImprovementActionEntityRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.SiadapInterimObjectiveRevisionEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SiadapInterimFeedbackRepositoryImpl implements SiadapInterimFeedbackRepository {

    private final SiadapInterimFeedbackEntityRepository jpaRepository;
    private final SiadapInterimCompetencyObservationEntityRepository competencyJpaRepository;
    private final SiadapInterimImprovementActionEntityRepository actionJpaRepository;
    private final SiadapInterimObjectiveRevisionEntityRepository revisionJpaRepository;
    private final SiadapInterimFeedbackMapper mapper;

    @Transactional
    @Override
    public SiadapInterimFeedback save(SiadapInterimFeedback feedback) {
        UUID evalId = feedback.getEvaluationId();

        // 1. Save main entity
        SiadapInterimFeedbackEntity entity = mapper.toEntity(feedback);
        SiadapInterimFeedbackEntity saved = jpaRepository.save(entity);

        // 2. Save child competency observations
        competencyJpaRepository.deleteByEvaluationId(evalId);
        List<SiadapInterimCompetencyObservationEntity> competencyEntities = mapper.toCompetencyEntities(feedback);
        competencyEntities.forEach(e -> e.setEvaluationId(evalId));
        competencyJpaRepository.saveAll(competencyEntities);

        // 3. Save child improvement actions
        actionJpaRepository.deleteByEvaluationId(evalId);
        List<SiadapInterimImprovementActionEntity> actionEntities = mapper.toActionEntities(feedback);
        actionEntities.forEach(e -> e.setEvaluationId(evalId));
        actionJpaRepository.saveAll(actionEntities);

        // 4. Save child objective revisions
        revisionJpaRepository.deleteByEvaluationId(evalId);
        List<SiadapInterimObjectiveRevisionEntity> revisionEntities = mapper.toRevisionEntities(feedback);
        revisionEntities.forEach(e -> e.setEvaluationId(evalId));
        revisionJpaRepository.saveAll(revisionEntities);

        return mapper.toDomain(saved, competencyEntities, actionEntities, revisionEntities);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<SiadapInterimFeedback> findByEvaluationId(UUID evaluationId) {
        if (evaluationId == null) return Optional.empty();

        return jpaRepository.findById(evaluationId)
                .map(entity -> {
                    List<SiadapInterimCompetencyObservationEntity> competencies = competencyJpaRepository.findByEvaluationId(evaluationId);
                    List<SiadapInterimImprovementActionEntity> actions = actionJpaRepository.findByEvaluationId(evaluationId);
                    List<SiadapInterimObjectiveRevisionEntity> revisions = revisionJpaRepository.findByEvaluationId(evaluationId);
                    return mapper.toDomain(entity, competencies, actions, revisions);
                });
    }
}
