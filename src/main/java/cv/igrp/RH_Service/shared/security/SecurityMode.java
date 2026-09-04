package cv.igrp.RH_Service.shared.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.env.Environment;

/**
 * Single source of truth for whether request authorization is disabled.
 *
 * <p>SECURITY_ENABLED=false is only honoured in the development profile. In staging/production
 * the flag is ignored and auth is always enforced.</p>
 *
 * <p>This function has two consumers: the HTTP filter chain ({@link SecurityConfig}) and the
 * method-security condition ({@link MethodSecurityCondition}). It exists as a single function,
 * consulted by both, precisely because they used to disagree — the HTTP layer honoured this
 * rule while the method-security layer ignored it entirely, leaving {@code @PreAuthorize} guards
 * enforced against an anonymous principal even with the security switch off. Anyone editing this
 * function is editing both layers at once.</p>
 */
final class SecurityMode {

  private static final Logger log = LoggerFactory.getLogger(SecurityMode.class);

  static final String SECURITY_ENABLED_PROPERTY = "app.security.enabled";
  static final String DEVELOPMENT_PROFILE = "development";

  private SecurityMode() {
  }

  /**
   * Evaluated during bean-definition registration — must never throw. A missing, malformed or
   * empty property resolves to "security enabled" (the safe default).
   *
   * @param environment the Spring {@link Environment} to read the property and active profiles from
   * @return {@code true} only when {@code app.security.enabled} is {@code false} AND the active
   *     profile is {@code development}
   */
  static boolean isSecurityDisabled(Environment environment) {
    boolean securityEnabled;
    try {
      securityEnabled = environment.getProperty(SECURITY_ENABLED_PROPERTY, Boolean.class, true);
    } catch (ConversionException ex) {
      String rawValue = environment.getProperty(SECURITY_ENABLED_PROPERTY);
      log.warn(
          "Malformed value for '{}': '{}'. Falling back to security enabled (the safe default) "
              + "instead of failing application startup.",
          SECURITY_ENABLED_PROPERTY, rawValue, ex);
      securityEnabled = true;
    }
    return !securityEnabled && environment.matchesProfiles(DEVELOPMENT_PROFILE);
  }
}
