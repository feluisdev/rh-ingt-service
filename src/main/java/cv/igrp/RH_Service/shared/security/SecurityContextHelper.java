package cv.igrp.RH_Service.shared.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SecurityContextHelper {

  @Value("${spring.profiles.active}")
  private String activeProfile;

  // Fallback UUID used in development/staging when no institution_id claim is present in the JWT.
  private static final UUID DEV_INSTITUTION_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000001");

  public UUID getCurrentInstitutionId() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.isAuthenticated()
        && authentication.getPrincipal() instanceof Jwt jwt) {
      String institutionId = jwt.getClaimAsString("institution_id");
      if (institutionId != null) {
        return UUID.fromString(institutionId);
      }
    }
    if ("development".equals(activeProfile) || "staging".equals(activeProfile)) {
      return DEV_INSTITUTION_ID;
    }
    return null;
  }

  public String getCurrentUserId() {
    if ("development".equals(activeProfile) || "staging".equals(activeProfile)) {
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
