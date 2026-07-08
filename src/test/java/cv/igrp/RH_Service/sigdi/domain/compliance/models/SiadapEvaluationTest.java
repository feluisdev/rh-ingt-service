package cv.igrp.RH_Service.sigdi.domain.compliance.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
                .negotiateObjectives();

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

        SiadapEvaluation negotiating = proposed.negotiateObjectives();

        assertEquals(AcceptanceStatus.NEGOTIATING, negotiating.getAcceptanceStatus());
        assertEquals(EvaluationPhase.OPEN, negotiating.getPhase());
    }

    @Test
    void negotiate_fromAccepted_throws() {
        SiadapEvaluation accepted = buildOpenEvaluation()
                .contractualizeObjectives(buildValidObjectives())
                .acceptObjectives();

        assertThrows(IgrpResponseStatusException.class, accepted::negotiateObjectives);
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
}
