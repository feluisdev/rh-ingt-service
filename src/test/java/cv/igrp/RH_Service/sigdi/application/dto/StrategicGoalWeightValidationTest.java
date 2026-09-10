package cv.igrp.RH_Service.sigdi.application.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * 136-09 (D-52): prova, com um {@code Validator} real, que o intervalo {@code [0.1, 10]} que o
 * cliente já exige para {@code weight} (GoalSchema, {@code src/app/(myapp)/types/strategy.ts:191-
 * 194}) passa a existir também no servidor -- na criação ({@link CreateStategicGoalDTO}) e na
 * alteração ({@link UpdateStategicGoalDTO}). Antes desta fase nenhum dos dois DTO tinha anotação
 * nenhuma sobre {@code weight}: um {@code POST}/{@code PUT} com {@code weight = 15.00} era aceite
 * sem aviso -- é exatamente o que produziu a linha {@code 8071245f-9c65-41a9-a44d-2d291de878f3}
 * em {@code t_strategic_goals}, que fica na base, declarada, e não é apagada por este plano.
 *
 * <p><b>Este teste é o único guarda que existe.</b> O tipo {@code decimal} do manifesto IGRP
 * ({@code .igrpstudio/sigdi/dto/CreateStategicGoalDTO.json} e
 * {@code UpdateStategicGoalDTO.json}) só tem a chave {@code positive} -- não tem {@code min} nem
 * {@code max} -- pelo que {@code @DecimalMin}/{@code @DecimalMax} não podem ser declarados no
 * manifesto (ver {@code 136-MANIFESTOS.md}, secção c). Uma regeneração do IGRP Studio deita estas
 * anotações fora sem que nada mais falhe: é este teste, e só ele, que fica vermelho nesse dia.
 */
class StrategicGoalWeightValidationTest {

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

    private CreateStategicGoalDTO validCreateDto() {
        CreateStategicGoalDTO dto = new CreateStategicGoalDTO();
        dto.setTitle("Objetivo estratégico de teste");
        dto.setPerspective("FINANCIAL");
        dto.setWeight(new BigDecimal("1.5"));
        dto.setYear(2026);
        return dto;
    }

    private UpdateStategicGoalDTO validUpdateDto() {
        UpdateStategicGoalDTO dto = new UpdateStategicGoalDTO();
        dto.setTitle("Objetivo estratégico de teste");
        dto.setWeight(new BigDecimal("1.5"));
        dto.setYear(2026);
        return dto;
    }

    // -- CreateStategicGoalDTO --------------------------------------------------------------

    @Test
    void createWeightBelowMinimumViolatesConstraint() {
        CreateStategicGoalDTO dto = validCreateDto();
        dto.setWeight(new BigDecimal("0.05"));

        Set<ConstraintViolation<CreateStategicGoalDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size(), "weight = 0.05 está abaixo do mínimo 0.1 -- tem de violar");
        assertEquals("weight", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void createWeightAboveMaximumViolatesConstraint() {
        CreateStategicGoalDTO dto = validCreateDto();
        dto.setWeight(new BigDecimal("15"));

        Set<ConstraintViolation<CreateStategicGoalDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size(), "weight = 15 está acima do máximo 10 -- tem de violar");
        assertEquals("weight", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void createWeightWithinRangeDoesNotViolateConstraint() {
        CreateStategicGoalDTO dto = validCreateDto();
        dto.setWeight(new BigDecimal("1.5"));

        Set<ConstraintViolation<CreateStategicGoalDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(), "weight = 1.5 está dentro de [0.1, 10] -- não pode violar");
    }

    // -- UpdateStategicGoalDTO ---------------------------------------------------------------

    @Test
    void updateWeightBelowMinimumViolatesConstraint() {
        UpdateStategicGoalDTO dto = validUpdateDto();
        dto.setWeight(new BigDecimal("0.05"));

        Set<ConstraintViolation<UpdateStategicGoalDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size(), "weight = 0.05 está abaixo do mínimo 0.1 -- tem de violar");
        assertEquals("weight", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void updateWeightAboveMaximumViolatesConstraint() {
        UpdateStategicGoalDTO dto = validUpdateDto();
        dto.setWeight(new BigDecimal("15"));

        Set<ConstraintViolation<UpdateStategicGoalDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size(), "weight = 15 está acima do máximo 10 -- tem de violar");
        assertEquals("weight", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void updateWeightWithinRangeDoesNotViolateConstraint() {
        UpdateStategicGoalDTO dto = validUpdateDto();
        dto.setWeight(new BigDecimal("1.5"));

        Set<ConstraintViolation<UpdateStategicGoalDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(), "weight = 1.5 está dentro de [0.1, 10] -- não pode violar");
    }
}
