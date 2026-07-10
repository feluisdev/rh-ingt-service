package cv.igrp.RH_Service.sigdi.infrastructure.mappers.compliance;

import cv.igrp.RH_Service.sigdi.application.constants.AcceptanceStatus;
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
 * passenger field added to {@link ObjectiveRevision} in phase 61 (FBINT-01), plus the phase 62
 * (RECONC-01/02/04) id-stability and blank-tolerant-approvalStatus fixes. Covers the round trip
 * through all 4 mapping directions plus a null/legacy-row case (pre-existing revisions saved
 * before phase 61, where objectiveCode is null throughout).
 */
class SiadapInterimFeedbackMapperTest {

    private final SiadapInterimFeedbackMapper mapper = new SiadapInterimFeedbackMapper();

    @Test
    void toRevisionEntities_threadsObjectiveCode() {
        ObjectiveRevision revision = ObjectiveRevision.create(
                null, "Texto", "Justificação", "Novo SMART", AcceptanceStatus.PENDING_ACCEPTANCE, "OBJ-1", null);
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
        revisionEntity.setApprovalStatus("PENDING_ACCEPTANCE");
        revisionEntity.setObjectiveCode("OBJ-2");

        SiadapInterimFeedback domain = mapper.toDomain(
                feedbackEntity, List.of(), List.of(), List.of(revisionEntity));

        assertEquals("OBJ-2", domain.getObjectiveRevisions().get(0).getObjectiveCode());
    }

    @Test
    void toDto_threadsObjectiveCode() {
        ObjectiveRevision revision = ObjectiveRevision.create(
                null, "Texto", "Justificação", "Novo SMART", AcceptanceStatus.PENDING_ACCEPTANCE, "OBJ-3", null);
        SiadapInterimFeedback domain = SiadapInterimFeedback.create(
                UUID.randomUUID(), "synthesis", "facts", "difficulties", "feedback",
                List.of(), List.of(), List.of(revision));

        SiadapInterimFeedbackDTO dto = mapper.toDto(domain);

        assertEquals("OBJ-3", dto.getObjectiveRevisions().get(0).getObjectiveCode());
    }

    @Test
    void toDomain_fromDto_threadsObjectiveCode() {
        ObjectiveRevisionDTO revisionDto = new ObjectiveRevisionDTO(
                null, "Texto", "Justificação", "Novo SMART", "PENDING_ACCEPTANCE", "OBJ-4", null);
        SiadapInterimFeedbackDTO dto = new SiadapInterimFeedbackDTO(
                UUID.randomUUID().toString(), "synthesis", "facts", "difficulties", "feedback",
                List.of(), List.of(), List.of(revisionDto));

        SiadapInterimFeedback domain = mapper.toDomain(dto);

        assertEquals("OBJ-4", domain.getObjectiveRevisions().get(0).getObjectiveCode());
    }

    @Test
    void nullObjectiveCode_roundTripsWithoutError() {
        // Legacy row: objectiveCode was never persisted before phase 61, so it is null
        // throughout entity, domain, and DTO. Must round-trip through all 4 directions
        // without throwing, staying null at every stage.
        SiadapInterimObjectiveRevisionEntity revisionEntity = new SiadapInterimObjectiveRevisionEntity();
        revisionEntity.setId(UUID.randomUUID());
        revisionEntity.setEvaluationId(UUID.randomUUID());
        revisionEntity.setCurrentObjectiveText("Texto legado");
        revisionEntity.setRevisionJustification("Justificação legada");
        revisionEntity.setNewObjectiveSmart("SMART legado");
        revisionEntity.setApprovalStatus("PENDING_ACCEPTANCE");
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

    @Test
    void idSurvivesRoundTrip_acrossTwoConsecutiveSaves() {
        // Brand-new draft row: no id supplied, the VO constructor mints one on first save.
        ObjectiveRevision revision = ObjectiveRevision.create(
                null, "Texto", "Justificação", "Novo SMART", null, "OBJ-5", null);
        SiadapInterimFeedback domain = SiadapInterimFeedback.create(
                UUID.randomUUID(), "synthesis", "facts", "difficulties", "feedback",
                List.of(), List.of(), List.of(revision));

        // First "save": mapper generates/threads the entity id.
        List<SiadapInterimObjectiveRevisionEntity> firstEntities = mapper.toRevisionEntities(domain);
        UUID firstId = firstEntities.get(0).getId();
        assertEquals(revision.getId(), firstId);

        SiadapInterimFeedbackEntity feedbackEntity = new SiadapInterimFeedbackEntity();
        feedbackEntity.setEvaluationId(domain.getEvaluationId());
        feedbackEntity.setObjectivesSynthesis("synthesis");
        feedbackEntity.setObservedFactsStar("facts");
        feedbackEntity.setDifficultiesObstacles("difficulties");
        feedbackEntity.setFeedbackAndAction("feedback");

        // Rebuild domain FROM the persisted entity, then "save" again.
        SiadapInterimFeedback domainFromEntity = mapper.toDomain(
                feedbackEntity, List.of(), List.of(), List.of(firstEntities.get(0)));
        List<SiadapInterimObjectiveRevisionEntity> secondEntities = mapper.toRevisionEntities(domainFromEntity);
        UUID secondId = secondEntities.get(0).getId();

        assertEquals(firstId, secondId, "id must be stable across repeated save-equivalent mapper calls, not regenerated");
    }

    @Test
    void blankApprovalStatus_roundTripsToNullWithoutThrowing() {
        // Legacy draft rows were persisted with approvalStatus = "" (not null) by the shipped
        // frontend. Must map to a null domain approvalStatus without throwing, and stay null
        // throughout all 4 mapping directions.
        SiadapInterimObjectiveRevisionEntity revisionEntity = new SiadapInterimObjectiveRevisionEntity();
        revisionEntity.setId(UUID.randomUUID());
        revisionEntity.setEvaluationId(UUID.randomUUID());
        revisionEntity.setCurrentObjectiveText("Texto legado");
        revisionEntity.setRevisionJustification("Justificação legada");
        revisionEntity.setNewObjectiveSmart("SMART legado");
        revisionEntity.setApprovalStatus("");
        revisionEntity.setObjectiveCode("OBJ-6");

        SiadapInterimFeedbackEntity feedbackEntity = new SiadapInterimFeedbackEntity();
        feedbackEntity.setEvaluationId(revisionEntity.getEvaluationId());
        feedbackEntity.setObjectivesSynthesis("synthesis");
        feedbackEntity.setObservedFactsStar("facts");
        feedbackEntity.setDifficultiesObstacles("difficulties");
        feedbackEntity.setFeedbackAndAction("feedback");

        SiadapInterimFeedback domainFromEntity = mapper.toDomain(
                feedbackEntity, List.of(), List.of(), List.of(revisionEntity));
        assertNull(domainFromEntity.getObjectiveRevisions().get(0).getApprovalStatus());

        List<SiadapInterimObjectiveRevisionEntity> roundTrippedEntities =
                mapper.toRevisionEntities(domainFromEntity);
        assertNull(roundTrippedEntities.get(0).getApprovalStatus());

        SiadapInterimFeedbackDTO dto = mapper.toDto(domainFromEntity);
        assertNull(dto.getObjectiveRevisions().get(0).getApprovalStatus());

        SiadapInterimFeedback domainFromDto = mapper.toDomain(dto);
        assertNull(domainFromDto.getObjectiveRevisions().get(0).getApprovalStatus());
    }
}
