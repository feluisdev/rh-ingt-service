package cv.igrp.RH_Service.sigdi.interfaces.rest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cv.igrp.RH_Service.sigdi.application.dto.CreatePaaSubmissionPeriodDTO;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Regression guard: if the required role name is ever hardcoded back into
 * {@code PaaSubmissionPeriodController} instead of being sourced from
 * {@code PaaSecurityProperties}, this test fails.
 */
class PaaSubmissionPeriodControllerSecurityTest {

  @Test
  void createPaaSubmissionPeriod_referencesRoleBeanInsteadOfLiteral() throws NoSuchMethodException {
    Method method = PaaSubmissionPeriodController.class.getMethod(
        "createPaaSubmissionPeriod", CreatePaaSubmissionPeriodDTO.class);

    assertRoleBeanReferenceAndNoLiteral(method);
  }

  @Test
  void closePaaSubmissionPeriod_referencesRoleBeanInsteadOfLiteral() throws NoSuchMethodException {
    Method method = PaaSubmissionPeriodController.class.getMethod(
        "closePaaSubmissionPeriod", String.class);

    assertRoleBeanReferenceAndNoLiteral(method);
  }

  private void assertRoleBeanReferenceAndNoLiteral(Method method) {
    PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);
    assertNotNull(preAuthorize, () -> "@PreAuthorize is missing on " + method.getName());

    String expression = preAuthorize.value();
    assertTrue(expression.contains("@paaSecurityProperties.submissionPeriodRole"),
        () -> "Expected role bean reference in expression: " + expression);
    assertFalse(expression.contains("'RH'"),
        () -> "Role name literal 'RH' must not be hardcoded in: " + expression);
    assertFalse(expression.contains("${"),
        () -> "Spring Security does not resolve ${...} placeholders inside @PreAuthorize: " + expression);

    assertDoesNotThrow(() -> new SpelExpressionParser().parseExpression(expression),
        () -> "Expression is not syntactically valid SpEL: " + expression);
  }
}
