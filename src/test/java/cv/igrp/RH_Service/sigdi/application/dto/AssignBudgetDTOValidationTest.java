package cv.igrp.RH_Service.sigdi.application.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * A-135-2AA: prova, com um Validator real (o mesmo motor que o {@code @Valid} do
 * TaticalController usa na vinculação do pedido), de que {@code activityId} deixou de
 * ser obrigatório na fronteira de validação de bean.
 *
 * <p>Antes desta correção, {@code TaticalController.assignBudget} só copiava o
 * {@code activityId} do {@code id} do caminho DEPOIS de o Spring já ter corrido esta
 * validação sobre o corpo do pedido -- pelo que o corpo real que a interface envia
 * (sem {@code activityId}, porque esse campo vem do caminho) levava sempre 400. A
 * contraprova (repor {@code @NotNull} em {@code activityId}) faz
 * {@code bodyWithoutActivityIdDoesNotViolateConstraint} falhar, exatamente como o
 * pedido real falhava antes desta fase.
 */
class AssignBudgetDTOValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    // Corpo real de AssignBudgetModal.tsx / functions/tactical.ts:assignBudget -- sem
    // activityId, que só existe no caminho do URL.
    private AssignBudgetDTO bodyAsTheRealUiSendsIt() {
        AssignBudgetDTO dto = new AssignBudgetDTO();
        dto.setBudgetEstimated(new BigDecimal("5000.00"));
        dto.setEconomicClassifier("02.03.01");
        return dto;
    }

    @Test
    void bodyWithoutActivityIdDoesNotViolateConstraint() {
        AssignBudgetDTO dto = bodyAsTheRealUiSendsIt();

        Set<ConstraintViolation<AssignBudgetDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(),
                "O corpo real da interface (sem activityId, que vem do caminho) tem de passar a "
                        + "validação -- era exatamente o caso que dava sempre 400 antes do A-135-2AA");
    }

    @Test
    void activityIdSetAfterValidationStillDoesNotViolateConstraint() {
        AssignBudgetDTO dto = bodyAsTheRealUiSendsIt();
        dto.setActivityId(UUID.randomUUID());

        Set<ConstraintViolation<AssignBudgetDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(),
                "Com activityId preenchido (como o controlador faz a partir do {id} do caminho) "
                        + "continua sem violações");
    }

    @Test
    void missingBudgetEstimatedStillViolatesConstraint() {
        AssignBudgetDTO dto = bodyAsTheRealUiSendsIt();
        dto.setBudgetEstimated(null);

        Set<ConstraintViolation<AssignBudgetDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        ConstraintViolation<AssignBudgetDTO> violation = violations.iterator().next();
        assertEquals("budgetEstimated", violation.getPropertyPath().toString(),
                "O afrouxamento aplicou-se só a activityId -- budgetEstimated continua obrigatório "
                        + "(prova de que a Regra 1 do CLAUDE.md do BFF, propagar o erro real, continua "
                        + "a ter um erro real para propagar)");
    }

    @Test
    void blankEconomicClassifierStillViolatesConstraint() {
        AssignBudgetDTO dto = bodyAsTheRealUiSendsIt();
        dto.setEconomicClassifier("   ");

        Set<ConstraintViolation<AssignBudgetDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty(),
                "economicClassifier em branco continua a violar a restrição de obrigatoriedade");
    }
}
