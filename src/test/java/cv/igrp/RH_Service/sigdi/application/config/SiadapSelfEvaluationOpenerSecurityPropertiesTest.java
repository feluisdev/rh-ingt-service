package cv.igrp.RH_Service.sigdi.application.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.mock.env.MockPropertySource;

/**
 * Mould: {@link SiadapCcaSecurityPropertiesTest}. The empty-list case is the most important
 * one in this file -- it is the fail-closed default that D-05 of {@code 111-02-PLAN.md} chose
 * deliberately, and it must be proven, not just documented.
 */
class SiadapSelfEvaluationOpenerSecurityPropertiesTest {

  private static final String ID_1 = "11111111-1111-1111-1111-111111111111";
  private static final String ID_2 = "22222222-2222-2222-2222-222222222222";
  private static final String ID_3 = "33333333-3333-3333-3333-333333333333";

  private static AnnotationConfigApplicationContext contextWithProperty(String value) {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    if (value != null) {
      MockPropertySource mockPropertySource =
          new MockPropertySource()
              .withProperty("sigdi.siadap.self-evaluation-opener-employee-ids", value);
      context.getEnvironment().getPropertySources().addFirst(mockPropertySource);
    }
    context.register(
        PropertySourcesPlaceholderConfigurer.class,
        SiadapSelfEvaluationOpenerSecurityProperties.class);
    return context;
  }

  private static ListAppender<ILoggingEvent> attachAppender() {
    Logger logger =
        (Logger) LoggerFactory.getLogger(SiadapSelfEvaluationOpenerSecurityProperties.class);
    ListAppender<ILoggingEvent> appender = new ListAppender<>();
    appender.start();
    logger.addAppender(appender);
    return appender;
  }

  private static void detachAppender(ListAppender<ILoggingEvent> appender) {
    Logger logger =
        (Logger) LoggerFactory.getLogger(SiadapSelfEvaluationOpenerSecurityProperties.class);
    logger.detachAppender(appender);
    appender.stop();
  }

  @Test
  void whenPropertyNotSet_isSelfEvaluationOpenerIsFalseForAnyId_failClosedCase() {
    try (AnnotationConfigApplicationContext context = contextWithProperty(null)) {
      context.refresh();
      SiadapSelfEvaluationOpenerSecurityProperties properties =
          context.getBean(SiadapSelfEvaluationOpenerSecurityProperties.class);

      assertFalse(properties.isSelfEvaluationOpener(ID_1));
      assertFalse(properties.isConfigured());
      assertEquals(0, properties.getConfiguredCount());
    }
  }

  @Test
  void whenPropertyNotSet_startupEmitsWarnNamingTheProperty() {
    ListAppender<ILoggingEvent> appender = attachAppender();
    try (AnnotationConfigApplicationContext context = contextWithProperty(null)) {
      context.refresh();

      boolean warned =
          appender.list.stream()
              .anyMatch(
                  event ->
                      event.getLevel() == Level.WARN
                          && event
                              .getFormattedMessage()
                              .contains("sigdi.siadap.self-evaluation-opener-employee-ids"));
      assertTrue(warned);
    } finally {
      detachAppender(appender);
    }
  }

  @Test
  void isSelfEvaluationOpener_nullIsSafeAndReturnsFalse() {
    try (AnnotationConfigApplicationContext context = contextWithProperty(ID_1)) {
      context.refresh();
      SiadapSelfEvaluationOpenerSecurityProperties properties =
          context.getBean(SiadapSelfEvaluationOpenerSecurityProperties.class);

      assertFalse(properties.isSelfEvaluationOpener(null));
    }
  }

  @Test
  void isSelfEvaluationOpener_blankIsSafeAndReturnsFalse() {
    try (AnnotationConfigApplicationContext context = contextWithProperty(ID_1)) {
      context.refresh();
      SiadapSelfEvaluationOpenerSecurityProperties properties =
          context.getBean(SiadapSelfEvaluationOpenerSecurityProperties.class);

      assertFalse(properties.isSelfEvaluationOpener("   "));
    }
  }

  @Test
  void whenPropertyHasSingleUuid_matchesInUppercase_caseInsensitiveComparison() {
    try (AnnotationConfigApplicationContext context = contextWithProperty(ID_1)) {
      context.refresh();
      SiadapSelfEvaluationOpenerSecurityProperties properties =
          context.getBean(SiadapSelfEvaluationOpenerSecurityProperties.class);

      assertTrue(properties.isSelfEvaluationOpener(ID_1.toUpperCase(java.util.Locale.ROOT)));
      assertEquals(1, properties.getConfiguredCount());
    }
  }

  @Test
  void whenPropertyHasSpacesAndTrailingComma_normalizesToRealEntriesOnly() {
    try (AnnotationConfigApplicationContext context =
        contextWithProperty(" " + ID_1 + " , , " + ID_2 + " , ")) {
      context.refresh();
      SiadapSelfEvaluationOpenerSecurityProperties properties =
          context.getBean(SiadapSelfEvaluationOpenerSecurityProperties.class);

      assertEquals(2, properties.getConfiguredCount());
      assertTrue(properties.isSelfEvaluationOpener(ID_1));
      assertTrue(properties.isSelfEvaluationOpener(ID_2));
      assertFalse(properties.isSelfEvaluationOpener(""));
    }
  }

  @Test
  void whenUuidNotConfigured_isSelfEvaluationOpenerReturnsFalse() {
    try (AnnotationConfigApplicationContext context = contextWithProperty(ID_1)) {
      context.refresh();
      SiadapSelfEvaluationOpenerSecurityProperties properties =
          context.getBean(SiadapSelfEvaluationOpenerSecurityProperties.class);

      assertFalse(properties.isSelfEvaluationOpener(ID_3));
    }
  }

  @Test
  void whenPropertyNotSet_isSelfEvaluationOpenerCallEmitsWarnNamingTheProperty() {
    try (AnnotationConfigApplicationContext context = contextWithProperty(null)) {
      context.refresh();
      SiadapSelfEvaluationOpenerSecurityProperties properties =
          context.getBean(SiadapSelfEvaluationOpenerSecurityProperties.class);

      ListAppender<ILoggingEvent> appender = attachAppender();
      try {
        assertFalse(properties.isSelfEvaluationOpener(ID_1));

        boolean warned =
            appender.list.stream()
                .anyMatch(
                    event ->
                        event.getLevel() == Level.WARN
                            && event
                                .getFormattedMessage()
                                .contains("sigdi.siadap.self-evaluation-opener-employee-ids"));
        assertTrue(warned);
      } finally {
        detachAppender(appender);
      }
    }
  }

  @Test
  void whenPropertyConfigured_noWarnIsEmittedAtStartup() {
    ListAppender<ILoggingEvent> appender = attachAppender();
    try (AnnotationConfigApplicationContext context = contextWithProperty(ID_1)) {
      context.refresh();

      boolean warned = appender.list.stream().anyMatch(event -> event.getLevel() == Level.WARN);
      assertFalse(warned);
    } finally {
      detachAppender(appender);
    }
  }
}
