package cv.igrp.RH_Service.shared.application.services.scheduler;

/**
 * Estado de uma execução registada em {@code t_scheduler_execucao}.
 *
 * <p>A distinção entre {@link #FALHA} e {@link #FALHA_PARCIAL} não é cosmética: decide se há retry
 * automático. Uma excepção que mata o job é quase sempre transitória (BD em baixo, timeout de rede)
 * e vale a pena repetir; itens que falharam por regra de negócio vão falhar outra vez, e repetir o
 * job só desperdiça trabalho e atrasa o momento em que alguém corrige o dado.
 */
public enum EstadoExecucao {

    /** Registo aberto no arranque. Se ficar preso aqui, o processo morreu a meio — o sweeper fecha-o. */
    A_CORRER,
    /** Terminou sem falhas. */
    SUCESSO,
    /** Terminou, mas com {@code falhas > 0} — erros por item, não fatais. Sem retry automático. */
    FALHA_PARCIAL,
    /** Abortou com excepção. Elegível a retry automático. */
    FALHA,
    /** Excedeu o {@code getTimeout()} do job. Elegível a retry automático. */
    TIMEOUT,
    /**
     * O cron devia ter disparado e não disparou (aplicação em baixo à hora prevista).
     * Linha sintética criada pelo sweeper — é o que torna visível uma execução que <em>não existe</em>.
     */
    OMITIDA;

    /** Estados que justificam retry automático. */
    public boolean permiteRetryAutomatico() {
        return this == FALHA || this == TIMEOUT;
    }

    public boolean isTerminal() {
        return this != A_CORRER;
    }
}
