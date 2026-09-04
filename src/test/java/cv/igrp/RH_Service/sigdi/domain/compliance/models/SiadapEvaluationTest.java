package cv.igrp.RH_Service.sigdi.domain.compliance.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.AcceptanceStatus;
import cv.igrp.RH_Service.sigdi.application.constants.CompetencyCategory;
import cv.igrp.RH_Service.sigdi.application.constants.EvaluationPhase;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.CompetencyItem;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.IndividualObjective;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

/**
 * Testes unitários do agregado {@link SiadapEvaluation}, cobrindo a máquina de estados de
 * aceitação de objetivos (dupla-aceitação avaliador-avaliado) e o seu acoplamento com a
 * fase da avaliação (CONTRACT-01..04).
 */
class SiadapEvaluationTest {

    private static final Integer YEAR = 2026;

    private SiadapEvaluation buildOpenEvaluation() {
        return SiadapEvaluation.create(
                UUID.randomUUID().toString(),
                YEAR,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                new BigDecimal("60"),
                new BigDecimal("40"));
    }

    private List<IndividualObjective> buildValidObjectives() {
        return List.of(
                IndividualObjective.create("OBJ-1", "Descrição do objetivo 1", "Indicador 1",
                        new BigDecimal("100"), new BigDecimal("40")),
                IndividualObjective.create("OBJ-2", "Descrição do objetivo 2", "Indicador 2",
                        new BigDecimal("100"), new BigDecimal("30")),
                IndividualObjective.create("OBJ-3", "Descrição do objetivo 3", "Indicador 3",
                        new BigDecimal("100"), new BigDecimal("30")));
    }

    @Test
    void propose_setsPendingAndKeepsOpen() {
        SiadapEvaluation evaluation = buildOpenEvaluation();

        SiadapEvaluation proposed = evaluation.contractualizeObjectives(buildValidObjectives());

        assertEquals(AcceptanceStatus.PENDING_ACCEPTANCE, proposed.getAcceptanceStatus());
        assertEquals(EvaluationPhase.OPEN, proposed.getPhase());
    }

    @Test
    void propose_rejectedWhenNotOpen() {
        SiadapEvaluation evaluation = buildOpenEvaluation()
                .contractualizeObjectives(buildValidObjectives())
                .acceptObjectives();

        assertThrows(IgrpResponseStatusException.class,
                () -> evaluation.contractualizeObjectives(buildValidObjectives()));
    }

    @Test
    void propose_rejectedWhenPendingAcceptance() {
        SiadapEvaluation evaluation = buildOpenEvaluation()
                .contractualizeObjectives(buildValidObjectives());

        assertThrows(IgrpResponseStatusException.class,
                () -> evaluation.contractualizeObjectives(buildValidObjectives()));
    }

    @Test
    void accept_fromPending_advancesPhase() {
        SiadapEvaluation proposed = buildOpenEvaluation().contractualizeObjectives(buildValidObjectives());

        SiadapEvaluation accepted = proposed.acceptObjectives();

        assertEquals(AcceptanceStatus.ACCEPTED, accepted.getAcceptanceStatus());
        assertEquals(EvaluationPhase.IN_PROGRESS, accepted.getPhase());
    }

    @Test
    void accept_fromNegotiating_advancesPhase() {
        SiadapEvaluation negotiating = buildOpenEvaluation()
                .contractualizeObjectives(buildValidObjectives())
                .negotiateObjectives("Gostaria de rever o peso do objetivo 1");

        SiadapEvaluation accepted = negotiating.acceptObjectives();

        assertEquals(AcceptanceStatus.ACCEPTED, accepted.getAcceptanceStatus());
        assertEquals(EvaluationPhase.IN_PROGRESS, accepted.getPhase());
    }

    @Test
    void accept_fromAbsent_throws() {
        SiadapEvaluation freshEvaluation = buildOpenEvaluation();

        assertThrows(IgrpResponseStatusException.class, freshEvaluation::acceptObjectives);
    }

    @Test
    void negotiate_fromPending_keepsOpen() {
        SiadapEvaluation proposed = buildOpenEvaluation().contractualizeObjectives(buildValidObjectives());

        SiadapEvaluation negotiating = proposed.negotiateObjectives("Peso do objetivo 2 parece desajustado");

        assertEquals(AcceptanceStatus.NEGOTIATING, negotiating.getAcceptanceStatus());
        assertEquals(EvaluationPhase.OPEN, negotiating.getPhase());
    }

