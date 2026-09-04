package cv.igrp.RH_Service.shared.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * The project's single method-security enabling declaration, conditional on
 * {@link SecurityMode#isSecurityDisabled} via {@link MethodSecurityCondition}. When this class is
 * not registered, {@code @PreAuthorize} annotations are never evaluated — no AOP proxy is
 * created, so calls go straight through instead of being denied.
 */
@Configuration
@EnableMethodSecurity
@Conditional(MethodSecurityCondition.class)
class MethodSecurityConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodSecurityConfig.class);

  MethodSecurityConfig() {
    LOGGER.info("Method security ENABLED - @PreAuthorize is enforced.");
  }
}
