package cv.igrp.RH_Service.sigdi.domain.tatical.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.Budget;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.DateRange;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

/**
 * Testes unitários do agregado {@link TacticalActivity}, cobrindo a guarda de orçamento em
 * {@code update()} que impede a alteração de orçamento numa atividade APPROVED (PAA-02).
 */
class TacticalActivityTest {

    private static final StrategicGoalId STRATEGIC_GOAL_ID = StrategicGoalId.from(UUID.randomUUID());
    private static final DateRange DATE_RANGE =
            DateRange.of(LocalDate.now(), LocalDate.now().plusDays(30));

    private TacticalActivity buildApprovedActivityWithBudget(Budget budget) {
        TacticalActivity draft = TacticalActivity.create(
                UUID.randomUUID(),
                STRATEGIC_GOAL_ID,
                UUID.randomUUID(),
                "Atividade tática aprovada",
                "Descrição",
                "Justificação",
                "Localização",
                null,
                "Metodologia",
                DATE_RANGE,
                budget,
                PaaLevel.UNIT_LEVEL);

        return draft.submit().approve().approve();
    }

    @Test
    void update_rejectsBudgetChangeWhenApproved() {
        Budget originalBudget = Budget.of(new BigDecimal("1000"), "02.02.01");
        TacticalActivity approved = buildApprovedActivityWithBudget(originalBudget);

        Budget differentBudget = Budget.of(new BigDecimal("2000"), "02.02.01");

        assertThrows(IgrpResponseStatusException.class,
                () -> approved.update(STRATEGIC_GOAL_ID, approved.getOrganicUnitId(), approved.getTitle(),
                        approved.getDescriptionWhat(), approved.getJustificationWhy(), approved.getLocationWhere(),
                        approved.getResponsibleWho(), approved.getMethodologyHow(), DATE_RANGE, differentBudget));
    }

    @Test
    void update_allowsSameBudgetWhenApproved() {
        Budget originalBudget = Budget.of(new BigDecimal("1000"), "02.02.01");
        TacticalActivity approved = buildApprovedActivityWithBudget(originalBudget);

        // Mesmo valor, escala decimal diferente (1000 vs 1000.00) -- prova que a comparação
        // usa Budget.equals()/compareTo() e não identidade de referência nem BigDecimal.equals.
        Budget sameValueDifferentScale = Budget.of(new BigDecimal("1000.00"), "02.02.01");

        TacticalActivity updated = approved.update(STRATEGIC_GOAL_ID, approved.getOrganicUnitId(),
                approved.getTitle(), approved.getDescriptionWhat(), approved.getJustificationWhy(),
                approved.getLocationWhere(), approved.getResponsibleWho(), approved.getMethodologyHow(),
                DATE_RANGE, sameValueDifferentScale);

        assertEquals(sameValueDifferentScale, updated.getBudget());
    }

    @Test
    void update_allowsBudgetChangeWhenDraft() {
        Budget originalBudget = Budget.of(new BigDecimal("1000"), "02.02.01");
        TacticalActivity draft = TacticalActivity.create(
                UUID.randomUUID(),
                STRATEGIC_GOAL_ID,
                UUID.randomUUID(),
                "Atividade tática em rascunho",
                "Descrição",
                "Justificação",
                "Localização",
                null,
                "Metodologia",
                DATE_RANGE,
                originalBudget,
                PaaLevel.UNIT_LEVEL);

        Budget differentBudget = Budget.of(new BigDecimal("2000"), "02.02.01");

        TacticalActivity updated = draft.update(STRATEGIC_GOAL_ID, draft.getOrganicUnitId(), draft.getTitle(),
                draft.getDescriptionWhat(), draft.getJustificationWhy(), draft.getLocationWhere(),
                draft.getResponsibleWho(), draft.getMethodologyHow(), DATE_RANGE, differentBudget);

        assertEquals(differentBudget, updated.getBudget());
    }

    // Decisão do operador, 2026-08-20: um update() que OMITE o orçamento (budget == null)
    // significa "não mexer no orçamento", não "apagar o orçamento". Sem este teste, a
    // condição `budget != null` da guarda poderia ser removida por engano numa limpeza
    // futura sem que nenhum teste falhasse.
    @Test
    void update_allowsOmittedBudgetWhenApproved_preservesCurrentBudget() {
        Budget originalBudget = Budget.of(new BigDecimal("1000"), "02.02.01");
        TacticalActivity approved = buildApprovedActivityWithBudget(originalBudget);

        TacticalActivity updated = approved.update(STRATEGIC_GOAL_ID, approved.getOrganicUnitId(),
                "Título corrigido", approved.getDescriptionWhat(), approved.getJustificationWhy(),
                approved.getLocationWhere(), approved.getResponsibleWho(), approved.getMethodologyHow(),
                DATE_RANGE, null);

        assertEquals(originalBudget, updated.getBudget());
        assertEquals("Título corrigido", updated.getTitle());
    }
}