    // WR-02: the avaliado's negotiation comment must be persisted on the aggregate so the
    // avaliador can see why negotiation was requested when reopening the evaluation.
    @Test
    void negotiate_persistsLastNegotiationComment() {
        SiadapEvaluation proposed = buildOpenEvaluation().contractualizeObjectives(buildValidObjectives());

        SiadapEvaluation negotiating = proposed.negotiateObjectives("Peso do objetivo 2 parece desajustado");

        assertEquals("Peso do objetivo 2 parece desajustado", negotiating.getLastNegotiationComment());
    }

    @Test
    void negotiate_acceptsNullComment() {
        SiadapEvaluation proposed = buildOpenEvaluation().contractualizeObjectives(buildValidObjectives());

        SiadapEvaluation negotiating = proposed.negotiateObjectives(null);

        assertEquals(AcceptanceStatus.NEGOTIATING, negotiating.getAcceptanceStatus());
        assertEquals(null, negotiating.getLastNegotiationComment());
    }

    @Test
    void negotiate_fromAccepted_throws() {
        SiadapEvaluation accepted = buildOpenEvaluation()
                .contractualizeObjectives(buildValidObjectives())
                .acceptObjectives();

        assertThrows(IgrpResponseStatusException.class, () -> accepted.negotiateObjectives("qualquer comentário"));
    }

    @Test
    void unrelatedTransition_preservesAcceptance() {
        SiadapEvaluation accepted = buildOpenEvaluation()
                .contractualizeObjectives(buildValidObjectives())
                .acceptObjectives();

        SiadapEvaluation withCompetencies = accepted.setCompetencies(
                List.of(CompetencyItem.create("COMP-1", "Orientação para o Serviço Público",
                        CompetencyCategory.BEHAVIORAL)));

        SiadapEvaluation evaluated = withCompetencies.evaluateCompetency("COMP-1", 4);

        assertEquals(AcceptanceStatus.ACCEPTED, evaluated.getAcceptanceStatus());
        assertEquals(EvaluationPhase.IN_PROGRESS, evaluated.getPhase());
    }

    @Test
    void applyObjectiveRevision_replacesOnlyDescription_preservesOtherFields() {
        SiadapEvaluation inProgress = buildOpenEvaluation()
                .contractualizeObjectives(buildValidObjectives())
                .acceptObjectives();

        SiadapEvaluation revised = inProgress.applyObjectiveRevision("OBJ-1", "Nova descrição revista");

        IndividualObjective obj1 = revised.getObjectives().stream()
                .filter(o -> "OBJ-1".equals(o.getCode())).findFirst().orElseThrow();
        assertEquals("Nova descrição revista", obj1.getDescription());
        assertEquals("Indicador 1", obj1.getIndicator());
        assertEquals(new BigDecimal("100"), obj1.getTargetValue());
        assertEquals(new BigDecimal("40"), obj1.getWeight());
        assertEquals(null, obj1.getAchievedValue());
        assertEquals(null, obj1.getScore());

        IndividualObjective obj2 = revised.getObjectives().stream()
                .filter(o -> "OBJ-2".equals(o.getCode())).findFirst().orElseThrow();
        assertEquals("Descrição do objetivo 2", obj2.getDescription());

        IndividualObjective obj3 = revised.getObjectives().stream()
                .filter(o -> "OBJ-3".equals(o.getCode())).findFirst().orElseThrow();
        assertEquals("Descrição do objetivo 3", obj3.getDescription());
    }

    @Test
    void applyObjectiveRevision_rejectedWhenCodeNotFound() {
        SiadapEvaluation inProgress = buildOpenEvaluation()
                .contractualizeObjectives(buildValidObjectives())
                .acceptObjectives();

        assertThrows(IgrpResponseStatusException.class,
                () -> inProgress.applyObjectiveRevision("OBJ-999", "Nova descrição revista"));
    }

    @Test
    void applyObjectiveRevision_rejectedWhenPhaseNotEligible() {
        SiadapEvaluation open = buildOpenEvaluation().contractualizeObjectives(buildValidObjectives());

        assertThrows(IgrpResponseStatusException.class,
                () -> open.applyObjectiveRevision("OBJ-1", "Nova descrição revista"));
    }

    // ============================================================
    // SIA-03 -- aceitação tácita da autoavaliação (fim da janela)
    // ============================================================

