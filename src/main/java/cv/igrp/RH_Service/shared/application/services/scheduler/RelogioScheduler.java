package cv.igrp.RH_Service.shared.application.services.scheduler;

import cv.igrp.RH_Service.shared.config.AppTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.support.CronExpression;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * O relógio do framework. <b>Todos</b> os instantes que o scheduler grava ({@code agendadoPara},
 * {@code inicio}, {@code proximaExecucao}, …) estão na hora de Cabo Verde ({@link AppTimeZone}) —
 * nunca na hora da JVM.
 *
 * <p>Porque é que isto não é um pormenor: o servidor corre em UTC e Cabo Verde está em UTC-1. Se a
 * {@code proximaExecucao} fosse calculada numa zona e comparada com {@code LocalDateTime.now()} na
 * outra, o sweeper veria cada disparo "atrasado" uma hora antes de ele acontecer e registaria
 * omissões falsas todas as noites; e o {@code JobContext} derivaria o dia errado entre as 23:00 e a
 * meia-noite. O fuso de cada job só serve para interpretar o cron.
 */
final class RelogioScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RelogioScheduler.class);
    static final ZoneId ZONA = AppTimeZone.CABO_VERDE;

    private RelogioScheduler() {
    }

    static LocalDateTime agora() {
        return LocalDateTime.now(ZONA);
    }

    static LocalDateTime de(Instant instante) {
        return LocalDateTime.ofInstant(instante, ZONA);
    }

    /**
     * Próximo disparo do cron depois de {@code base}. O cron é interpretado no fuso do job; a
     * resposta, como tudo o resto, vem na hora de Cabo Verde.
     */
    static LocalDateTime proximaDepoisDe(String cron, String timezone, LocalDateTime base) {
        try {
            var zonaDoJob = timezone != null && !timezone.isBlank() ? ZoneId.of(timezone) : ZONA;
            var proxima = CronExpression.parse(cron).next(base.atZone(ZONA).withZoneSameInstant(zonaDoJob));
            return proxima == null ? null : proxima.withZoneSameInstant(ZONA).toLocalDateTime();
        } catch (Exception e) {
            LOGGER.warn("Cron inválido ao calcular próxima execução ({}): {}", cron, e.getMessage());
            return null;
        }
    }
}
