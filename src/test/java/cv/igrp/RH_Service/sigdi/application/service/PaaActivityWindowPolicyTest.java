package cv.igrp.RH_Service.sigdi.application.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.shared.config.AppTimeZone;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.PaaSubmissionPeriod;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PaaSubmissionPeriodRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.time.Year;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Covers the single owner of "is the {@code Purpose.PAA} submission window of the current year
 * open for a given {@code PaaLevel}?" -- the criterion this class exists to keep identical to
 * {@code CreateTacticalActivityCommandHandler} and {@code UpdateTacticalActivityCommandHandler}
 * (D-27). Test 3 below is the one that would catch a diverging criterion; the other three would
 * still pass with a wrong purpose or a wrong year.
 */
@ExtendWith(MockitoExtension.class)
class PaaActivityWindowPolicyTest {

    private static final PaaLevel LEVEL = PaaLevel.UNIT_LEVEL;

    @Mock
    private PaaSubmissionPeriodRepository periodRepository;

    // Construído explicitamente, não por @InjectMocks: a classe ganhou um segundo construtor
    // (periodRepository, Clock) só para teste, e o Mockito escolhe sempre o construtor com
    // mais parâmetros -- injetaria um Clock nulo, porque não há nenhum @Mock Clock neste
    // ficheiro. Chamar o construtor de produção explicitamente evita a ambiguidade.
    private PaaActivityWindowPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new PaaActivityWindowPolicy(periodRepository);
    }

    @Test
    void requireOpenForDoesNotThrowWhenWindowIsActive() {
        PaaSubmissionPeriod activePeriod = mock(PaaSubmissionPeriod.class);
        when(periodRepository.findActiveByTypeAndYearAndPurpose(LEVEL, Year.now().getValue(), Purpose.PAA))
                .thenReturn(Optional.of(activePeriod));

        assertDoesNotThrow(() -> policy.requireOpenFor(LEVEL));
    }

    @Test
    void requireOpenForThrowsBadRequestWithTheProductMessageWhenWindowIsNotActive() {
        when(periodRepository.findActiveByTypeAndYearAndPurpose(LEVEL, Year.now().getValue(), Purpose.PAA))
                .thenReturn(Optional.empty());

        IgrpResponseStatusException exception =
                assertThrows(IgrpResponseStatusException.class, () -> policy.requireOpenFor(LEVEL));

        assertEquals(400, exception.getBody().getStatus());
        assertTrue(exception.getBody().getTitle().contains(
                "Prazo não configurado para a submissão de atividades do PAA"));
    }

    /**
     * The triplet assertion that D-27 exists to guarantee. Proves the query is made with exactly
     * {@code (paaLevel, Year.now().getValue(), Purpose.PAA)} -- the pair and year criterion from
     * the mould handlers, not any other permutation.
     */
    @Test
    void queriesWithExactlyTheCallerLevelCurrentYearAndPaaPurpose() {
        when(periodRepository.findActiveByTypeAndYearAndPurpose(LEVEL, Year.now().getValue(), Purpose.PAA))
                .thenReturn(Optional.empty());

        assertThrows(IgrpResponseStatusException.class, () -> policy.requireOpenFor(LEVEL));

        ArgumentCaptor<PaaLevel> levelCaptor = ArgumentCaptor.forClass(PaaLevel.class);
        ArgumentCaptor<Integer> yearCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Purpose> purposeCaptor = ArgumentCaptor.forClass(Purpose.class);
        Mockito.verify(periodRepository)
                .findActiveByTypeAndYearAndPurpose(levelCaptor.capture(), yearCaptor.capture(), purposeCaptor.capture());

        assertEquals(LEVEL, levelCaptor.getValue());
        assertEquals(Year.now().getValue(), yearCaptor.getValue());
        assertEquals(Purpose.PAA, purposeCaptor.getValue());
    }

    /**
     * Fail-closed with a null level: refuses with the same message, never a NullPointerException.
     * Proves the fail-closed behaviour is the one the spec describes and not an accident of the
     * mock returning empty for any argument.
     */
    @Test
    void requireOpenForRefusesWithTheSameMessageWhenLevelIsNull() {
        when(periodRepository.findActiveByTypeAndYearAndPurpose(null, Year.now().getValue(), Purpose.PAA))
                .thenReturn(Optional.empty());

        IgrpResponseStatusException exception =
                assertThrows(IgrpResponseStatusException.class, () -> policy.requireOpenFor(null));

        assertEquals(400, exception.getBody().getStatus());
        assertTrue(exception.getBody().getTitle().contains(
                "Prazo não configurado para a submissão de atividades do PAA"));
    }

    /**
     * T-136-36 (D-53): prova que o ano consultado é o de Cabo Verde, não o do sistema, no
     * instante exato em que os dois discordam -- 23:30 de 31 de dezembro em Cabo Verde é já
     * 00:30 de 1 de janeiro em UTC. {@code Year.now(ZoneId)} sozinho não chega para este teste:
     * fixa o fuso, mas não o instante, e sem fixar o instante o teste corre contra o relógio
     * real e só falharia (ou passaria por acidente) uma vez por ano. Por isso a política ganhou
     * um construtor de teste que aceita um {@link Clock} -- este teste fixa os dois ao mesmo
     * tempo.
     */
    @Test
    void requireOpenForUsesCapeVerdeYearNotUtcYearAtTheYearBoundary() {
        // 2025-12-31T23:30 em Cabo Verde (UTC-1) == 2026-01-01T00:30Z em UTC.
        Instant instantWhereCvAndUtcDisagree = Instant.parse("2026-01-01T00:30:00Z");
        Clock cvClockAtTheBoundary = Clock.fixed(instantWhereCvAndUtcDisagree, AppTimeZone.CABO_VERDE);

        // Confirma a premissa do teste: no mesmo instante, o fuso UTC já leria 2026.
        Clock utcClockAtTheSameInstant = Clock.fixed(instantWhereCvAndUtcDisagree, ZoneOffset.UTC);
        assertEquals(2025, Year.now(cvClockAtTheBoundary).getValue());
        assertEquals(2026, Year.now(utcClockAtTheSameInstant).getValue());

        PaaActivityWindowPolicy policyAtTheBoundary =
                new PaaActivityWindowPolicy(periodRepository, cvClockAtTheBoundary);

        when(periodRepository.findActiveByTypeAndYearAndPurpose(LEVEL, 2025, Purpose.PAA))
                .thenReturn(Optional.of(mock(PaaSubmissionPeriod.class)));

        assertDoesNotThrow(() -> policyAtTheBoundary.requireOpenFor(LEVEL));

        // A prova em si: o repositório foi interrogado com o ano de Cabo Verde (2025), nunca
        // com o ano UTC (2026) -- mesmo instante, fuso diferente, resposta diferente.
        Mockito.verify(periodRepository)
                .findActiveByTypeAndYearAndPurpose(LEVEL, 2025, Purpose.PAA);
        Mockito.verify(periodRepository, Mockito.never())
                .findActiveByTypeAndYearAndPurpose(LEVEL, 2026, Purpose.PAA);
    }
}
