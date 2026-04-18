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

  public UUID getCurrentInstitutionId() {
    if ("development".equals(activeProfile) || "staging".equals(activeProfile)) {
      return null;
    }
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()
        || "anonymousUser".equals(authentication.getPrincipal())) {
      return null;
    }
    if (authentication.getPrincipal() instanceof Jwt jwt) {
      String institutionId = jwt.getClaimAsString("institution_id");
      return institutionId != null ? UUID.fromString(institutionId) : null;
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
