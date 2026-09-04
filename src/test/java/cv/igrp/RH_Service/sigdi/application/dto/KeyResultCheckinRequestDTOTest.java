package cv.igrp.RH_Service.sigdi.application.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.Test;

// OKR-01: fixes the new KeyResultCheckinRequestDTO contract by Bean Validation, proving
// that evidenceUrl is now optional while valueAdded and comment remain mandatory.
class KeyResultCheckinRequestDTOTest {

    private static final Validator VALIDATOR;

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        VALIDATOR = factory.getValidator();
    }

    private KeyResultCheckinRequestDTO buildValidDto() {
        KeyResultCheckinRequestDTO dto = new KeyResultCheckinRequestDTO();
        dto.setValueAdded(new BigDecimal("10"));
        dto.setComment("Progresso registado nesta iteração");
        dto.setEvidenceUrl("https://exemplo.cv/evidencia.pdf");
        return dto;
    }

    @Test
    void validate_acceptsNullEvidenceUrl() {
        KeyResultCheckinRequestDTO dto = buildValidDto();
        dto.setEvidenceUrl(null);

        Set<ConstraintViolation<KeyResultCheckinRequestDTO>> violations = VALIDATOR.validate(dto);

        assertTrue(violations.isEmpty(),
                "Um check-in sem evidência tem de passar a validação — era o caso impossível antes desta fase");
    }

    @Test
    void validate_acceptsBlankEvidenceUrl() {
        KeyResultCheckinRequestDTO dto = buildValidDto();
        dto.setEvidenceUrl("");

        Set<ConstraintViolation<KeyResultCheckinRequestDTO>> violations = VALIDATOR.validate(dto);

        assertTrue(violations.isEmpty(),
                "String vazia e ausência têm de ser tratadas do mesmo modo para evidenceUrl");
    }

    @Test
    void validate_rejectsNullValueAdded() {
        KeyResultCheckinRequestDTO dto = buildValidDto();
        dto.setValueAdded(null);

        Set<ConstraintViolation<KeyResultCheckinRequestDTO>> violations = VALIDATOR.validate(dto);

        assertEquals(1, violations.size());
        ConstraintViolation<KeyResultCheckinRequestDTO> violation = violations.iterator().next();
        assertEquals("valueAdded", violation.getPropertyPath().toString(),
                "Fixa por teste o nome do campo, para que uma regeneração silenciosa do DTO fique vermelha");
    }

    @Test
    void validate_rejectsBlankComment() {
        KeyResultCheckinRequestDTO dto = buildValidDto();
        dto.setComment("   ");

        Set<ConstraintViolation<KeyResultCheckinRequestDTO>> violations = VALIDATOR.validate(dto);

        assertEquals(1, violations.size());
        ConstraintViolation<KeyResultCheckinRequestDTO> violation = violations.iterator().next();
        assertEquals("comment", violation.getPropertyPath().toString(),
                "O afrouxamento aplicou-se só à evidência, o comentário continua obrigatório");
    }
}
