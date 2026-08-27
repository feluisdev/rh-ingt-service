package cv.igrp.RH_Service.sigdi.application.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * PRZ-03: prova, com um Validator real (Hibernate Validator 8, via
 * spring-boot-starter-validation), de que o campo purpose deixou de ser
 * opcional na fronteira de validação de bean. Sem contexto de Spring de
 * propósito -- ver 118-01-PLAN.md Task 1.
 */
class CreatePaaSubmissionPeriodDTOValidationTest {

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

    private CreatePaaSubmissionPeriodDTO completeDtoWithPurpose(String purpose) {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(10);
        return new CreatePaaSubmissionPeriodDTO(
                PaaLevel.UNIT_LEVEL.getCode(), start, end, 2026, purpose);
    }

    private boolean hasViolationOnPurpose(Set<ConstraintViolation<CreatePaaSubmissionPeriodDTO>> violations) {
        return violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("purpose"));
    }

    @Test
    void nullPurposeViolatesConstraint() {
        CreatePaaSubmissionPeriodDTO dto = completeDtoWithPurpose(null);

        Set<ConstraintViolation<CreatePaaSubmissionPeriodDTO>> violations = validator.validate(dto);

        assertTrue(hasViolationOnPurpose(violations),
                "purpose nulo devia violar a restrição de obrigatoriedade");
    }

    @Test
    void emptyPurposeViolatesConstraint() {
        CreatePaaSubmissionPeriodDTO dto = completeDtoWithPurpose("");

        Set<ConstraintViolation<CreatePaaSubmissionPeriodDTO>> violations = validator.validate(dto);

        assertTrue(hasViolationOnPurpose(violations),
                "purpose em cadeia vazia devia violar a restrição de obrigatoriedade");
    }

    @Test
    void blankPurposeViolatesConstraint() {
        CreatePaaSubmissionPeriodDTO dto = completeDtoWithPurpose("   ");

        Set<ConstraintViolation<CreatePaaSubmissionPeriodDTO>> violations = validator.validate(dto);

        assertTrue(hasViolationOnPurpose(violations),
                "purpose só de espaços devia violar a restrição de obrigatoriedade");
    }

    @Test
    void knownPurposeCodeDoesNotViolateConstraint() {
        CreatePaaSubmissionPeriodDTO dto = completeDtoWithPurpose(Purpose.SIADAP.getCode());

        Set<ConstraintViolation<CreatePaaSubmissionPeriodDTO>> violations = validator.validate(dto);

        assertFalse(hasViolationOnPurpose(violations),
                "um código válido de Purpose não devia violar nenhuma restrição em purpose");
    }
}
