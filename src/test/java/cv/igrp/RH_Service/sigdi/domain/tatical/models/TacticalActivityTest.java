package cv.igrp.RH_Service.sigdi.domain.tatical.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.ChangeRequestField;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.TacticalActivityStatus;
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

    // Documents an existing behaviour that the PAA-02 guard does NOT change, and that the
    // guard's own rationale runs into: update() always recomputes the status, so an edit the
    // guard correctly permits still demotes an APPROVED activity back to DRAFT. The comment on
    // that line says "as per requirements", so it reads as a deliberate rule -- content changed,
    // re-approval needed -- and this phase does not alter it. It is asserted here so the
    // transition is explicit rather than incidental, and so any future change to it fails
    // loudly. Raised as 92-REVIEW.md WR-01 for the operator to rule on.
    @Test
    void update_demotesApprovedActivityToDraft_evenWhenOnlyTitleChanges() {
        Budget originalBudget = Budget.of(new BigDecimal("1000"), "02.02.01");
        TacticalActivity approved = buildApprovedActivityWithBudget(originalBudget);
        assertEquals(TacticalActivityStatus.APPROVED, approved.getStatus());

        TacticalActivity updated = approved.update(STRATEGIC_GOAL_ID, approved.getOrganicUnitId(),
                "Apenas o título mudou", approved.getDescriptionWhat(), approved.getJustificationWhy(),
                approved.getLocationWhere(), approved.getResponsibleWho(), approved.getMethodologyHow(),
                DATE_RANGE, null);

        assertEquals(TacticalActivityStatus.DRAFT, updated.getStatus());
    }

    // ---------------------------------------------------------------------------------------
    // applyApprovedChange() -- a via que a mensagem de erro da guarda do PAA-02 recomenda.
    // ---------------------------------------------------------------------------------------

    // O par deste teste com update_rejectsBudgetChangeWhenApproved: a mesma alteracao de
    // orcamento na mesma atividade Aprovada, recusada por update() e permitida por aqui. Sem os
    // dois lado a lado, a recomendacao "Utilize um Change Request" continuaria a nao levar a
    // lado nenhum, que e o defeito que esta fase fecha.
    @Test
    void applyApprovedChange_changesBudgetOnApprovedActivity_whereUpdateRefuses() {
        TacticalActivity approved = buildApprovedActivityWithBudget(
                Budget.of(new BigDecimal("1000"), "02.02.01"));

        TacticalActivity changed = approved.applyApprovedChange(ChangeRequestField.BUDGET, "2500");

        assertEquals(0, new BigDecimal("2500").compareTo(changed.getBudget().getEstimatedAmount()));
        assertEquals("02.02.01", changed.getBudget().getClassifier().getCode());
    }

    // Criterio 3: sem despromover a DRAFT. Decisao do operador de 2026-08-24 -- a alteracao
    // aplica-se, mas a atividade repassa pela aprovacao tatica.
    @Test
    void applyApprovedChange_sendsActivityBackToTacticalApproval_notToDraft() {
        TacticalActivity approved = buildApprovedActivityWithBudget(
                Budget.of(new BigDecimal("1000"), "02.02.01"));

        TacticalActivity changed = approved.applyApprovedChange(ChangeRequestField.BUDGET, "2500");

        assertEquals(TacticalActivityStatus.PENDING_TACTICAL, changed.getStatus());
        assertEquals(approved.getVersion() + 1, changed.getVersion());
    }

    @Test
    void applyApprovedChange_refusesWhenActivityIsNotApproved() {
        TacticalActivity draft = TacticalActivity.create(
                UUID.randomUUID(), STRATEGIC_GOAL_ID, UUID.randomUUID(),
                "Atividade tática em rascunho", "Descrição", "Justificação", "Localização", null,
                "Metodologia", DATE_RANGE, Budget.of(new BigDecimal("1000"), "02.02.01"),
                PaaLevel.UNIT_LEVEL);

        assertThrows(IgrpResponseStatusException.class,
                () -> draft.applyApprovedChange(ChangeRequestField.BUDGET, "2500"));
    }

    // Alterar UM extremo do intervalo pode inverte-lo contra o outro, que fica como esta. Sem
    // esta conversao, DateRange lancaria IllegalArgumentException e o utilizador via um 500.
    @Test
    void applyApprovedChange_refusesAnInvertedDateRangeAsBadRequest() {
        TacticalActivity approved = buildApprovedActivityWithBudget(
                Budget.of(new BigDecimal("1000"), "02.02.01"));

        assertThrows(IgrpResponseStatusException.class,
                () -> approved.applyApprovedChange(ChangeRequestField.START_DATE,
                        DATE_RANGE.getEndDate().plusDays(1).toString()));
    }

    @Test
    void applyApprovedChange_refusesANonPositiveBudgetAsBadRequest() {
        TacticalActivity approved = buildApprovedActivityWithBudget(
                Budget.of(new BigDecimal("1000"), "02.02.01"));

        assertThrows(IgrpResponseStatusException.class,
                () -> approved.applyApprovedChange(ChangeRequestField.BUDGET, "0"));
    }
}
