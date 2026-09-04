package cv.igrp.RH_Service.sigdi.application.queries;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cv.igrp.RH_Service.sigdi.application.dto.SiadapCcaStatusDTO;
import cv.igrp.framework.auth.core.security.IgrpAuthorizationService;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Unit tests for {@link GetSiadapCcaStatusQueryHandler} (AUT-04, Phase 115, decision
 * {@code ler-a-permissao-no-handler} of 2026-08-27 -- see {@code 115-07-SUMMARY.md}).
 *
 * <p>Uses the real {@link IgrpAuthorizationService}, not a mock. Confirmed by {@code javap}:
 * {@code checkPermission} has no Spring-managed collaborator, only a bare no-arg constructor
 * reading {@link SecurityContextHolder} directly. Mocking it would only prove the handler calls
 * a method that answers whatever the test tells it to -- it would not exercise the
 * {@code IGRP_SUPER_ADMIN} escape this class is required to cover (same reasoning as the
 * escape test in {@code AuthorizationWiringConfigTest}, Phase 115 plan 01). Setting real
 * authorities on the security context and asserting the handler's response is what actually
 * proves the escape.
 */
class GetSiadapCcaStatusQueryHandlerTest {

  private final IgrpAuthorizationService igrpAuthorization = new IgrpAuthorizationService();
  private final GetSiadapCcaStatusQueryHandler handler =
      new GetSiadapCcaStatusQueryHandler(igrpAuthorization);

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void callerWithTheCcaPermissionYieldsIsCcaTrue() {
    authenticateWith("siadap.cca.consultarEstado");

    ResponseEntity<SiadapCcaStatusDTO> response = handler.handle(new GetSiadapCcaStatusQuery());

    assertEquals(200, response.getStatusCode().value());
    assertTrue(response.getBody().getIsCca());
  }

  @Test
  void callerWithoutTheCcaPermissionYieldsIsCcaFalse() {
    authenticateWith("siadap.mencaoMerito.atribuir");

    ResponseEntity<SiadapCcaStatusDTO> response = handler.handle(new GetSiadapCcaStatusQuery());

    assertEquals(200, response.getStatusCode().value());
    assertFalse(response.getBody().getIsCca());
  }

  @Test
  void superAdminYieldsIsCcaTrueEvenWithoutTheCcaPermission() {
    authenticateWith("IGRP_SUPER_ADMIN");

    ResponseEntity<SiadapCcaStatusDTO> response = handler.handle(new GetSiadapCcaStatusQuery());

    assertEquals(200, response.getStatusCode().value());
    assertTrue(response.getBody().getIsCca());
  }

  private void authenticateWith(String authority) {
    Authentication authentication = new UsernamePasswordAuthenticationToken(
        "utilizador-de-teste", null, List.of(new SimpleGrantedAuthority(authority)));
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }
}
