package cv.igrp.RH_Service.shared.application.services.scheduler;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Tudo o que é preciso para abrir uma execução. Existe para o {@code JobRunner} não ter métodos de
 * sete argumentos posicionais, onde trocar {@code solicitante} com {@code referencia} compila e
 * só se descobre em produção.
 */
@Getter
@Builder
public class PedidoExecucao {

    private final ScheduledJob job;
    private final TipoDisparo disparo;
    private final String solicitante;
    private final Map<String, Object> parametros;

    /** Instante do cron a que a execução corresponde; se omitido, assume-se {@code now()}. */
    private final LocalDateTime agendadoPara;

    @Builder.Default private final int tentativa = 1;

    /** Execução que deu origem a esta (retry automático ou re-execução manual). */
    private final UUID execucaoPaiId;

    /**
     * Primeira tentativa de um disparo do cron. É a única que pode chegar em duplicado — o mesmo
     * instante dispara em todas as réplicas — e por isso a única que o registo deduplica.
     */
    public boolean isPrimeiroDisparoAgendado() {
        return disparo == TipoDisparo.AGENDADO && tentativa <= 1;
    }
}
