package cv.igrp.RH_Service.shared.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * Covers the six profile x {@code app.security.enabled} combinations that decide whether
 * {@link MethodSecurityConfig} — and with it the project's single method-security-enabling
 * declaration — gets registered.
 *
 * <p>Deliberately uses {@link ApplicationContextRunner} instead of {@code @SpringBootTest}:
 * booting the full application context would run Flyway against the operator's database, and
 * this test has to run on the fast per-task-commit cycle.</p>
 *
 * <p><b>Self-verifying note on profile activation, read before "fixing" this test.</b> Profile
 * activation here relies on {@code ApplicationContextRunner#withPropertyValues
 * ("spring.profiles.active=...")}. If that mechanism ever stopped producing an active profile,
 * the context would carry no active profiles, {@code Environment#matchesProfiles("development")}
 * would return {@code false}, {@link SecurityMode#isSecurityDisabled} would return {@code false},
 * and {@link #developmentProfile_securityEnabledFalse_methodSecurityConfigIsAbsent()} — which
 * expects {@link MethodSecurityConfig} to be ABSENT — would fail. No separate test of the
 * activation mechanism is required: that first case already fails loudly if activation breaks.
 * Do not "fix" that failure by loosening the assertion — it means the mechanism broke.</p>
 */
class MethodSecurityConditionTest {

  private final ApplicationContextRunner runner =
      new ApplicationContextRunner().withUserConfiguration(MethodSecurityConfig.class);

  @Test
  void developmentProfile_securityEnabledFalse_methodSecurityConfigIsAbsent() {
    runner
        .withPropertyValues("spring.profiles.active=development", "app.security.enabled=false")
        .run(context -> assertThat(context).doesNotHaveBean(MethodSecurityConfig.class));
  }

  @Test
  void developmentProfile_securityEnabledTrue_methodSecurityConfigIsPresent() {
    runner
        .withPropertyValues("spring.profiles.active=development", "app.security.enabled=true")
        .run(context -> {
          assertThat(context).hasNotFailed();
          assertThat(context).hasSingleBean(MethodSecurityConfig.class);
          // AuthorizationManagerBeforeMethodInterceptor is not independently resolvable as a
          // bean type here: Spring Security 6.5.1's PrePostMethodSecurityConfiguration wraps it
          // inside a DeferringMethodInterceptor (to support @Lazy circular-dependency
          // resolution), and only that wrapper is registered under
          // context.getBean(AuthorizationManagerBeforeMethodInterceptor.class) — confirmed by
          // printing context.getBeanDefinitionNames() against this exact configuration during
          // authoring of this test (see 108-02-SUMMARY.md for the full bean list). The stable,
          // @PreAuthorize-specific substitute used here is the named bean
          // "preAuthorizeAuthorizationMethodInterceptor", which
          // PrePostMethodSecurityConfiguration always registers when method security is active.
          // Removing @EnableMethodSecurity from MethodSecurityConfig removes this bean too, so
          // this assertion still catches an empty configuration class.
          assertThat(context.containsBean("preAuthorizeAuthorizationMethodInterceptor")).isTrue();
        });
  }

  @Test
  void developmentProfile_securityEnabledUnset_methodSecurityConfigIsPresent() {
    runner
        .withPropertyValues("spring.profiles.active=development")
        .run(context -> {
          assertThat(context).hasNotFailed();
          assertThat(context).hasSingleBean(MethodSecurityConfig.class);
        });
  }

  @Test
  void stagingProfile_securityEnabledFalse_methodSecurityConfigIsPresent() {
    runner
        .withPropertyValues("spring.profiles.active=staging", "app.security.enabled=false")
        .run(context -> {
          assertThat(context).hasNotFailed();
          assertThat(context).hasSingleBean(MethodSecurityConfig.class);
        });
  }

  @Test
  void productionProfile_securityEnabledFalse_methodSecurityConfigIsPresent() {
    runner
        .withPropertyValues("spring.profiles.active=production", "app.security.enabled=false")
        .run(context -> {
          assertThat(context).hasNotFailed();
          assertThat(context).hasSingleBean(MethodSecurityConfig.class);
        });
  }

  @Test
  void noActiveProfile_securityEnabledFalse_methodSecurityConfigIsPresent() {
    runner
        .withPropertyValues("app.security.enabled=false")
        .run(context -> {
          assertThat(context).hasNotFailed();
          assertThat(context).hasSingleBean(MethodSecurityConfig.class);
        });
  }
}
