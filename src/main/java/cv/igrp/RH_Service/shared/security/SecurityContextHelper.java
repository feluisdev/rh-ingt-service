package cv.igrp.RH_Service.shared.security;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.core.env.Environment;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.util.Arrays;
import java.util.UUID;

@Component
public class SecurityContextHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(SecurityContextHelper.class);

  @Autowired
  private Environment environment;

  // Fallback UUID used in development/staging when no institution_id claim is present in the JWT.
  private static final UUID DEV_INSTITUTION_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000001");

  private HttpServletRequest getRequest() {
    var attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    return attributes != null ? attributes.getRequest() : null;
  }

  public UUID getCurrentInstitutionId() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.isAuthenticated()
        && authentication.getPrincipal() instanceof Jwt jwt) {
      String institutionId = jwt.getClaimAsString("institution_id");
      if (institutionId != null) {
        LOGGER.debug("Found institution_id in JWT: {}", institutionId);
        return UUID.fromString(institutionId);
      }
    }

    // Support for debug header in non-production environments
    HttpServletRequest request = getRequest();
    if (request != null && isNonProduction()) {
      String debugInstId = request.getHeader("X-Institution-Id");
      if (debugInstId != null && !debugInstId.isBlank()) {
        try {
          UUID uuid = UUID.fromString(debugInstId);
          LOGGER.info("Using institution ID from debug header: {}", uuid);
          return uuid;
        } catch (IllegalArgumentException e) {
          LOGGER.warn("Invalid UUID in X-Institution-Id header: {}", debugInstId);
        }
      }
    }

    if (isNonProduction()) {
      LOGGER.debug("No institution_id found, returning default DEV_INSTITUTION_ID: {}", DEV_INSTITUTION_ID);
      return DEV_INSTITUTION_ID;
    }

    LOGGER.warn("No institution_id found in security context and not in dev/staging profile. Active profiles: {}", 
        Arrays.toString(environment.getActiveProfiles()));
    return null;
  }

  private boolean isNonProduction() {
    return environment.acceptsProfiles(org.springframework.core.env.Profiles.of("development", "staging", "dev", "local"));
  }

  public String getCurrentUserId() {
    if (isNonProduction()) {
      return "system";
    }
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()
        || "anonymousUser".equals(authentication.getPrincipal())) {
      return "system";
    }
    return authentication.getName();
  }
}
