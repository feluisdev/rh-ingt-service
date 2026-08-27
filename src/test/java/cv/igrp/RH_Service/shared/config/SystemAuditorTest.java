package cv.igrp.RH_Service.shared.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Cobertura de {@link SystemAuditor}: o âmbito de autor de sistema por thread que a Fase 117
 * introduz para que um agendador se possa nomear na auditoria sem haver utilizador
 * autenticado. JUnit 5 puro, sem Mockito e sem Spring -- não há nada aqui que precise de um
 * contexto de aplicação.
 *
 * <p>{@link SecurityContextHolder} é ele próprio apoiado num {@code ThreadLocal}, e o Surefire
 * reutiliza threads entre classes de teste. Sem limpar o contexto em {@code @BeforeEach} e em
 * {@code @AfterEach}, a asserção sobre o fallback do {@link ApplicationAuditorAware} ficaria
 * dependente da ordem de execução das classes de teste.
 */
class SystemAuditorTest {

    @BeforeEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void clearSecurityContextAfter() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void currentIsEmptyOutsideAnyScope() {
        assertTrue(SystemAuditor.current().isEmpty());
    }

    @Test
    void applicationAuditorAwareFallsBackToSystemBotOutsideAnyScope() {
        ApplicationAuditorAware auditorAware = new ApplicationAuditorAware();

        Optional<String> auditor = auditorAware.getCurrentAuditor();

        assertTrue(auditor.isPresent());
        assertEquals("system-bot@nosi.cv", auditor.get());
    }

    @Test
    void currentReturnsTheNamedAuditorInsideRunAs() {
        SystemAuditor.runAs("scheduler:period-expiry", () ->
                assertEquals(Optional.of("scheduler:period-expiry"), SystemAuditor.current()));
    }

    @Test
    void applicationAuditorAwareReturnsTheNamedAuditorInsideRunAs() {
        ApplicationAuditorAware auditorAware = new ApplicationAuditorAware();

        SystemAuditor.runAs("scheduler:period-expiry", () -> {
            Optional<String> auditor = auditorAware.getCurrentAuditor();
            assertTrue(auditor.isPresent());
            assertEquals("scheduler:period-expiry", auditor.get());
        });
    }

    @Test
    void currentIsEmptyAgainAfterRunAsReturns() {
        SystemAuditor.runAs("scheduler:period-expiry", () -> {
        });

        assertTrue(SystemAuditor.current().isEmpty());
    }

    @Test
    void scopeIsClearedEvenWhenTheActionThrows() {
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                SystemAuditor.runAs("scheduler:period-expiry", () -> {
                    throw new RuntimeException("falha proposital de teste");
                }));

        assertEquals("falha proposital de teste", thrown.getMessage());
        assertTrue(SystemAuditor.current().isEmpty());
    }

    @Test
    void nestedRunAsRestoresTheOuterValueOnExitingTheInnerScope() {
        SystemAuditor.runAs("scheduler:outer", () -> {
            SystemAuditor.runAs("scheduler:inner", () ->
                    assertEquals(Optional.of("scheduler:inner"), SystemAuditor.current()));

            assertEquals(Optional.of("scheduler:outer"), SystemAuditor.current());
        });

        assertFalse(SystemAuditor.current().isPresent());
    }
}
