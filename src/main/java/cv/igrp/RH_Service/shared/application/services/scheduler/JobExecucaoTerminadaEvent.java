package cv.igrp.RH_Service.shared.application.services.scheduler;

import java.util.UUID;

/**
 * Publicado pelo {@code JobRunner} sempre que uma execução termina, qualquer que seja o desfecho.
 *
 * <p>Existe para manter o scheduler desacoplado das notificações: quando houver um módulo de
 * alertas, basta-lhe um {@code @EventListener} que filtre pelos estados que interessam. Sem esta
 * costura, mais cedo ou mais tarde alguém metia uma chamada ao serviço de email dentro do
 * {@code JobRunner}.
 *
 * @param chave      chave do job
 * @param execucao   uuid da execução
 * @param estado     desfecho
 * @param referencia contexto de negócio (ex.: {@code 2026}), quando o job o reporta
 * @param mensagem   resumo legível
 */
public record JobExecucaoTerminadaEvent(
        String chave,
        UUID execucao,
        EstadoExecucao estado,
        String referencia,
        String mensagem) {

    public boolean isFalha() {
        return estado == EstadoExecucao.FALHA
                || estado == EstadoExecucao.FALHA_PARCIAL
                || estado == EstadoExecucao.TIMEOUT;
    }
}
