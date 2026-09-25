package cv.igrp.RH_Service.shared.application.services.scheduler;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.ExecutorService;

/**
 * Os dois pools do framework, embrulhados para <b>não</b> serem beans {@code TaskScheduler}.
 *
 * <p>Se o {@code ThreadPoolTaskScheduler} fosse um bean, o Spring passava a usá-lo também para os
 * {@code @Scheduled} que ainda existem (os do {@code sigdi}), que hoje correm em série no scheduler
 * por omissão, de um só thread — e a ordem entre eles (00:30 → 00:45 → 01:00) deixaria de ser
 * garantida. Assim ficam como estavam.
 *
 * @param disparador dispara os triggers cron e os retries; não é aqui que o trabalho corre
 * @param trabalho   onde a lógica dos jobs corre de facto — ver {@code SchedulingConfig}
 */
public record JobExecutores(ThreadPoolTaskScheduler disparador, ExecutorService trabalho) {

    public void fechar() {
        disparador.shutdown();
        trabalho.shutdown();
    }
}
