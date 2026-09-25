package cv.igrp.RH_Service.shared.application.services.scheduler;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

/**
 * Resultado genérico de uma execução de job, devolvido por {@link ScheduledJob#executar} e
 * persistido pelo {@link JobRunner}. Os contadores são deliberadamente genéricos (servem qualquer
 * job):
 * <ul>
 *   <li>{@code processados} — total de itens considerados.</li>
 *   <li>{@code criados} — efeito novo (ex.: saldos criados, efeitos aplicados).</li>
 *   <li>{@code repetidos} — saltados por idempotência (já estavam feitos).</li>
 *   <li>{@code saltados} — saltados por regra de negócio.</li>
 *   <li>{@code falhas} — erros por item (não fatais); {@code > 0} marca a execução como FALHA_PARCIAL.</li>
 * </ul>
 * {@code referencia} guarda contexto de negócio livre (ex.: o ano {@code 2026}) e {@code detalhes}
 * permite a um job guardar métricas extra sem alterar o esquema.
 */
@Getter
@Builder
public class JobResult {

    @Builder.Default private final int processados = 0;
    @Builder.Default private final int criados = 0;
    @Builder.Default private final int repetidos = 0;
    @Builder.Default private final int saltados = 0;
    @Builder.Default private final int falhas = 0;

    private final String referencia;
    private final String mensagem;
    private final Map<String, Object> detalhes;

    /** Resultado vazio, para jobs que não têm contadores a reportar. */
    public static JobResult vazio() {
        return JobResult.builder().build();
    }
}
