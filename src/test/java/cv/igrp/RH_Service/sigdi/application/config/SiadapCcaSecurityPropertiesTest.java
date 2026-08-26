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

class SiadapCcaSecurityPropertiesTest {

  private static final String ID_1 = "11111111-1111-1111-1111-111111111111";
  private static final String ID_2 = "22222222-2222-2222-2222-222222222222";
  private static final String ID_3 = "33333333-3333-3333-3333-333333333333";

  private static AnnotationConfigApplicationContext contextWithProperty(String value) {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    if (value != null) {
      MockPropertySource mockPropertySource =
          new MockPropertySource().withProperty("sigdi.siadap.cca-employee-ids", value);
      context.getEnvironment().getPropertySources().addFirst(mockPropertySource);
    }
    context.register(PropertySourcesPlaceholderConfigurer.class, SiadapCcaSecurityProperties.class);
    return context;
  }

  // First ListAppender in this repository: it exists because a silent WARN is precisely
  // the defect this plan corrects, and no other test in the codebase asserts log output.
  private static ListAppender<ILoggingEvent> attachAppender() {
    Logger logger = (Logger) LoggerFactory.getLogger(SiadapCcaSecurityProperties.class);
    ListAppender<ILoggingEvent> appender = new ListAppender<>();
    appender.start();
    logger.addAppender(appender);
    return appender;
  }

  private static void detachAppender(ListAppender<ILoggingEvent> appender) {
    Logger logger = (Logger) LoggerFactory.getLogger(SiadapCcaSecurityProperties.class);
    logger.detachAppender(appender);
    appender.stop();
  }

  @Test
  void whenPropertyNotSet_isCcaIsFalseForAnyId_andIsConfiguredFalse_andCountIsZero() {
    try (AnnotationConfigApplicationContext context = contextWithProperty(null)) {
      context.refresh();
      SiadapCcaSecurityProperties properties = context.getBean(SiadapCcaSecurityProperties.class);

      assertFalse(properties.isCca(ID_1));
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
                          && event.getFormattedMessage().contains("sigdi.siadap.cca-employee-ids"));
      assertTrue(warned);
    } finally {
      detachAppender(appender);
    }
  }

  @Test
  void whenPropertyHasSingleUuid_isCcaMatchesOnlyThatUuid_andCountIsOne() {
    try (AnnotationConfigApplicationContext context = contextWithProperty(ID_1)) {
      context.refresh();
      SiadapCcaSecurityProperties properties = context.getBean(SiadapCcaSecurityProperties.class);

      assertTrue(properties.isCca(ID_1));
      assertFalse(properties.isCca(ID_2));
      assertEquals(1, properties.getConfiguredCount());
    }
  }

  @Test
  void whenPropertyHasTwoUuids_isCcaMatchesBoth_andCountIsTwo() {
    try (AnnotationConfigApplicationContext context = contextWithProperty(ID_1 + "," + ID_2)) {
      context.refresh();
      SiadapCcaSecurityProperties properties = context.getBean(SiadapCcaSecurityProperties.class);

      assertTrue(properties.isCca(ID_1));
      assertTrue(properties.isCca(ID_2));
      assertFalse(properties.isCca(ID_3));
      assertEquals(2, properties.getConfiguredCount());
    }
  }

  @Test
  void whenPropertyHasSpacesAndTrailingComma_normalizesToTwoElements_andEmptyStringIsNotAMember() {
    try (AnnotationConfigApplicationContext context =
        contextWithProperty(" " + ID_1 + " , , " + ID_2 + " , ")) {
      context.refresh();
      SiadapCcaSecurityProperties properties = context.getBean(SiadapCcaSecurityProperties.class);

      assertEquals(2, properties.getConfiguredCount());
      assertFalse(properties.isCca(""));
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

  @Test
  void isCca_nullIsSafeAndReturnsFalse() {
    try (AnnotationConfigApplicationContext context = contextWithProperty(ID_1)) {
      context.refresh();
      SiadapCcaSecurityProperties properties = context.getBean(SiadapCcaSecurityProperties.class);

      assertFalse(properties.isCca(null));
    }
  }

  @Test
  void isCca_blankIsSafeAndReturnsFalse() {
    try (AnnotationConfigApplicationContext context = contextWithProperty(ID_1)) {
      context.refresh();
      SiadapCcaSecurityProperties properties = context.getBean(SiadapCcaSecurityProperties.class);

      assertFalse(properties.isCca("  "));
    }
  }

  @Test
  void whenPropertyNotSet_isCcaCallEmitsWarnNamingTheProperty() {
    try (AnnotationConfigApplicationContext context = contextWithProperty(null)) {
      context.refresh();
      SiadapCcaSecurityProperties properties = context.getBean(SiadapCcaSecurityProperties.class);

      ListAppender<ILoggingEvent> appender = attachAppender();
      try {
        assertFalse(properties.isCca(ID_1));

        boolean warned =
            appender.list.stream()
                .anyMatch(
                    event ->
                        event.getLevel() == Level.WARN
                            && event
                                .getFormattedMessage()
                                .contains("sigdi.siadap.cca-employee-ids"));
        assertTrue(warned);
      } finally {
        detachAppender(appender);
      }
    }
  }

  @Test
  void whenPropertyConfiguredAndCallerIsNotAMember_isCcaReturnsFalse_andNoWarnIsEmitted() {
    try (AnnotationConfigApplicationContext context = contextWithProperty(ID_1)) {
      context.refresh();
      SiadapCcaSecurityProperties properties = context.getBean(SiadapCcaSecurityProperties.class);

      ListAppender<ILoggingEvent> appender = attachAppender();
      try {
        assertFalse(properties.isCca(ID_2));

        boolean warned = appender.list.stream().anyMatch(event -> event.getLevel() == Level.WARN);
        assertFalse(warned);
      } finally {
        detachAppender(appender);
      }
    }
  }

  @Test
  void isCca_caseInsensitiveComparison() {
    String uuidUppercase = "AAAAAAAA-BBBB-CCCC-DDDD-EEEEEEEEEEEE";
    try (AnnotationConfigApplicationContext context = contextWithProperty(uuidUppercase)) {
      context.refresh();
      SiadapCcaSecurityProperties properties = context.getBean(SiadapCcaSecurityProperties.class);

      assertTrue(properties.isCca(uuidUppercase.toLowerCase(java.util.Locale.ROOT)));
    }
  }
}
