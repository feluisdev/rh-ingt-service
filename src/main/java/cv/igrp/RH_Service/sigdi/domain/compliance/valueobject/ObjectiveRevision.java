package cv.igrp.RH_Service.sigdi.domain.compliance.valueobject;

import cv.igrp.RH_Service.sigdi.application.constants.AcceptanceStatus;
import lombok.Getter;

import java.util.UUID;

/**
 * Value Object representando a revisão de um objetivo no feedback intercalar.
 */
@Getter
public class ObjectiveRevision {

    private final UUID id;
    private final String currentObjectiveText;
    private final String revisionJustification;
    private final String newObjectiveSmart;
    private final AcceptanceStatus approvalStatus;
    private final String objectiveCode;
    private final String lastNegotiationComment;

    private ObjectiveRevision(UUID id, String currentObjectiveText, String revisionJustification, String newObjectiveSmart, AcceptanceStatus approvalStatus, String objectiveCode, String lastNegotiationComment) {
        this.id = id != null ? id : UUID.randomUUID();
        this.currentObjectiveText = currentObjectiveText;
        this.revisionJustification = revisionJustification;
        this.newObjectiveSmart = newObjectiveSmart;
        this.approvalStatus = approvalStatus;
        this.objectiveCode = objectiveCode;
        this.lastNegotiationComment = lastNegotiationComment;
    }

    /**
     * Cria uma revisão nova (id nunca vem do cliente — se null, é gerado agora, no primeiro save).
     */
    public static ObjectiveRevision create(UUID id, String currentObjectiveText, String revisionJustification, String newObjectiveSmart, AcceptanceStatus approvalStatus, String objectiveCode, String lastNegotiationComment) {
        return new ObjectiveRevision(id, currentObjectiveText, revisionJustification, newObjectiveSmart, approvalStatus, objectiveCode, lastNegotiationComment);
    }

    /**
     * Reconstrói a partir da persistência — o id vem sempre da PK da linha persistida, nunca null.
     */
    public static ObjectiveRevision reconstruct(UUID id, String currentObjectiveText, String revisionJustification, String newObjectiveSmart, AcceptanceStatus approvalStatus, String objectiveCode, String lastNegotiationComment) {
        if (id == null) throw new IllegalArgumentException("id é obrigatório ao reconstruir a partir da persistência");
        return new ObjectiveRevision(id, currentObjectiveText, revisionJustification, newObjectiveSmart, approvalStatus, objectiveCode, lastNegotiationComment);
    }

    /** Transição: proposta enviada, aguarda aceitação do avaliado. */
    public ObjectiveRevision withProposed() {
        return new ObjectiveRevision(this.id, this.currentObjectiveText, this.revisionJustification, this.newObjectiveSmart,
                AcceptanceStatus.PENDING_ACCEPTANCE, this.objectiveCode, this.lastNegotiationComment);
    }

    /** Transição: proposta aceite. */
    public ObjectiveRevision withAccepted() {
        return new ObjectiveRevision(this.id, this.currentObjectiveText, this.revisionJustification, this.newObjectiveSmart,
                AcceptanceStatus.ACCEPTED, this.objectiveCode, this.lastNegotiationComment);
    }

    /** Transição: em negociação, com o comentário do avaliado. */
    public ObjectiveRevision withNegotiated(String comment) {
        return new ObjectiveRevision(this.id, this.currentObjectiveText, this.revisionJustification, this.newObjectiveSmart,
                AcceptanceStatus.NEGOTIATING, this.objectiveCode, comment);
    }
}
