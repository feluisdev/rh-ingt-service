package cv.igrp.RH_Service.shared.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Answers Open Question 2 of {@code 109-RESEARCH.md} (assumption A1): does
 * {@code spring.jpa.properties.org.hibernate.envers.default_schema=audit_schema},
 * set in the base {@code application.properties} (line 13), survive into the
 * {@code staging} and {@code production} profiles even though the same key is
 * present but <em>commented out</em> in {@code application-staging.properties}
 * (line 34) and {@code application-production.properties} (line 29)?
 *
 * <p>A commented-out line defines no key at all, so under normal Spring Boot
 * property-source precedence it cannot override the base file's value — the
 * base value must resolve in every profile. That is the claim under test.
 * This is what authorizes {@code V32} to create the Envers shadow table only
 * in {@code audit_schema}, with no schema hedge (decision D-07 of
 * {@code 109-01-PLAN.md}): if this test is wrong, production's audit table
 * would live in {@code public}, silently orphaned from the schema the
 * migration writes to.
 *
 * <p><strong>This test never starts an application context.</strong> It
 * resolves property sources for a given Spring profile by applying
 * {@link ConfigDataApplicationContextInitializer} to a
 * {@link StandardEnvironment} held by a {@link GenericApplicationContext}
 * that is <strong>never refreshed</strong> — {@code refresh()} is the call
 * that would instantiate beans, and it is deliberately never made. Using
 * {@code SpringApplicationBuilder(...).run()} or {@code SpringApplication.run(...)}
 * instead is forbidden for this purpose: {@code RecursosHumanosApplication} is
 * a plain {@code @SpringBootApplication} with zero autoconfiguration
 * exclusions, so either of those would autoconfigure the {@code DataSource}
 * and Flyway and, under the {@code development} profile
 * ({@code spring.flyway.enabled=true}, {@code ddl-auto=update}), would run
 * Flyway migrations against the operator's real database — the same class of
 * failure Phase 103 hit via {@code @SpringBootTest}, reached this time by a
 * path a simple {@code grep} for that annotation would not catch.
 */
class EnversAuditSchemaResolutionTest {

    private static final String ENVERS_SCHEMA_KEY =
        "spring.jpa.properties.org.hibernate.envers.default_schema";
    private static final String DDL_AUTO_KEY = "spring.jpa.hibernate.ddl-auto";
    private static final String FLYWAY_ENABLED_KEY = "spring.flyway.enabled";

    /**
     * Resolves the {@link StandardEnvironment} for the given Spring profile by
     * processing {@code application*.properties} property sources through
     * {@link ConfigDataApplicationContextInitializer}. The context is
     * constructed and its environment populated, but {@code refresh()} is
     * never called — no bean definition is ever instantiated, and no
     * {@code DataSource} or Flyway autoconfiguration is ever triggered.
     *
     * <p>{@code application.properties} (the base, profile-agnostic document)
     * itself declares {@code spring.profiles.active=${SPRING_ACTIVE_PROFILE:development}}.
     * Config data processing resolves that placeholder from the environment's
     * existing property sources and uses the result to select the active
     * profile — which silently overrides an active profile set beforehand via
     * {@link StandardEnvironment#setActiveProfiles}. To select the profile
     * under test deterministically, the {@code SPRING_ACTIVE_PROFILE} property
     * that the placeholder resolves against is injected as the
     * highest-priority property source before the initializer runs, exactly
     * as the real environment variable of the same name does at runtime.
     */
    private StandardEnvironment resolveEnvironmentForProfile(String profile) {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(
            new MapPropertySource("test-active-profile-selector",
                Map.of("SPRING_ACTIVE_PROFILE", profile)));

        GenericApplicationContext context = new GenericApplicationContext();
        context.setEnvironment(environment);
        new ConfigDataApplicationContextInitializer().initialize(context);
        // No context.refresh() call: only property sources are processed.

        return environment;
    }

    // --- Thesis assertions: envers.default_schema resolves to audit_schema in every profile ---

    @Test
    void enversDefaultSchema_resolvesToAuditSchema_emDevelopment() {
        StandardEnvironment environment = resolveEnvironmentForProfile("development");
        assertEquals("audit_schema", environment.getProperty(ENVERS_SCHEMA_KEY));
    }

    @Test
    void enversDefaultSchema_resolvesToAuditSchema_emStaging() {
        StandardEnvironment environment = resolveEnvironmentForProfile("staging");
        assertEquals("audit_schema", environment.getProperty(ENVERS_SCHEMA_KEY));
    }

    @Test
    void enversDefaultSchema_resolvesToAuditSchema_emProduction() {
        StandardEnvironment environment = resolveEnvironmentForProfile("production");
        assertEquals("audit_schema", environment.getProperty(ENVERS_SCHEMA_KEY));
    }

    // --- Control group: keys the profiles genuinely do override, proving per-profile
    // resolution is actually alive and not an inert Environment that would make the
    // thesis assertions above pass vacuously. ---

    @Test
    void ddlAuto_controlo_difereEntreDevelopmentEProduction() {
        StandardEnvironment production = resolveEnvironmentForProfile("production");
        StandardEnvironment development = resolveEnvironmentForProfile("development");

        assertEquals("validate", production.getProperty(DDL_AUTO_KEY));
        assertEquals("update", development.getProperty(DDL_AUTO_KEY));
    }

    @Test
    void flywayEnabled_controlo_difereEntreStagingEProduction() {
        StandardEnvironment staging = resolveEnvironmentForProfile("staging");
        StandardEnvironment production = resolveEnvironmentForProfile("production");

        assertEquals("false", staging.getProperty(FLYWAY_ENABLED_KEY));
        assertEquals("true", production.getProperty(FLYWAY_ENABLED_KEY));
    }
}
