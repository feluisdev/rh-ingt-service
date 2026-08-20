package cv.igrp.RH_Service.sigdi.interfaces.rest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cv.igrp.framework.core.domain.CommandBus;
import cv.igrp.framework.core.domain.QueryBus;
import cv.igrp.RH_Service.sigdi.application.config.PaaSecurityProperties;
import cv.igrp.RH_Service.sigdi.application.dto.CreatePaaSubmissionPeriodDTO;
import cv.igrp.RH_Service.sigdi.application.dto.PaaSubmissionPeriodResponseDTO;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * Runtime counterpart to {@link PaaSubmissionPeriodControllerSecurityTest}, which only
 * inspects the annotation by reflection. That is not enough on its own: reflection proves the
 * expression is well-formed SpEL referencing a bean, not that Spring Security actually resolves
 * {@code @paaSecurityProperties.submissionPeriodRole} and enforces it. Renaming the bean,
 * dropping {@code @EnableMethodSecurity}, or any AOP-proxy failure would leave the reflection
 * test green while authorization silently stopped happening (92-REVIEW.md WR-03).
 *
 * This test boots a minimal context with method security enabled and invokes the proxied
 * controller directly, so the assertion is about enforcement, not about the source text.
 * It deliberately does NOT confirm that "RH" is the correct realm role — that remains an open
 * question for IAM. It confirms that whatever role the property carries is the one enforced.
 */
@SpringJUnitConfig(PaaSubmissionPeriodControllerMethodSecurityTest.TestConfig.class)
class PaaSubmissionPeriodControllerMethodSecurityTest {

  @Autowired
  private PaaSubmissionPeriodController controller;

  @Configuration
  @EnableMethodSecurity
  static class TestConfig {

    @Bean
    static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
      // Required so the @Value default ":RH" on PaaSecurityProperties resolves in this
      // standalone context, exactly as it does from application.properties in the app.
      return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean("paaSecurityProperties")
    PaaSecurityProperties paaSecurityProperties() {
      return new PaaSecurityProperties();
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
  @WithMockUser(roles = "RH")
  void closePaaSubmissionPeriod_allowsConfiguredRole() {
    assertDoesNotThrow(() -> controller.closePaaSubmissionPeriod(UUID.randomUUID().toString()));
  }

  @Test
  @WithMockUser(roles = "GESTOR")
  void closePaaSubmissionPeriod_deniesOtherRole() {
    assertThrows(AccessDeniedException.class,
        () -> controller.closePaaSubmissionPeriod(UUID.randomUUID().toString()));
  }

  @Test
  @WithMockUser(roles = "RH")
  void createPaaSubmissionPeriod_allowsConfiguredRole() {
    assertDoesNotThrow(() -> controller.createPaaSubmissionPeriod(new CreatePaaSubmissionPeriodDTO()));
  }

  @Test
  @WithMockUser(roles = "GESTOR")
  void createPaaSubmissionPeriod_deniesOtherRole() {
    assertThrows(AccessDeniedException.class,
        () -> controller.createPaaSubmissionPeriod(new CreatePaaSubmissionPeriodDTO()));
  }
}
