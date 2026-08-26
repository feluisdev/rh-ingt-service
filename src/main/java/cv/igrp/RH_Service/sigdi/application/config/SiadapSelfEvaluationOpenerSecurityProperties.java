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
 * Single source of truth for "is this employee allowed to open the self-evaluation phase by
 * hand?". This is a deliberately separate concept from {@link SiadapCcaSecurityProperties}
 * (CCA membership): "belongs to the CCA" and "may open the self-evaluation by hand" answer
 * different questions about different people, and sharing one list would make a product
 * decision by accident of implementation. The mechanism -- a flat, fail-closed list of
 * FuncionarioId values in configuration -- is the same; the concept is not.
 *
 * <p>Membership is a flat list of FuncionarioId values in configuration, by explicit planner
 * decision D-05 (see {@code 111-02-PLAN.md}): empty by default, and while empty nobody may open
 * a self-evaluation manually. There is no honest non-empty default -- any UUID hardcoded here
 * would belong to one concrete database.
 *
 * <p>Follows the {@code SiadapCcaSecurityProperties} mould: a plain {@code @Component} with a
 * {@code @Value} field, not {@code @ConfigurationProperties} -- this codebase does not use that
 * mechanism anywhere.
 */
@Component("siadapSelfEvaluationOpenerSecurityProperties")
public class SiadapSelfEvaluationOpenerSecurityProperties {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(SiadapSelfEvaluationOpenerSecurityProperties.class);
  private static final String PROPERTY_NAME = "sigdi.siadap.self-evaluation-opener-employee-ids";
  private static final String ENV_VAR_NAME = "SIADAP_SELF_EVALUATION_OPENER_EMPLOYEE_IDS";

  @Value("${sigdi.siadap.self-evaluation-opener-employee-ids:}")
  private String rawOpenerEmployeeIds;

  private Set<String> normalizedOpenerEmployeeIds = Collections.emptySet();

  // Pitfall: "".split(",") in Java returns a one-element array containing the empty string,
  // not an empty array. Binding this property through SpEL (@Value("#{'${...}'.split(',')}"))
  // would hide that trap inside an annotation no test can reach. Normalizing explicitly here,
  // discarding blanks, keeps the trap visible and assertable -- do not "simplify" this back to
  // the SpEL form.
  @PostConstruct
  void normalizeConfiguredEmployeeIds() {
    Set<String> normalized = new LinkedHashSet<>();
    if (rawOpenerEmployeeIds != null) {
      for (String candidate : rawOpenerEmployeeIds.split(",")) {
        String trimmed = candidate.trim();
        if (!trimmed.isEmpty()) {
          normalized.add(trimmed.toLowerCase(Locale.ROOT));
        }
      }
    }
    this.normalizedOpenerEmployeeIds = Collections.unmodifiableSet(normalized);

    if (normalizedOpenerEmployeeIds.isEmpty()) {
      LOGGER.warn(
          "Property '{}' (environment variable '{}') is not set: no employee is recognised as "
              + "a self-evaluation opener, so the manual self-evaluation opening endpoint "
              + "(POST siadap/evaluations/.../self-evaluation/open) will return 403 to every "
              + "caller.",
          PROPERTY_NAME,
          ENV_VAR_NAME);
    } else {
      LOGGER.info(
          "Self-evaluation opener list configured with {} employee id(s).",
          normalizedOpenerEmployeeIds.size());
    }
  }

  /**
   * Returns whether {@code funcionarioId} is allowed to open the self-evaluation phase by hand.
   * Blank/null-safe: returns {@code false} before any comparison. Fail-closed: an empty
   * configured set makes this return {@code false} for every caller, and logs a WARN naming the
   * missing property -- the HTTP response stays an identical 403 either way; only the log
   * distinguishes "nobody configured" from "you are not on the list".
   */
  public boolean isSelfEvaluationOpener(String funcionarioId) {
    if (funcionarioId == null || funcionarioId.trim().isEmpty()) {
      return false;
    }
    if (normalizedOpenerEmployeeIds.isEmpty()) {
      LOGGER.warn(
          "Refusing self-evaluation opener check because property '{}' (environment variable "
              + "'{}') is not set: no employee is recognised as a self-evaluation opener.",
          PROPERTY_NAME,
          ENV_VAR_NAME);
      return false;
    }
    boolean opener =
        normalizedOpenerEmployeeIds.contains(funcionarioId.trim().toLowerCase(Locale.ROOT));
    if (!opener) {
      LOGGER.debug("Employee '{}' is not a configured self-evaluation opener.", funcionarioId);
    }
    return opener;
  }

  public boolean isConfigured() {
    return !normalizedOpenerEmployeeIds.isEmpty();
  }

  public int getConfiguredCount() {
    return normalizedOpenerEmployeeIds.size();
  }
}
