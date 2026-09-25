package cv.igrp.RH_Service.sigdi.application.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * 136-09 (D-52, F2 -- «limite de negócio só no cliente»): prova, com um {@code Validator} real,
 * que o limite {@code min(10)} que o cliente já exige para {@code justificationWhy}
 * ({@code ActivitySchema.justification_why}, {@code src/app/(myapp)/types/tactical.ts:197-199})
 * passa a existir também no servidor. Antes desta fase o Java aceitava um único caractere
 * (@Size(min = 1)) -- quem chamasse o Gateway diretamente escrevia o que quisesse.
 *
 * <p>Este é o caso mais simples da família: valida {@link CreateTacticalActivityDTO} isolado. O
 * caso que dá valor à alteração de atividade -- a mesma regra atravessando
 * {@link cv.igrp.RH_Service.sigdi.application.commands.UpdateTacticalActivityCommand} -- está em
 * {@code UpdateTacticalActivityCommandValidationTest#nestedInvalidJustificationWhyViolatesConstraint},
 * porque é aí que a cascata do {@code @Valid} (Task 2a) é exercida.
 */
class CreateTacticalActivityDTOJustificationWhyValidationTest {

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

    private CreateTacticalActivityDTO validDto() {
        CreateTacticalActivityDTO dto = new CreateTacticalActivityDTO();
        dto.setStrategicGoalId(UUID.randomUUID());
        dto.setOrganicUnitId(UUID.randomUUID());
        dto.setTitle("Atividade tática de teste");
        dto.setStartDate(LocalDate.of(2026, 1, 1));
        dto.setEndDate(LocalDate.of(2026, 12, 31));
        return dto;
    }

    @Test
    void justificationWhyWithNineCharactersViolatesConstraint() {
        CreateTacticalActivityDTO dto = validDto();
        dto.setJustificationWhy("123456789"); // 9 caracteres

        Set<ConstraintViolation<CreateTacticalActivityDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size(),
                "justificationWhy com 9 caracteres está abaixo do mínimo 10 -- tem de violar");
        assertEquals("justificationWhy", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void justificationWhyWithTenCharactersDoesNotViolateConstraint() {
        CreateTacticalActivityDTO dto = validDto();
        dto.setJustificationWhy("1234567890"); // 10 caracteres

        Set<ConstraintViolation<CreateTacticalActivityDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(),
                "justificationWhy com exatamente 10 caracteres cumpre o mínimo -- não pode violar");
    }
}
