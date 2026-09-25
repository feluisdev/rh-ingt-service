package cv.igrp.RH_Service.shared.application.services.scheduler;

import java.time.Duration;
import java.util.List;

/**
 * Contrato de um job agendado. É a única coisa que um programador escreve para ganhar
 * agendamento configurável pela UI, registo de execução, disparo manual parametrizado, detecção de
 * execuções em falta, guarda de concorrência entre réplicas, timeout e retry — tudo isso vive no
 * framework ({@code SchedulerService}, {@code JobRunner}, {@code SchedulerSweeper}), não no job.
 *
 * <p>Basta anotar a implementação com {@code @Component}: o {@code SchedulerService} descobre-a
 * por injecção de {@code List<ScheduledJob>} e semeia a configuração no arranque. Não se usa
 * {@code @Scheduled} — é o framework que agenda.
 *
 * <p>O job corre sob o autor de sistema {@code scheduler:<chave>} ({@code SystemAuditor}), para que a
 * auditoria distinga o que cada job escreveu.
 *
 * <p>Os métodos com {@code default} são opcionais — sobrepõe-se apenas o que o job precisa.
 */
public interface ScheduledJob {

    /** Identificador estável, em maiúsculas com underscore (ex.: {@code RH_VENCIMENTO_FERIAS}). */
    String getChave();

    /** Nome apresentado na interface. */
    String getNomeLegivel();

    /** Cron inicial (6 campos, estilo Spring). Depois de semeado, quem manda é a configuração em BD. */
    String getCronPadrao();

    /** A lógica de negócio. Tudo o resto é do framework. */
    JobResult executar(JobContext ctx);

    /**
     * Parâmetros que o job aceita no disparo manual. A UI gera o formulário a partir desta lista,
     * por isso um job novo com parâmetros novos não obriga a mexer no frontend.
     */
    default List<JobParametro> getParametros() {
        return List.of();
    }

    /**
     * Tentativas automáticas em caso de {@code FALHA} ou {@code TIMEOUT} (0 = sem retry automático).
     * Nunca se aplica a {@code FALHA_PARCIAL}: repetir o job inteiro voltaria a falhar exactamente
     * nos mesmos itens. Esses resolvem-se corrigindo o dado e re-executando à mão.
     */
    default int getMaxTentativas() {
        return 0;
    }

    /** Ao fim deste tempo a execução é marcada {@code TIMEOUT} e a guarda de concorrência é libertada. */
    default Duration getTimeout() {
        return Duration.ofMinutes(30);
    }

    /**
     * Estado com que o job é semeado <b>da primeira vez</b>. Só vale nesse instante: a partir daí quem
     * manda é a configuração em BD, alterável pela UI ({@code alterarEstado}).
     *
     * <p>Devolver {@code false} serve os jobs que devem existir na interface — e poder ser
     * disparados à mão — sem começarem a correr sozinhos no primeiro arranque em produção.
     */
    default boolean isActivoPorOmissao() {
        return true;
    }
}
