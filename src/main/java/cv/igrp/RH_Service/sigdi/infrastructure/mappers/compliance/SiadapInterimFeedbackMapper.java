package cv.igrp.RH_Service.sigdi.infrastructure.mappers.compliance;

import cv.igrp.RH_Service.sigdi.application.dto.CompetencyObservationDTO;
import cv.igrp.RH_Service.sigdi.application.dto.ImprovementActionDTO;
import cv.igrp.RH_Service.sigdi.application.dto.ObjectiveRevisionDTO;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapInterimFeedbackDTO;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapInterimFeedback;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.CompetencyObservation;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.ImprovementAction;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.ObjectiveRevision;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.SiadapInterimCompetencyObservationEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.SiadapInterimFeedbackEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.SiadapInterimImprovementActionEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.SiadapInterimObjectiveRevisionEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class SiadapInterimFeedbackMapper {

    public SiadapInterimFeedback toDomain(SiadapInterimFeedbackEntity entity,
                                          List<SiadapInterimCompetencyObservationEntity> competencyEntities,
                                          List<SiadapInterimImprovementActionEntity> actionEntities,
                                          List<SiadapInterimObjectiveRevisionEntity> revisionEntities) {
        if (entity == null) return null;

        List<CompetencyObservation> competencies = competencyEntities != null ? competencyEntities.stream()
                .map(e -> CompetencyObservation.reconstruct(e.getCompetencyCode(), e.getObservedEvidence()))
                .collect(Collectors.toList()) : new ArrayList<>();

        List<ImprovementAction> actions = actionEntities != null ? actionEntities.stream()
                .map(e -> ImprovementAction.reconstruct(e.getActionAgreed(), e.getResponsible(), e.getDeadline(), e.getNeededSupport()))
                .collect(Collectors.toList()) : new ArrayList<>();

        List<ObjectiveRevision> revisions = revisionEntities != null ? revisionEntities.stream()
                .map(e -> ObjectiveRevision.reconstruct(e.getCurrentObjectiveText(), e.getRevisionJustification(), e.getNewObjectiveSmart(), e.getApprovalStatus()))
                .collect(Collectors.toList()) : new ArrayList<>();

        return SiadapInterimFeedback.reconstruct(
                entity.getEvaluationId(),
                entity.getObjectivesSynthesis(),
                entity.getObservedFactsStar(),
                entity.getDifficultiesObstacles(),
                entity.getFeedbackAndAction(),
                competencies,
                actions,
                revisions
        );
    }

    public SiadapInterimFeedbackEntity toEntity(SiadapInterimFeedback domain) {
        if (domain == null) return null;

        SiadapInterimFeedbackEntity entity = new SiadapInterimFeedbackEntity();
        entity.setEvaluationId(domain.getEvaluationId());
        entity.setObjectivesSynthesis(domain.getObjectivesSynthesis());
        entity.setObservedFactsStar(domain.getObservedFactsStar());
        entity.setDifficultiesObstacles(domain.getDifficultiesObstacles());
        entity.setFeedbackAndAction(domain.getFeedbackAndAction());
        return entity;
    }

    public List<SiadapInterimCompetencyObservationEntity> toCompetencyEntities(SiadapInterimFeedback domain) {
        if (domain == null || domain.getCompetencyObservations() == null) return new ArrayList<>();
        UUID evalId = domain.getEvaluationId();
        return domain.getCompetencyObservations().stream().map(c -> {
            SiadapInterimCompetencyObservationEntity entity = new SiadapInterimCompetencyObservationEntity();
            entity.setId(UUID.randomUUID());
            entity.setEvaluationId(evalId);
            entity.setCompetencyCode(c.getCompetencyCode());
            entity.setObservedEvidence(c.getObservedEvidence());
            return entity;
        }).collect(Collectors.toList());
    }

    public List<SiadapInterimImprovementActionEntity> toActionEntities(SiadapInterimFeedback domain) {
        if (domain == null || domain.getImprovementActions() == null) return new ArrayList<>();
        UUID evalId = domain.getEvaluationId();
        return domain.getImprovementActions().stream().map(a -> {
            SiadapInterimImprovementActionEntity entity = new SiadapInterimImprovementActionEntity();
            entity.setId(UUID.randomUUID());
            entity.setEvaluationId(evalId);
            entity.setActionAgreed(a.getActionAgreed());
            entity.setResponsible(a.getResponsible());
            entity.setDeadline(a.getDeadline());
            entity.setNeededSupport(a.getNeededSupport());
            return entity;
        }).collect(Collectors.toList());
    }

    public List<SiadapInterimObjectiveRevisionEntity> toRevisionEntities(SiadapInterimFeedback domain) {
        if (domain == null || domain.getObjectiveRevisions() == null) return new ArrayList<>();
        UUID evalId = domain.getEvaluationId();
        return domain.getObjectiveRevisions().stream().map(r -> {
            SiadapInterimObjectiveRevisionEntity entity = new SiadapInterimObjectiveRevisionEntity();
            entity.setId(UUID.randomUUID());
            entity.setEvaluationId(evalId);
            entity.setCurrentObjectiveText(r.getCurrentObjectiveText());
            entity.setRevisionJustification(r.getRevisionJustification());
            entity.setNewObjectiveSmart(r.getNewObjectiveSmart());
            entity.setApprovalStatus(r.getApprovalStatus());
            return entity;
        }).collect(Collectors.toList());
    }

    public SiadapInterimFeedbackDTO toDto(SiadapInterimFeedback domain) {
        if (domain == null) return null;

        List<CompetencyObservationDTO> competencies = domain.getCompetencyObservations().stream()
                .map(c -> new CompetencyObservationDTO(c.getCompetencyCode(), c.getObservedEvidence()))
                .collect(Collectors.toList());

        List<ImprovementActionDTO> actions = domain.getImprovementActions().stream()
                .map(a -> new ImprovementActionDTO(a.getActionAgreed(), a.getResponsible(), a.getDeadline(), a.getNeededSupport()))
                .collect(Collectors.toList());

        List<ObjectiveRevisionDTO> revisions = domain.getObjectiveRevisions().stream()
                .map(r -> new ObjectiveRevisionDTO(r.getCurrentObjectiveText(), r.getRevisionJustification(), r.getNewObjectiveSmart(), r.getApprovalStatus()))
                .collect(Collectors.toList());

        return new SiadapInterimFeedbackDTO(
                domain.getEvaluationId().toString(),
                domain.getObjectivesSynthesis(),
                domain.getObservedFactsStar(),
                domain.getDifficultiesObstacles(),
                domain.getFeedbackAndAction(),
                competencies,
                actions,
                revisions
        );
    }

    public SiadapInterimFeedback toDomain(SiadapInterimFeedbackDTO dto) {
        if (dto == null) return null;

        UUID evalId = UUID.fromString(dto.getEvaluationId());

        List<CompetencyObservation> competencies = dto.getCompetencyObservations() != null ? dto.getCompetencyObservations().stream()
                .map(c -> CompetencyObservation.create(c.getCompetencyCode(), c.getObservedEvidence()))
                .collect(Collectors.toList()) : new ArrayList<>();

        List<ImprovementAction> actions = dto.getImprovementActions() != null ? dto.getImprovementActions().stream()
                .map(a -> ImprovementAction.create(a.getActionAgreed(), a.getResponsible(), a.getDeadline(), a.getNeededSupport()))
                .collect(Collectors.toList()) : new ArrayList<>();

        List<ObjectiveRevision> revisions = dto.getObjectiveRevisions() != null ? dto.getObjectiveRevisions().stream()
                .map(r -> ObjectiveRevision.create(r.getCurrentObjectiveText(), r.getRevisionJustification(), r.getNewObjectiveSmart(), r.getApprovalStatus()))
                .collect(Collectors.toList()) : new ArrayList<>();

        return SiadapInterimFeedback.create(
                evalId,
                dto.getObjectivesSynthesis(),
                dto.getObservedFactsStar(),
                dto.getDifficultiesObstacles(),
                dto.getFeedbackAndAction(),
                competencies,
                actions,
                revisions
        );
    }
}
