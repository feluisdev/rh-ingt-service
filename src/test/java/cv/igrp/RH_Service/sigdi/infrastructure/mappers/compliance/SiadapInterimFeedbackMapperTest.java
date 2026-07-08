package cv.igrp.RH_Service.sigdi.infrastructure.mappers.compliance;

import cv.igrp.RH_Service.sigdi.application.dto.ObjectiveRevisionDTO;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapInterimFeedbackDTO;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapInterimFeedback;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.ObjectiveRevision;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.SiadapInterimFeedbackEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.SiadapInterimObjectiveRevisionEntity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link SiadapInterimFeedbackMapper}, focused on the {@code objectiveCode}
 * passenger field added to {@link ObjectiveRevision} in phase 61 (FBINT-01). Covers the round
 * trip through all 4 mapping directions plus a null/legacy-row case (pre-existing revisions
 * saved before this phase, where objectiveCode is null throughout).
 */
class SiadapInterimFeedbackMapperTest {

    private final SiadapInterimFeedbackMapper mapper = new SiadapInterimFeedbackMapper();

    @Test
    void toRevisionEntities_threadsObjectiveCode() {
        ObjectiveRevision revision = ObjectiveRevision.create(
                "Texto", "Justificação", "Novo SMART", "APPROVED", "OBJ-1");
        SiadapInterimFeedback domain = SiadapInterimFeedback.create(
                UUID.randomUUID(), "synthesis", "facts", "difficulties", "feedback",
                List.of(), List.of(), List.of(revision));

        List<SiadapInterimObjectiveRevisionEntity> entities = mapper.toRevisionEntities(domain);

        assertEquals("OBJ-1", entities.get(0).getObjectiveCode());
    }

    @Test
    void toDomain_fromEntity_threadsObjectiveCode() {
        SiadapInterimFeedbackEntity feedbackEntity = new SiadapInterimFeedbackEntity();
        feedbackEntity.setEvaluationId(UUID.randomUUID());
        feedbackEntity.setObjectivesSynthesis("synthesis");
        feedbackEntity.setObservedFactsStar("facts");
        feedbackEntity.setDifficultiesObstacles("difficulties");
        feedbackEntity.setFeedbackAndAction("feedback");

        SiadapInterimObjectiveRevisionEntity revisionEntity = new SiadapInterimObjectiveRevisionEntity();
        revisionEntity.setId(UUID.randomUUID());
        revisionEntity.setEvaluationId(feedbackEntity.getEvaluationId());
        revisionEntity.setCurrentObjectiveText("Texto");
        revisionEntity.setRevisionJustification("Justificação");
        revisionEntity.setNewObjectiveSmart("Novo SMART");
        revisionEntity.setApprovalStatus("APPROVED");
        revisionEntity.setObjectiveCode("OBJ-2");

        SiadapInterimFeedback domain = mapper.toDomain(
                feedbackEntity, List.of(), List.of(), List.of(revisionEntity));

        assertEquals("OBJ-2", domain.getObjectiveRevisions().get(0).getObjectiveCode());
    }

    @Test
    void toDto_threadsObjectiveCode() {
        ObjectiveRevision revision = ObjectiveRevision.create(
                "Texto", "Justificação", "Novo SMART", "APPROVED", "OBJ-3");
        SiadapInterimFeedback domain = SiadapInterimFeedback.create(
                UUID.randomUUID(), "synthesis", "facts", "difficulties", "feedback",
                List.of(), List.of(), List.of(revision));

        SiadapInterimFeedbackDTO dto = mapper.toDto(domain);

        assertEquals("OBJ-3", dto.getObjectiveRevisions().get(0).getObjectiveCode());
    }

    @Test
    void toDomain_fromDto_threadsObjectiveCode() {
        ObjectiveRevisionDTO revisionDto = new ObjectiveRevisionDTO(
                "Texto", "Justificação", "Novo SMART", "APPROVED", "OBJ-4");
        SiadapInterimFeedbackDTO dto = new SiadapInterimFeedbackDTO(
                UUID.randomUUID().toString(), "synthesis", "facts", "difficulties", "feedback",
                List.of(), List.of(), List.of(revisionDto));

        SiadapInterimFeedback domain = mapper.toDomain(dto);

        assertEquals("OBJ-4", domain.getObjectiveRevisions().get(0).getObjectiveCode());
    }

    @Test
    void nullObjectiveCode_roundTripsWithoutError() {
        // Legacy row: objectiveCode was never persisted before this phase, so it is null
        // throughout entity, domain, and DTO. Must round-trip through all 4 directions
        // without throwing, staying null at every stage.
        SiadapInterimObjectiveRevisionEntity revisionEntity = new SiadapInterimObjectiveRevisionEntity();
        revisionEntity.setId(UUID.randomUUID());
        revisionEntity.setEvaluationId(UUID.randomUUID());
        revisionEntity.setCurrentObjectiveText("Texto legado");
        revisionEntity.setRevisionJustification("Justificação legada");
        revisionEntity.setNewObjectiveSmart("SMART legado");
        revisionEntity.setApprovalStatus("APPROVED");
        revisionEntity.setObjectiveCode(null);

        SiadapInterimFeedbackEntity feedbackEntity = new SiadapInterimFeedbackEntity();
        feedbackEntity.setEvaluationId(revisionEntity.getEvaluationId());
        feedbackEntity.setObjectivesSynthesis("synthesis");
        feedbackEntity.setObservedFactsStar("facts");
        feedbackEntity.setDifficultiesObstacles("difficulties");
        feedbackEntity.setFeedbackAndAction("feedback");

        SiadapInterimFeedback domainFromEntity = mapper.toDomain(
                feedbackEntity, List.of(), List.of(), List.of(revisionEntity));
        assertNull(domainFromEntity.getObjectiveRevisions().get(0).getObjectiveCode());

        List<SiadapInterimObjectiveRevisionEntity> roundTrippedEntities =
                mapper.toRevisionEntities(domainFromEntity);
        assertNull(roundTrippedEntities.get(0).getObjectiveCode());

        SiadapInterimFeedbackDTO dto = mapper.toDto(domainFromEntity);
        assertNull(dto.getObjectiveRevisions().get(0).getObjectiveCode());

        SiadapInterimFeedback domainFromDto = mapper.toDomain(dto);
        assertNull(domainFromDto.getObjectiveRevisions().get(0).getObjectiveCode());
    }
}
