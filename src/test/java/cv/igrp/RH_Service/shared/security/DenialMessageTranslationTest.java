package cv.igrp.RH_Service.shared.security;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cv.igrp.RH_Service.authorization.AuthorizationWiringConfig;
import cv.igrp.RH_Service.shared.domain.exceptions.GlobalExceptionHandler;
import cv.igrp.platform.access.client.ApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Prova end-to-end, por {@link MockMvc}, de que uma recusa de {@code @PreAuthorize} chega ao
 * cliente com a mensagem em português da ação (ou uma mensagem genérica quando a ação não
 * declara {@link DenialMessage}), na mesma forma {@code ProblemDetail} do resto do serviço.
 *
 * <p>Usa um controller-sonda declarado dentro do próprio teste -- dois métodos guardados por
 * {@code @PreAuthorize}, um com {@link DenialMessage} e outro sem -- para continuar a valer
 * quando os controllers reais mudarem nas ondas seguintes (115-03 a 115-07). O contexto inclui
 * {@link AuthorizationWiringConfig} (para {@code T(Permission)} e o bean {@code igrpAuthorization}
 * resolverem, tal como provado pelo {@code AuthorizationWiringConfigTest} do plano 115-01) e
 * {@code @EnableMethodSecurity}, para a cadeia ser a verdadeira: pedido HTTP -> proxy AOP do
 * método-security -> {@code AccessDeniedException} -> {@link GlobalExceptionHandler}.
 */
@SpringJUnitConfig(DenialMessageTranslationTest.TestConfig.class)
class DenialMessageTranslationTest {

  private static final String MENSAGEM_DECLARADA =
      "Apenas um membro do Conselho Coordenador da Avaliação (CCA) pode executar esta ação";

  @Autowired
  private ProbeController probeController;

  @Autowired
  private GlobalExceptionHandler globalExceptionHandler;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    // probeController é o bean tal como o contexto Spring o entrega -- com @EnableMethodSecurity
    // presente, é o proxy AOP do método-security, não a instância nua. standaloneSetup invoca-o
    // através desse proxy, tal como o DispatcherServlet real faria.
    mockMvc = MockMvcBuilders.standaloneSetup(probeController)
        .setControllerAdvice(globalExceptionHandler)
        .build();
  }

  @Test
  @WithMockUser(authorities = "outra.coisa")
  void deniedCallerOnActionWithDeclaredMessage_getsThe403WithTheDeclaredMessage() throws Exception {
    mockMvc.perform(get("/probe/with-message"))
        .andExpect(status().isForbidden())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").value(MENSAGEM_DECLARADA));
  }

  @Test
  @WithMockUser(authorities = "outra.coisa")
  void deniedCallerOnActionWithoutDeclaredMessage_getsAGenericNonEmptyPortugueseMessage() throws Exception {
    mockMvc.perform(get("/probe/without-message"))
        .andExpect(status().isForbidden())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.title").isNotEmpty())
        .andExpect(jsonPath("$.title").value(org.hamcrest.Matchers.not(MENSAGEM_DECLARADA)));
  }

  @Test
  @WithMockUser(authorities = "siadap.mencaoMerito.atribuir")
  void callerWithMatchingPermission_getsA2xxWithNoErrorBody() throws Exception {
    mockMvc.perform(get("/probe/with-message"))
        .andExpect(status().isOk())
        .andExpect(content().string(""));
  }

  @Configuration
  @EnableMethodSecurity
  @Import(AuthorizationWiringConfig.class)
  static class TestConfig {

    @Bean
    static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
      // Necessário para os @Value do AuthorizationSyncRunner (todos com omissão ":") resolverem
      // neste contexto isolado -- mesmo padrão do AuthorizationWiringConfigTest do plano 115-01.
      return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean("igrpApiClient")
    ApiClient igrpApiClient() {
      // Sobrepõe-se, por nome, ao bean real de AuthorizationWiringConfig -- evita uma chamada
      // HTTP real ao arranque do AuthorizationSyncRunner (irrelevante para este teste).
      return mock(ApiClient.class);
    }

    @Bean
    ProbeController probeController() {
      return new ProbeController();
    }

    @Bean
    GlobalExceptionHandler globalExceptionHandler() {
      return new GlobalExceptionHandler();
    }
  }

  /**
   * Sonda deliberadamente alheia a qualquer controller real -- continua a valer depois de os
   * controllers das ondas 115-03 a 115-07 mudarem. A expressão SpEL é exatamente a forma que
   * esses planos replicam nos pontos de decisão reais.
   */
  @RestController
  static class ProbeController {

    @GetMapping("/probe/with-message")
    @PreAuthorize("@igrpAuthorization.checkPermission(T(Permission).SIADAP_MENCAOMERITO_ATRIBUIR)")
    @DenialMessage(MENSAGEM_DECLARADA)
    ResponseEntity<Void> withDeclaredMessage() {
      return ResponseEntity.ok().build();
    }

    @GetMapping("/probe/without-message")
    @PreAuthorize("@igrpAuthorization.checkPermission(T(Permission).SIADAP_MENCAOMERITO_ATRIBUIR)")
    ResponseEntity<Void> withoutDeclaredMessage() {
      return ResponseEntity.ok().build();
    }
  }
}
