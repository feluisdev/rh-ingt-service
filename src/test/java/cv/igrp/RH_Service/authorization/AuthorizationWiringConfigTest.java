package cv.igrp.RH_Service.authorization;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import cv.igrp.framework.auth.core.autoconfig.AuthorizationSyncRunner;
import cv.igrp.platform.access.client.ApiClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * Prova ao vivo do achado P1 da investigação da Fase 115: sem {@link AuthorizationWiringConfig},
 * {@code T(Permission)} dentro de um {@code @PreAuthorize} não resolve neste serviço, e a falha
 * só aparece em execução -- {@code SpelEvaluationException} no primeiro pedido que a expressão é
 * avaliada, nunca à compilação. Este teste é a única coisa entre a fase inteira e essa falha
 * silenciosa: invoca o método através do proxy AOP real do Spring Security, com
 * {@code @EnableMethodSecurity} e {@link AuthorizationWiringConfig} ambos presentes, e distingue
 * explicitamente {@link AccessDeniedException} (recusa real, wiring ligado) de
 * {@code SpelEvaluationException} (a expressão nem chegou a ser avaliada -- wiring desfeito).
 *
 * <p><b>Nota de 2026-08-27:</b> {@link AuthorizationWiringConfig} deixou de usar {@code @Import}
 * das classes do jar -- ver o javadoc dessa classe para a razão medida (ASM do Spring 6.2.8 só lê
 * bytecode até {@code major 69}; os jars IGRP são {@code major 70}). Declara agora, directamente,
 * quatro beans equivalentes byte a byte: {@code igrpApiClient}, {@code igrpAuthorization},
 * {@code authorizationSyncRunner} e {@code methodSecurityExpressionHandler}. Se alguém remover
 * um destes quatro beans de {@link AuthorizationWiringConfig}, este teste tem de ficar vermelho.
 * A partir daqui -- e só a partir daqui -- é seguro escrever {@code @PreAuthorize} com
 * {@code T(Permission)} nos pontos de decisão das ondas seguintes.
 */
@SpringJUnitConfig(AuthorizationWiringConfigTest.TestConfig.class)
class AuthorizationWiringConfigTest {

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private Probe probe;

  @Configuration
  @EnableMethodSecurity
  @Import(AuthorizationWiringConfig.class)
  static class TestConfig {

    @Bean
    static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
      // Necessário para os @Value do AuthorizationSyncRunner (todos com omissão ":") resolverem
      // neste contexto isolado, tal como resolveriam a partir de application.properties na app.
      return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean("igrpApiClient")
    ApiClient igrpApiClient() {
      // Sobrepõe-se, por nome, ao bean "igrpApiClient" real que AuthorizationWiringConfig
      // declara (Spring permite substituição de definições de bean pelo nome). Evita que o
      // AuthorizationSyncRunner deste teste faça uma chamada HTTP real a um endpoint
      // inexistente -- a excepção seria apanhada e ignorada de qualquer forma (o @PostConstruct
      // do runner intercepta tudo e só regista log), mas um mock é mais rápido e determinístico.
      return mock(ApiClient.class);
    }

    @Bean
    Probe probe() {
      return new Probe();
    }
  }

  /**
   * Sonda trivial e deliberadamente alheia a qualquer controller real: continua a valer depois
   * de os controllers das ondas seguintes mudarem. A expressão SpEL é exatamente a forma que os
   * planos 03 a 07 replicam nos pontos de decisão reais.
   */
  @Component
  static class Probe {

    @PreAuthorize("@igrpAuthorization.checkPermission(T(Permission).SIADAP_MENCAOMERITO_ATRIBUIR)")
    void probeAction() {
      // corpo vazio -- só o @PreAuthorize importa
    }
  }

  @Test
  @WithMockUser(authorities = "siadap.mencaoMerito.atribuir")
  void allowsCallerWithMatchingPermission() {
    assertDoesNotThrow(probe::probeAction);
  }

  @Test
  @WithMockUser(authorities = "outra.coisa")
  void deniesCallerWithoutPermission() {
    // A distinção é o coração deste teste: AccessDeniedException prova que a expressão foi
    // avaliada e recusou; SpelEvaluationException provaria que o wiring está desfeito e a
    // expressão nem chegou a ser avaliada. assertThrows falha o teste com a mensagem de erro do
    // JUnit se for lançada qualquer exceção que não seja AccessDeniedException (ou subclasse),
    // incluindo SpelEvaluationException -- não há forma de este teste passar por engano com o
    // wiring desfeito.
    assertThrows(AccessDeniedException.class, probe::probeAction);
  }

  @Test
  @WithMockUser(authorities = "IGRP_SUPER_ADMIN")
  void allowsSuperAdminEscape() {
    // Cobre por teste, não por boa-fé, o escape documentado no javadoc de
    // AuthorizationWiringConfig: IgrpAuthorizationService.checkPermission devolve true a
    // qualquer chamador com IGRP_SUPER_ADMIN, independentemente da permissão pedida.
    assertDoesNotThrow(probe::probeAction);
  }

  @Test
  void contextHasIgrpAuthorizationBeanWithExactName() {
    assertNotNull(applicationContext.getBean("igrpAuthorization"));
  }

  @Test
  void contextHasAuthorizationSyncRunnerBean() {
    assertInstanceOf(AuthorizationSyncRunner.class,
        applicationContext.getBean(AuthorizationSyncRunner.class));
  }
}
