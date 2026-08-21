package cv.igrp.RH_Service.sigdi.application.config;

import jakarta.annotation.PostConstruct;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Single source of truth for "is this employee a member of the CCA (Conselho Coordenador
 * da Avaliacao)?". Membership is a flat, global list of FuncionarioId values in
 * configuration, by explicit operator decision -- no organic-unit scoping, no IAM role.
 * See docs/07-Verificacao-de-Ator-SIADAP.md for the decision record.
 *
 * <p>Follows the {@code PaaSecurityProperties} mould: a plain {@code @Component} with a
 * {@code @Value} field, not {@code @ConfigurationProperties} -- this codebase does not use
 * that mechanism anywhere.
 */
@Component("siadapCcaSecurityProperties")
public class SiadapCcaSecurityProperties {

  private static final Logger LOGGER = LoggerFactory.getLogger(SiadapCcaSecurityProperties.class);
  private static final String PROPERTY_NAME = "sigdi.siadap.cca-employee-ids";
  private static final String ENV_VAR_NAME = "SIADAP_CCA_EMPLOYEE_IDS";

  @Value("${sigdi.siadap.cca-employee-ids:}")
  private String rawCcaEmployeeIds;

  private Set<String> normalizedCcaEmployeeIds = Collections.emptySet();

  // Pitfall: "".split(",") in Java returns a one-element array containing the empty
  // string, not an empty array. Binding this property through SpEL
  // (@Value("#{'${...}'.split(',')}")) would hide that trap inside an annotation no test
  // can reach. Normalizing explicitly here, discarding blanks, keeps the trap visible and
  // assertable -- do not "simplify" this back to the SpEL form.
  @PostConstruct
  void normalizeConfiguredEmployeeIds() {
    Set<String> normalized = new LinkedHashSet<>();
    if (rawCcaEmployeeIds != null) {
      for (String candidate : rawCcaEmployeeIds.split(",")) {
        String trimmed = candidate.trim();
        if (!trimmed.isEmpty()) {
          normalized.add(trimmed.toLowerCase(Locale.ROOT));
        }
      }
    }
    this.normalizedCcaEmployeeIds = Collections.unmodifiableSet(normalized);

    if (normalizedCcaEmployeeIds.isEmpty()) {
      LOGGER.warn(
          "Property '{}' (environment variable '{}') is not set: no employee is recognised "
              + "as a CCA member, so assigning merit ratings and closing evaluation cycles "
              + "will return 403 to every caller.",
          PROPERTY_NAME,
          ENV_VAR_NAME);
    } else {
      LOGGER.info("CCA membership configured with {} employee id(s).", normalizedCcaEmployeeIds.size());
    }
  }

  /**
   * Returns whether {@code funcionarioId} belongs to the configured CCA membership.
   * Blank/null-safe: returns {@code false} before any comparison. Fail-closed: an empty
   * configured set makes this return {@code false} for every caller, and logs a WARN
   * naming the missing property -- the HTTP response stays an identical 403 either way;
   * only the log distinguishes "nobody configured" from "you are not a member".
   */
  public boolean isCca(String funcionarioId) {
    if (funcionarioId == null || funcionarioId.trim().isEmpty()) {
      return false;
    }
    if (normalizedCcaEmployeeIds.isEmpty()) {
      LOGGER.warn(
          "Refusing CCA check because property '{}' (environment variable '{}') is not set: "
              + "no employee is recognised as a CCA member.",
          PROPERTY_NAME,
          ENV_VAR_NAME);
      return false;
    }
    boolean member = normalizedCcaEmployeeIds.contains(funcionarioId.trim().toLowerCase(Locale.ROOT));
    if (!member) {
      LOGGER.debug("Employee '{}' is not a configured CCA member.", funcionarioId);
    }
    return member;
  }

  public boolean isConfigured() {
    return !normalizedCcaEmployeeIds.isEmpty();
  }

  public int getConfiguredCount() {
    return normalizedCcaEmployeeIds.size();
  }
}
