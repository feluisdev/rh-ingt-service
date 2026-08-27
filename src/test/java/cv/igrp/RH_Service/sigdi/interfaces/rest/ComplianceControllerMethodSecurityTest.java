package cv.igrp.RH_Service.sigdi.interfaces.rest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.authorization.AuthorizationWiringConfig;
import cv.igrp.RH_Service.sigdi.application.dto.AssignMeritRatingRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.CloseEvaluationsRequestDTO;
import cv.igrp.framework.core.domain.CommandBus;
import cv.igrp.framework.core.domain.QueryBus;
import cv.igrp.platform.access.client.ApiClient;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * Contraparte em execução de {@link ComplianceControllerSecurityTest}, que só inspeciona a
 * anotação por reflexão. Isso não basta: a reflexão prova que a expressão está bem formada e
 * referencia o bean certo, não que o Spring Security a resolve e a aplica de facto. Uma quebra
 * do wiring do plano 115-01 (bean {@code igrpAuthorization} renomeado, {@code @EnableMethodSecurity}
 * retirado, ou falha do proxy AOP) deixaria o teste de reflexão verde enquanto a autorização
 * deixava, em silêncio, de acontecer.
 *
 * <p>Este teste arranca um contexto mínimo com segurança de método ligada
 * ({@link AuthorizationWiringConfig}, importado tal como o plano 115-01 provou), e invoca os
 * métodos do {@link ComplianceController} <b>proxied</b> diretamente — nunca por
 * {@code MockMvc} — para que a asserção seja sobre imposição e não sobre roteamento HTTP.
 *
 * <p>Cada caso de recusa distingue explicitamente {@link AccessDeniedException} (recusa real,
 * wiring ligado) de {@code SpelEvaluationException} (a expressão nem chegou a ser avaliada —
 * wiring desfeito). {@code assertThrows(AccessDeniedException.class, ...)} falha com a mensagem
 * de erro do JUnit se for lançada qualquer outra exceção, incluindo
 * {@code SpelEvaluationException} — não há forma deste teste passar por engano com o wiring
 * desfeito.
 */
@SpringJUnitConfig(ComplianceControllerMethodSecurityTest.TestConfig.class)
class ComplianceControllerMethodSecurityTest {

  @Autowired
  private ComplianceController controller;

  @Configuration
  @EnableMethodSecurity
  @Import(AuthorizationWiringConfig.class)
  static class TestConfig {

    @Bean
    static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
      // Necessário para os @Value do AuthorizationSyncRunner (todos com omissão ":")
      // resolverem neste contexto isolado, tal como resolveriam a partir de
      // application.properties na aplicação real.
      return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean("igrpApiClient")
    ApiClient igrpApiClient() {
      // Sobrepõe-se, por nome, ao bean "igrpApiClient" real que AuthorizationWiringConfig
      // declara. Evita que o AuthorizationSyncRunner deste teste faça uma chamada HTTP real.
      return mock(ApiClient.class);
    }

    @Bean
    QueryBus queryBus() {
      return mock(QueryBus.class);
    }

    @Bean
    CommandBus commandBus() {
      CommandBus commandBus = mock(CommandBus.class);
      // Um único stub genérico serve os três comandos: o que este teste verifica é se o
      // despacho é alcançado, não a forma da resposta.
      when(commandBus.send(any())).thenReturn(ResponseEntity.ok().build());
      return commandBus;
    }

    @Bean
    ComplianceController complianceController(QueryBus queryBus, CommandBus commandBus) {
      return new ComplianceController(queryBus, commandBus);
    }
  }

  @Test
  @WithMockUser(authorities = "siadap.mencaoMerito.atribuir")
  void assignMeritRating_allowsMatchingPermission() {
    assertDoesNotThrow(() -> controller.assignMeritRating(
        UUID.randomUUID().toString(), new AssignMeritRatingRequestDTO("GOOD")));
  }

  @Test
  @WithMockUser(authorities = "outra.permissao.qualquer")
  void assignMeritRating_deniesWithoutPermission() {
    assertThrows(AccessDeniedException.class, () -> controller.assignMeritRating(
        UUID.randomUUID().toString(), new AssignMeritRatingRequestDTO("GOOD")));
  }

  @Test
  @WithMockUser(authorities = "siadap.avaliacoes.fecharEmLote")
  void closeEvaluations_allowsMatchingPermission() {
    assertDoesNotThrow(() -> controller.closeEvaluations(
        new CloseEvaluationsRequestDTO(2026, null)));
  }

  @Test
  @WithMockUser(authorities = "outra.permissao.qualquer")
  void closeEvaluations_deniesWithoutPermission() {
    assertThrows(AccessDeniedException.class, () -> controller.closeEvaluations(
        new CloseEvaluationsRequestDTO(2026, null)));
  }

  @Test
  @WithMockUser(authorities = "siadap.autoavaliacao.abrir")
  void openSelfEvaluationPhase_allowsMatchingPermission() {
    assertDoesNotThrow(() -> controller.openSelfEvaluationPhase(UUID.randomUUID().toString()));
  }

  @Test
  @WithMockUser(authorities = "outra.permissao.qualquer")
  void openSelfEvaluationPhase_deniesWithoutPermission() {
    assertThrows(AccessDeniedException.class,
        () -> controller.openSelfEvaluationPhase(UUID.randomUUID().toString()));
  }

  @Test
  @WithMockUser(authorities = "siadap.avaliacoes.fecharEmLote")
  void assignMeritRating_deniesCallerWithOnlyCloseEvaluationsPermission() {
    // T-115-08: ter a permissão de uma das três ações não pode abrir nenhuma das outras
    // duas. Um copiar-colar entre os três métodos que trocasse a constante de permissão
    // deixaria este caso passar por engano.
    assertThrows(AccessDeniedException.class, () -> controller.assignMeritRating(
        UUID.randomUUID().toString(), new AssignMeritRatingRequestDTO("GOOD")));
  }
}
