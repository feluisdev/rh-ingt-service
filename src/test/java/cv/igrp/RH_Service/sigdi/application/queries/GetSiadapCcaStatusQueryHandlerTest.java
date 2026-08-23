package cv.igrp.RH_Service.sigdi.application.queries;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.RH_Service.sigdi.application.config.SiadapCcaSecurityProperties;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapCcaStatusDTO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

/**
 * Unit tests for {@link GetSiadapCcaStatusQueryHandler} (SIA-05, Phase 104, decision D-01).
 *
 * <p>Covers the four behaviours specified in {@code 104-02-PLAN.md}: membership true, membership
 * false, exactly-once delegation to {@link SiadapCcaSecurityProperties#isCca(String)} with the
 * resolver's own value (via {@link ArgumentCaptor}, so the handler cannot start deciding on its
 * own), and propagation -- not swallowing -- of the resolver's identity-lookup exception.
 */
@ExtendWith(MockitoExtension.class)
class GetSiadapCcaStatusQueryHandlerTest {

  @Mock
  private CurrentEmployeeResolver currentEmployeeResolver;

  @Mock
  private SiadapCcaSecurityProperties ccaSecurityProperties;

  @InjectMocks
  private GetSiadapCcaStatusQueryHandler handler;

  @Test
  void resolvedEmployeeInConfiguredListYieldsIsCcaTrue() {
    FuncionarioId employeeId = FuncionarioId.gerarNovo();
    when(currentEmployeeResolver.resolve()).thenReturn(employeeId);
    when(ccaSecurityProperties.isCca(employeeId.getStringValor())).thenReturn(true);

    ResponseEntity<SiadapCcaStatusDTO> response = handler.handle(new GetSiadapCcaStatusQuery());

    assertEquals(200, response.getStatusCode().value());
    assertTrue(response.getBody().getIsCca());
  }

  @Test
  void resolvedEmployeeOutsideConfiguredListYieldsIsCcaFalse() {
    FuncionarioId employeeId = FuncionarioId.gerarNovo();
    when(currentEmployeeResolver.resolve()).thenReturn(employeeId);
    when(ccaSecurityProperties.isCca(employeeId.getStringValor())).thenReturn(false);

    ResponseEntity<SiadapCcaStatusDTO> response = handler.handle(new GetSiadapCcaStatusQuery());

    assertEquals(200, response.getStatusCode().value());
    assertFalse(response.getBody().getIsCca());
  }

  @Test
  void isCcaIsCalledExactlyOnceWithTheResolversOwnValue() {
    FuncionarioId employeeId = FuncionarioId.gerarNovo();
    when(currentEmployeeResolver.resolve()).thenReturn(employeeId);
    when(ccaSecurityProperties.isCca(anyString())).thenReturn(true);

    handler.handle(new GetSiadapCcaStatusQuery());

    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    verify(ccaSecurityProperties, times(1)).isCca(captor.capture());
    assertEquals(employeeId.getStringValor(), captor.getValue());
  }

  @Test
  void resolverIdentityFailurePropagatesAndIsNeverConvertedToIsCcaFalse() {
    when(currentEmployeeResolver.resolve())
        .thenThrow(IgrpResponseStatusException.notFound(
            "Funcionário não encontrado para o utilizador autenticado"));

    assertThrows(IgrpResponseStatusException.class,
        () -> handler.handle(new GetSiadapCcaStatusQuery()));

    verify(ccaSecurityProperties, never()).isCca(anyString());
  }
}
