package cv.igrp.RH_Service.shared.application.services.scheduler;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JobContextTest {

    private static JobContext ctx(Map<String, Object> parametros, LocalDateTime agendadoPara) {
        return JobContext.of(parametros, agendadoPara, TipoDisparo.AGENDADO, null, null, 1);
    }

    @Test
    @DisplayName("sem parâmetro, o dia é o do agendamento — mesmo que a execução corra dias depois")
    void dataReferencia_derivaDoAgendado() {
        assertEquals(LocalDate.of(2026, 12, 31), ctx(Map.of(), LocalDateTime.of(2026, 12, 31, 0, 5)).dataReferencia());
    }

    @Test
    void dataReferencia_parametroExplicitoGanha_nosDoisFormatos() {
        var agendado = LocalDateTime.of(2026, 12, 31, 0, 5);
        assertEquals(LocalDate.of(2026, 9, 20), ctx(Map.of("data", "2026-09-20"), agendado).dataReferencia());
        assertEquals(LocalDate.of(2026, 9, 20), ctx(Map.of("data", "20/09/2026"), agendado).dataReferencia());
    }

    @Test
    void dataReferencia_invalida_recusada() {
        assertThrows(IgrpResponseStatusException.class,
                () -> ctx(Map.of("data", "ontem"), LocalDateTime.now()).dataReferencia());
        // Um dia que não existe é recusado, e não arredondado para o último dia do mês.
        assertThrows(IgrpResponseStatusException.class,
                () -> ctx(Map.of("data", "31/02/2026"), LocalDateTime.now()).dataReferencia());
        assertThrows(IgrpResponseStatusException.class,
                () -> ctx(Map.of("data", "2026-02-31"), LocalDateTime.now()).dataReferencia());
    }

    @Test
    void getInteger_eBoolean() {
        var c = ctx(Map.of("ano", "2026", "sim", "true"), LocalDateTime.now());
        assertEquals(2026, c.getInteger("ano"));
        assertEquals(true, c.getBoolean("sim", false));
        assertEquals(false, c.getBoolean("outro", false));
        assertThrows(IgrpResponseStatusException.class, () -> ctx(Map.of("ano", "x"), LocalDateTime.now()).getInteger("ano"));
    }

    @Test
    @DisplayName("o relógio do scheduler está em Cabo Verde (UTC-1), não na zona da JVM")
    void relogio_emCaboVerde() {
        var utc = LocalDateTime.now(ZoneOffset.UTC);
        var cv = RelogioScheduler.agora();
        long minutos = java.time.Duration.between(cv, utc).toMinutes();
        org.junit.jupiter.api.Assertions.assertTrue(Math.abs(minutos - 60) <= 1, "diferença: " + minutos);
    }

    @Test
    @DisplayName("o cron é lido no fuso do job, e a resposta volta na hora de Cabo Verde")
    void proximaDepoisDe_converteFusos() {
        var base = LocalDateTime.of(2026, 9, 25, 12, 0);
        // 00:05 em Cabo Verde
        assertEquals(LocalDateTime.of(2026, 9, 26, 0, 5),
                RelogioScheduler.proximaDepoisDe("0 5 0 * * *", "Atlantic/Cape_Verde", base));
        // 00:05 UTC = 23:05 em Cabo Verde do dia anterior
        assertEquals(LocalDateTime.of(2026, 9, 25, 23, 5),
                RelogioScheduler.proximaDepoisDe("0 5 0 * * *", "UTC", base));
    }
}
