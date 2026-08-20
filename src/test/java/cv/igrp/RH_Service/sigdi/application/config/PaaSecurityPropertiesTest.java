package cv.igrp.RH_Service.sigdi.application.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.mock.env.MockPropertySource;

class PaaSecurityPropertiesTest {

  @Test
  void getSubmissionPeriodRole_defaultsToRhWhenPropertyIsNotSet() {
    try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
      context.register(PropertySourcesPlaceholderConfigurer.class, PaaSecurityProperties.class);
      context.refresh();

      PaaSecurityProperties properties = context.getBean(PaaSecurityProperties.class);

      assertEquals("RH", properties.getSubmissionPeriodRole());
    }
  }

  @Test
  void getSubmissionPeriodRole_returnsOverrideWhenPropertyIsSet() {
    try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
      MockPropertySource mockPropertySource = new MockPropertySource()
          .withProperty("sigdi.paa.submission-period-role", "PAA_ADMIN");
      context.getEnvironment().getPropertySources().addFirst(mockPropertySource);
      context.register(PropertySourcesPlaceholderConfigurer.class, PaaSecurityProperties.class);
      context.refresh();

      PaaSecurityProperties properties = context.getBean(PaaSecurityProperties.class);

      assertEquals("PAA_ADMIN", properties.getSubmissionPeriodRole());
    }
  }
}
