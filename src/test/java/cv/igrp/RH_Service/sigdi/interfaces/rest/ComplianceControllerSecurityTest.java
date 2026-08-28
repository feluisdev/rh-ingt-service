package cv.igrp.RH_Service.sigdi.interfaces.rest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cv.igrp.RH_Service.shared.security.DenialMessage;
import cv.igrp.RH_Service.sigdi.application.dto.AssignMeritRatingRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.CloseEvaluationsRequestDTO;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Regression guard por reflexão: prova a forma das três expressões {@code @PreAuthorize}
 * acrescentadas pelo plano 115-03 e a presença de {@code @DenialMessage} em cada uma. Não
 * substitui {@link ComplianceControllerMethodSecurityTest}, que prova a imposição em execução —
 * este teste existe para apanhar um erro de escrita da expressão (nome de permissão trocado,
 * bean errado, SpEL malformado) com uma mensagem de falha legível, em vez de um
 * {@code AccessDeniedException} inexplicável a aparecer só quando alguém tentar mesmo a ação.
 */
class ComplianceControllerSecurityTest {

  @Test
  void closeEvaluations_referencesFecharEmLotePermission() throws NoSuchMethodException {
    Method method = ComplianceController.class.getMethod(
        "closeEvaluations", CloseEvaluationsRequestDTO.class);

    assertPermissionCheckAndDenialMessage(method, "SIADAP_AVALIACOES_FECHAREMLOTE");
  }

  @Test
  void openSelfEvaluationPhase_referencesAutoavaliacaoAbrirPermission() throws NoSuchMethodException {
    Method method = ComplianceController.class.getMethod(
        "openSelfEvaluationPhase", String.class);

    assertPermissionCheckAndDenialMessage(method, "SIADAP_AUTOAVALIACAO_ABRIR");
  }

  @Test
  void assignMeritRating_referencesMencaoMeritoAtribuirPermission() throws NoSuchMethodException {
    Method method = ComplianceController.class.getMethod(
        "assignMeritRating", String.class, AssignMeritRatingRequestDTO.class);

    assertPermissionCheckAndDenialMessage(method, "SIADAP_MENCAOMERITO_ATRIBUIR");
  }

  private void assertPermissionCheckAndDenialMessage(Method method, String expectedPermissionConstant) {
    PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);
    assertNotNull(preAuthorize, () -> "@PreAuthorize is missing on " + method.getName());

    String expression = preAuthorize.value();
    assertTrue(expression.contains("@igrpAuthorization"),
        () -> "Expected igrpAuthorization bean reference in expression: " + expression);
    assertTrue(expression.contains("checkPermission"),
        () -> "Expected checkPermission call in expression: " + expression);
    assertTrue(expression.contains(expectedPermissionConstant),
        () -> "Expected permission constant " + expectedPermissionConstant
            + " in expression: " + expression);

    assertDoesNotThrow(() -> new SpelExpressionParser().parseExpression(expression),
        () -> "Expression is not syntactically valid SpEL: " + expression);

    DenialMessage denialMessage = method.getAnnotation(DenialMessage.class);
    assertNotNull(denialMessage, () -> "@DenialMessage is missing on " + method.getName());
    assertFalse(denialMessage.value().isBlank(),
        () -> "@DenialMessage value must not be blank on " + method.getName());
  }
}
