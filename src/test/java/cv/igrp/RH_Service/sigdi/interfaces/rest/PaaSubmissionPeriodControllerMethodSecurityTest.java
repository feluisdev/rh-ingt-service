package cv.igrp.RH_Service.sigdi.interfaces.rest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.authorization.AuthorizationWiringConfig;
import cv.igrp.RH_Service.sigdi.application.dto.CreatePaaSubmissionPeriodDTO;
import cv.igrp.RH_Service.sigdi.application.dto.PaaSubmissionPeriodResponseDTO;
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
 * Contraparte em execução de {@link PaaSubmissionPeriodControllerSecurityTest}, que só
 * inspeciona a anotação por reflexão. A autorização deste controller deixou de depender de
 * {@code PaaSecurityProperties} (bean com o papel {@code "RH"} nunca confirmado contra o realm
 * IAM) e passou a duas permissões IGRP nomeadas -- {@code paa.periodoSubmissao.criar} e
 * {@code paa.periodoSubmissao.fechar}. Este teste arranca um contexto mínimo com segurança de
 * método ligada ({@link AuthorizationWiringConfig}, importado tal como o plano 115-01 provou), e
 * invoca os métodos do {@link PaaSubmissionPeriodController} <b>proxied</b> diretamente -- nunca
 * por {@code MockMvc} -- para que a asserção seja sobre imposição e não sobre roteamento HTTP.
 *
 * <p>O caso {@code roles = "RH"} recusado nas duas ações não é redundante com "qualquer outra
 * authority": é o caso que documenta a mudança -- a prova de que a cadeia do papel foi mesmo
 * cortada, e a única asserção que fica vermelha se alguém repuser {@code hasRole("RH")}.
 */
@SpringJUnitConfig(PaaSubmissionPeriodControllerMethodSecurityTest.TestConfig.class)
class PaaSubmissionPeriodControllerMethodSecurityTest {

  @Autowired
  private PaaSubmissionPeriodController controller;

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
      when(commandBus.send(any())).thenReturn(ResponseEntity.ok(new PaaSubmissionPeriodResponseDTO()));
      return commandBus;
    }

    @Bean
    PaaSubmissionPeriodController paaSubmissionPeriodController(QueryBus queryBus, CommandBus commandBus) {
      return new PaaSubmissionPeriodController(queryBus, commandBus);
    }
  }

  @Test
  @WithMockUser(authorities = "paa.periodoSubmissao.criar")
  void createPaaSubmissionPeriod_allowsMatchingPermission() {
    assertDoesNotThrow(() -> controller.createPaaSubmissionPeriod(new CreatePaaSubmissionPeriodDTO()));
  }

  @Test
  @WithMockUser(authorities = "outra.permissao.qualquer")
  void createPaaSubmissionPeriod_deniesWithoutPermission() {
    assertThrows(AccessDeniedException.class,
        () -> controller.createPaaSubmissionPeriod(new CreatePaaSubmissionPeriodDTO()));
  }

  @Test
  @WithMockUser(authorities = "paa.periodoSubmissao.fechar")
  void createPaaSubmissionPeriod_deniesCallerWithOnlyFecharPermission() {
    // Ter a permissao de fechar nao pode abrir a de criar.
    assertThrows(AccessDeniedException.class,
        () -> controller.createPaaSubmissionPeriod(new CreatePaaSubmissionPeriodDTO()));
  }

  @Test
  @WithMockUser(roles = "RH")
  void createPaaSubmissionPeriod_deniesRoleRH() {
    // Prova de que a cadeia do papel "RH" foi cortada e nao apenas duplicada.
    assertThrows(AccessDeniedException.class,
        () -> controller.createPaaSubmissionPeriod(new CreatePaaSubmissionPeriodDTO()));
  }

  @Test
  @WithMockUser(authorities = "paa.periodoSubmissao.fechar")
  void closePaaSubmissionPeriod_allowsMatchingPermission() {
    assertDoesNotThrow(() -> controller.closePaaSubmissionPeriod(UUID.randomUUID().toString()));
  }

  @Test
  @WithMockUser(authorities = "outra.permissao.qualquer")
  void closePaaSubmissionPeriod_deniesWithoutPermission() {
    assertThrows(AccessDeniedException.class,
        () -> controller.closePaaSubmissionPeriod(UUID.randomUUID().toString()));
  }

  @Test
  @WithMockUser(authorities = "paa.periodoSubmissao.criar")
  void closePaaSubmissionPeriod_deniesCallerWithOnlyCriarPermission() {
    // Ter a permissao de criar nao pode abrir a de fechar.
    assertThrows(AccessDeniedException.class,
        () -> controller.closePaaSubmissionPeriod(UUID.randomUUID().toString()));
  }

  @Test
  @WithMockUser(roles = "RH")
  void closePaaSubmissionPeriod_deniesRoleRH() {
    // Prova de que a cadeia do papel "RH" foi cortada e nao apenas duplicada.
    assertThrows(AccessDeniedException.class,
        () -> controller.closePaaSubmissionPeriod(UUID.randomUUID().toString()));
  }
}
