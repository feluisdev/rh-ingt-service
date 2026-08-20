package cv.igrp.RH_Service.sigdi.application.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Exposes the role name required by {@code PaaSubmissionPeriodController} as a named bean,
 * so it can be referenced from the {@code @PreAuthorize} SpEL expression via
 * {@code @paaSecurityProperties.submissionPeriodRole}.
 *
 * The bean name "paaSecurityProperties" is explicit and load-bearing: it is the exact
 * identifier the SpEL expression resolves against, so renaming this bean without also
 * updating the controller silently breaks authorization.
 */
@Component("paaSecurityProperties")
public class PaaSecurityProperties {

  // "RH" is a best-effort guess at the Keycloak/IAM realm role name. No other endpoint
  // in this codebase does role-based authorization, so there is no internal convention
  // to confirm this against. Verify the exact role/authority string against the real
  // IAM realm configuration before this reaches production.
  @Value("${sigdi.paa.submission-period-role:RH}")
  private String submissionPeriodRole;

  public String getSubmissionPeriodRole() {
    return submissionPeriodRole;
  }
}
