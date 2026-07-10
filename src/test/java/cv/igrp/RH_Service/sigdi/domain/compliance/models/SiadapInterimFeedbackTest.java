package cv.igrp.RH_Service.sigdi.domain.compliance.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.AcceptanceStatus;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.ObjectiveRevision;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

/**
 * Testes unitários do agregado {@link SiadapInterimFeedback}, cobrindo as suas primeiras
 * métodos comportamentais — a máquina de estados de aceitação da revisão de objetivos
 * (RECONC-01/02): proposeRevision/acceptRevision/negotiateRevision.
 */
class SiadapInterimFeedbackTest {

    private ObjectiveRevision draftRevision() {
        return ObjectiveRevision.create(null, "Texto atual", "Justificação", "Novo SMART", null, "OBJ-1", null);
    }

    private SiadapInterimFeedback buildFeedback(ObjectiveRevision... revisions) {
        return SiadapInterimFeedback.create(UUID.randomUUID(), "synthesis", "facts", "difficulties", "feedback",
                List.of(), List.of(), List.of(revisions));
    }

    @Test
    void proposeRevision_fromDraft_setsPending() {
        ObjectiveRevision revision = draftRevision();
        SiadapInterimFeedback feedback = buildFeedback(revision);

        SiadapInterimFeedback proposed = feedback.proposeRevision(revision.getId());

        assertEquals(AcceptanceStatus.PENDING_ACCEPTANCE, proposed.getObjectiveRevisions().get(0).getApprovalStatus());
    }

    @Test
    void proposeRevision_rejectedWhenAlreadyProposed() {
        ObjectiveRevision revision = draftRevision();
        SiadapInterimFeedback proposed = buildFeedback(revision).proposeRevision(revision.getId());
        UUID revisionId = proposed.getObjectiveRevisions().get(0).getId();

        assertThrows(IgrpResponseStatusException.class, () -> proposed.proposeRevision(revisionId));
    }

    @Test
    void proposeRevision_rejectedWhenMissingObjectiveCodeOrSmart() {
        ObjectiveRevision noCode = ObjectiveRevision.create(null, "Texto atual", "Justificação", "Novo SMART", null, null, null);
        SiadapInterimFeedback feedback = buildFeedback(noCode);

        assertThrows(IgrpResponseStatusException.class, () -> feedback.proposeRevision(noCode.getId()));

        ObjectiveRevision noSmart = ObjectiveRevision.create(null, "Texto atual", "Justificação", null, null, "OBJ-1", null);
        SiadapInterimFeedback feedback2 = buildFeedback(noSmart);

        assertThrows(IgrpResponseStatusException.class, () -> feedback2.proposeRevision(noSmart.getId()));
    }

    @Test
    void proposeRevision_rejectedWhenNotFound() {
        SiadapInterimFeedback feedback = buildFeedback(draftRevision());
        UUID unknownId = UUID.randomUUID();

        assertThrows(IgrpResponseStatusException.class, () -> feedback.proposeRevision(unknownId));
    }

    @Test
    void acceptRevision_fromPending_setsAccepted() {
        ObjectiveRevision revision = draftRevision();
        SiadapInterimFeedback proposed = buildFeedback(revision).proposeRevision(revision.getId());
        UUID revisionId = proposed.getObjectiveRevisions().get(0).getId();

        SiadapInterimFeedback accepted = proposed.acceptRevision(revisionId);

        assertEquals(AcceptanceStatus.ACCEPTED, accepted.getObjectiveRevisions().get(0).getApprovalStatus());
    }

    @Test
    void acceptRevision_fromNegotiating_setsAccepted() {
        ObjectiveRevision revision = draftRevision();
        SiadapInterimFeedback proposed = buildFeedback(revision).proposeRevision(revision.getId());
        UUID revisionId = proposed.getObjectiveRevisions().get(0).getId();
        SiadapInterimFeedback negotiating = proposed.negotiateRevision(revisionId, "Discordo do prazo");

        SiadapInterimFeedback accepted = negotiating.acceptRevision(revisionId);

        assertEquals(AcceptanceStatus.ACCEPTED, accepted.getObjectiveRevisions().get(0).getApprovalStatus());
    }

    @Test
    void acceptRevision_rejectedFromDraft() {
        ObjectiveRevision revision = draftRevision();
        SiadapInterimFeedback feedback = buildFeedback(revision);

        assertThrows(IgrpResponseStatusException.class, () -> feedback.acceptRevision(revision.getId()));
    }

    @Test
    void negotiateRevision_fromPending_setsNegotiatingAndStoresComment() {
        ObjectiveRevision revision = draftRevision();
        SiadapInterimFeedback proposed = buildFeedback(revision).proposeRevision(revision.getId());
        UUID revisionId = proposed.getObjectiveRevisions().get(0).getId();

        SiadapInterimFeedback negotiating = proposed.negotiateRevision(revisionId, "Discordo do prazo");

        ObjectiveRevision updated = negotiating.getObjectiveRevisions().get(0);
        assertEquals(AcceptanceStatus.NEGOTIATING, updated.getApprovalStatus());
        assertEquals("Discordo do prazo", updated.getLastNegotiationComment());
    }

    @Test
    void negotiateRevision_rejectedWhenNotPending() {
        ObjectiveRevision revision = draftRevision();
        SiadapInterimFeedback feedback = buildFeedback(revision);

        assertThrows(IgrpResponseStatusException.class, () -> feedback.negotiateRevision(revision.getId(), "comentário"));

        SiadapInterimFeedback anotherFeedback = buildFeedback(draftRevision());
        ObjectiveRevision revision2 = anotherFeedback.getObjectiveRevisions().get(0);
        SiadapInterimFeedback proposed = anotherFeedback.proposeRevision(revision2.getId());
        UUID revisionId = proposed.getObjectiveRevisions().get(0).getId();
        SiadapInterimFeedback fullyAccepted = proposed.acceptRevision(revisionId);

        assertThrows(IgrpResponseStatusException.class, () -> fullyAccepted.negotiateRevision(revisionId, "comentário"));
    }

    @Test
    void otherRevisionsUnaffected_whenTransitioningOneOfTwo() {
        ObjectiveRevision revisionA = ObjectiveRevision.create(null, "Texto A", "Justificação A", "SMART A", null, "OBJ-A", null);
        ObjectiveRevision revisionB = ObjectiveRevision.create(null, "Texto B", "Justificação B", "SMART B", null, "OBJ-B", null);
        SiadapInterimFeedback feedback = buildFeedback(revisionA, revisionB);

        SiadapInterimFeedback proposed = feedback.proposeRevision(revisionA.getId());

        ObjectiveRevision resultA = proposed.getObjectiveRevisions().stream()
                .filter(r -> r.getId().equals(revisionA.getId())).findFirst().orElseThrow();
        ObjectiveRevision resultB = proposed.getObjectiveRevisions().stream()
                .filter(r -> r.getId().equals(revisionB.getId())).findFirst().orElseThrow();

        assertEquals(AcceptanceStatus.PENDING_ACCEPTANCE, resultA.getApprovalStatus());
        assertNull(resultB.getApprovalStatus());
        assertEquals("Texto B", resultB.getCurrentObjectiveText());
        assertEquals("Justificação B", resultB.getRevisionJustification());
        assertEquals("SMART B", resultB.getNewObjectiveSmart());
        assertEquals("OBJ-B", resultB.getObjectiveCode());
        assertNull(resultB.getLastNegotiationComment());
        assertEquals(revisionB.getId(), resultB.getId());
    }
}