    private SiadapEvaluation buildSelfEvaluationPhaseEvaluation() {
        return buildOpenEvaluation()
                .contractualizeObjectives(buildValidObjectives())
                .acceptObjectives()
                .openSelfEvaluationPhase();
    }

    @Test
    void applyTacitSelfEvaluationAcceptance_fromSelfEvaluation_advancesToManagerEvaluationWithoutScore() {
        SiadapEvaluation selfEvaluation = buildSelfEvaluationPhaseEvaluation();

        SiadapEvaluation tacitlyAccepted = selfEvaluation.applyTacitSelfEvaluationAcceptance();

        assertEquals(EvaluationPhase.MANAGER_EVALUATION, tacitlyAccepted.getPhase());
        assertEquals(null, tacitlyAccepted.getSelfEvaluationScore());
        assertTrue(tacitlyAccepted.isSelfEvaluationTacitlyAccepted());
    }

    @Test
    void applyTacitSelfEvaluationAcceptance_rejectedWhenNotSelfEvaluation() {
        SiadapEvaluation open = buildOpenEvaluation();
        assertThrows(IgrpResponseStatusException.class, open::applyTacitSelfEvaluationAcceptance);

        SiadapEvaluation managerEvaluation = buildSelfEvaluationPhaseEvaluation().applyTacitSelfEvaluationAcceptance();
        // Idempotência: uma segunda invocação sobre o resultado da primeira também lança --
        // é do que o agendador do Plano 03 depende para não reprocessar a mesma avaliação.
        assertThrows(IgrpResponseStatusException.class, managerEvaluation::applyTacitSelfEvaluationAcceptance);
    }

    @Test
    void applyTacitSelfEvaluationAcceptance_leavesAcceptanceStatusUntouched() {
        SiadapEvaluation selfEvaluation = buildSelfEvaluationPhaseEvaluation();

        SiadapEvaluation tacitlyAccepted = selfEvaluation.applyTacitSelfEvaluationAcceptance();

        assertEquals(selfEvaluation.getAcceptanceStatus(), tacitlyAccepted.getAcceptanceStatus());
    }

    @Test
    void submitSelfEvaluation_stillRequiresPhaseAndScore() {
        SiadapEvaluation selfEvaluation = buildSelfEvaluationPhaseEvaluation();

        assertThrows(IgrpResponseStatusException.class, () -> selfEvaluation.submitSelfEvaluation(null));
        assertThrows(IgrpResponseStatusException.class, () -> selfEvaluation.submitSelfEvaluation(BigDecimal.ZERO));
        assertThrows(IgrpResponseStatusException.class, () -> selfEvaluation.submitSelfEvaluation(new BigDecimal("6")));

        SiadapEvaluation open = buildOpenEvaluation();
        assertThrows(IgrpResponseStatusException.class, () -> open.submitSelfEvaluation(new BigDecimal("4")));

        SiadapEvaluation submitted = selfEvaluation.submitSelfEvaluation(new BigDecimal("4"));
        assertEquals(new BigDecimal("4"), submitted.getSelfEvaluationScore());
        assertEquals(false, submitted.isSelfEvaluationTacitlyAccepted());
    }

    @Test
    void tacitlyAcceptedEvaluation_reachesHarmonizationWithoutSelfEvaluationScore() {
        SiadapEvaluation tacitlyAccepted = buildSelfEvaluationPhaseEvaluation()
                .applyTacitSelfEvaluationAcceptance();

        SiadapEvaluation withAchievements = tacitlyAccepted
                .recordObjectiveAchievement("OBJ-1", new BigDecimal("100"), 3)
                .recordObjectiveAchievement("OBJ-2", new BigDecimal("100"), 3)
                .recordObjectiveAchievement("OBJ-3", new BigDecimal("100"), 3);

        SiadapEvaluation withCompetencies = withAchievements.setCompetencies(
                List.of(CompetencyItem.create("COMP-1", "Orientação para o Serviço Público",
                        CompetencyCategory.BEHAVIORAL)));

        SiadapEvaluation evaluated = withCompetencies.evaluateCompetency("COMP-1", 4);

        SiadapEvaluation finalized = evaluated.finalizeEvaluation();

        assertEquals(EvaluationPhase.HARMONIZATION, finalized.getPhase());
        assertNotNull(finalized.getFinalScore());
        assertEquals(null, finalized.getSelfEvaluationScore());
        assertTrue(finalized.isSelfEvaluationTacitlyAccepted());
    }
}
