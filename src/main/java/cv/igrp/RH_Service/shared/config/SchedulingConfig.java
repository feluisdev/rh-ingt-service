package cv.igrp.RH_Service.shared.config;

import cv.igrp.RH_Service.shared.application.services.scheduler.JobExecutores;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Pools do framework de jobs ({@code shared/application/services/scheduler}). O
 * {@code @EnableScheduling} fica na classe da aplicação, onde já estava.
 */
@Configuration
public class SchedulingConfig {

    /**
     * Dois pools, e a separação é o que torna o tempo limite possível: o thread do disparador
     * submete o trabalho ao segundo pool e fica à espera com prazo. Se corressem no mesmo pool,
     * esperar por um job ocuparia um dos threads que também têm de disparar os restantes — e, com
     * jobs suficientes, o pool bloqueava-se a si próprio.
     *
     * <p>O pool de trabalho não tem limite de dimensão porque a guarda de concorrência do
     * {@code JobRunner} já limita a uma execução por chave; os threads inactivos são recolhidos
     * ao fim de um minuto.
     */
    @Bean(destroyMethod = "fechar")
    public JobExecutores jobExecutores() {
        var disparador = new ThreadPoolTaskScheduler();
        disparador.setPoolSize(4);
        disparador.setThreadNamePrefix("rh-scheduler-");
        disparador.setWaitForTasksToCompleteOnShutdown(true);
        disparador.setAwaitTerminationSeconds(30);
        disparador.setRemoveOnCancelPolicy(true);
        disparador.initialize();

        var contador = new AtomicInteger();
        var trabalho = new ThreadPoolExecutor(
                0, Integer.MAX_VALUE,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                runnable -> {
                    var thread = Executors.defaultThreadFactory().newThread(runnable);
                    thread.setName("rh-job-" + contador.incrementAndGet());
                    thread.setDaemon(true);
                    return thread;
                });

        return new JobExecutores(disparador, trabalho);
    }
}
