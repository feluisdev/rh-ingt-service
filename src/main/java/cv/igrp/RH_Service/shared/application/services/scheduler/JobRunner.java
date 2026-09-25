package cv.igrp.RH_Service.shared.application.services.scheduler;

import cv.igrp.RH_Service.shared.config.SystemAuditor;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.scheduler.SchedulerJobEntityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Envolve a execução de um {@link ScheduledJob} e é onde vivem todas as preocupações que não são
 * lógica de negócio. O scheduler do Spring é fire-and-forget; isto é o que lhe dá memória.
 *
 * <p>Pipeline de cada execução, por ordem:
 * <ol>
 *   <li>guarda de concorrência entre réplicas — nunca duas execuções da mesma chave em simultâneo,
 *       e um disparo do cron conta uma só vez, mesmo chegando a todas as réplicas;</li>
 *   <li>abertura do registo, já com os parâmetros;</li>
 *   <li>execução com tempo limite, sob o autor de sistema {@code scheduler:<chave>};</li>
 *   <li>fecho do registo em transacção própria;</li>
 *   <li>actualização do estado do job;</li>
 *   <li>evento de desfecho (costura para futuras notificações);</li>
 *   <li>agendamento do retry, se aplicável.</li>
 * </ol>
 *
 * <p>Nunca propaga a excepção de um job: o thread do scheduler não pode morrer por causa de um.
 */
@Component
public class JobRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobRunner.class);

    /** Backoff do retry automático: 5, 15 e 45 minutos. */
    private static final long BACKOFF_BASE_MINUTOS = 5;
    private static final long BACKOFF_FACTOR = 3;

    private final ExecucaoRegistoService registo;
    private final SchedulerJobEntityRepository jobRepository;
    private final JobExecutores executores;
    private final ApplicationEventPublisher eventPublisher;

    public JobRunner(ExecucaoRegistoService registo, SchedulerJobEntityRepository jobRepository,
                     JobExecutores executores, ApplicationEventPublisher eventPublisher) {
        this.registo = registo;
        this.jobRepository = jobRepository;
        this.executores = executores;
        this.eventPublisher = eventPublisher;
    }

    /** Registo aberto e pronto a correr. */
    public record ExecucaoAberta(UUID id, JobContext contexto) {}

    /** Autor com que o job escreve na auditoria. */
    static String auditor(ScheduledJob job) {
        return "scheduler:" + job.getChave();
    }

    /**
     * Abre o registo da execução.
     *
     * @return vazio quando outra réplica já registou este mesmo disparo do cron
     * @throws IgrpResponseStatusException 409 se já houver uma execução em curso para esta chave
     */
    public Optional<ExecucaoAberta> iniciar(PedidoExecucao pedido) {
        return registo.abrir(pedido, cronAtual(pedido.getJob())).map(entidade -> new ExecucaoAberta(
                entidade.getId(),
                JobContext.of(
                        pedido.getParametros(),
                        entidade.getAgendadoPara(),
                        pedido.getDisparo(),
                        pedido.getSolicitante(),
                        entidade.getId(),
                        pedido.getTentativa())));
    }

    /** Corre o job e fecha o registo. Não propaga: o desfecho fica sempre gravado. */
    public void executar(ExecucaoAberta aberta, ScheduledJob job) {
        long t0 = System.currentTimeMillis();
        JobResult resultado = null;
        Throwable erro = null;
        boolean timeout = false;
        long limite = segundosDeTimeout(job);

        Future<JobResult> futuro = executores.trabalho().submit(() -> correrComoSistema(job, aberta.contexto()));
        try {
            resultado = futuro.get(limite, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            timeout = true;
            // Marca o desfecho e liberta a guarda de concorrência. A interrupção é um pedido: uma
            // chamada JDBC a meio pode ignorá-la e o thread ficar a correr até terminar por si.
            futuro.cancel(true);
            LOGGER.error("[{}] execução excedeu o tempo limite de {}s", job.getChave(), limite);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            erro = e;
            LOGGER.error("[{}] execução interrompida", job.getChave(), e);
        } catch (Exception e) {
            erro = e.getCause() != null ? e.getCause() : e;
            LOGGER.error("[{}] execução falhou: {}", job.getChave(), erro.getMessage(), erro);
        }

        var estado = registo.fechar(aberta.id(), resultado, erro, timeout, System.currentTimeMillis() - t0);
        var agendado = TipoDisparo.AGENDADO == aberta.contexto().getDisparo();
        registo.actualizarJob(job.getChave(), estado, agendado);

        publicarEvento(job, aberta, estado, resultado);
        agendarRetrySeNecessario(job, aberta, estado);
    }

    /** Conveniência: abre, corre e fecha numa só chamada. É o que o cron usa. */
    public void run(PedidoExecucao pedido) {
        iniciar(pedido).ifPresent(aberta -> executar(aberta, pedido.getJob()));
    }

    /** Atalho para o disparo agendado. */
    public void runAgendado(ScheduledJob job, LocalDateTime agendadoPara) {
        run(PedidoExecucao.builder()
                .job(job)
                .disparo(TipoDisparo.AGENDADO)
                .agendadoPara(agendadoPara)
                .build());
    }

    // ── internals ────────────────────────────────────────────────────────────

    /**
     * O job corre num thread do pool de trabalho, e o {@link SystemAuditor} é por thread — por isso
     * o âmbito abre-se aqui dentro, e não à volta do {@code submit}.
     */
    private static JobResult correrComoSistema(ScheduledJob job, JobContext contexto) {
        var resultado = new JobResult[1];
        SystemAuditor.runAs(auditor(job), () -> resultado[0] = job.executar(contexto));
        return resultado[0];
    }

    private void publicarEvento(ScheduledJob job, ExecucaoAberta aberta,
                                EstadoExecucao estado, JobResult resultado) {
        try {
            eventPublisher.publishEvent(new JobExecucaoTerminadaEvent(
                    job.getChave(),
                    aberta.id(),
                    estado,
                    resultado != null ? resultado.getReferencia() : null,
                    resultado != null ? resultado.getMensagem() : null));
        } catch (Exception e) {
            // Um listener mal comportado não pode afectar o desfecho do job, que já está gravado.
            LOGGER.warn("[{}] falha ao publicar evento de desfecho: {}", job.getChave(), e.getMessage());
        }
    }

    /**
     * Retry automático apenas em {@code FALHA} e {@code TIMEOUT} — falhas tipicamente transitórias.
     * {@code FALHA_PARCIAL} nunca: os mesmos itens voltariam a falhar. Esses resolvem-se corrigindo
     * o dado e re-executando à mão, e a idempotência do job salta o que já ficou feito.
     */
    private void agendarRetrySeNecessario(ScheduledJob job, ExecucaoAberta aberta, EstadoExecucao estado) {
        if (!estado.permiteRetryAutomatico()) return;

        int tentativa = aberta.contexto().getTentativa();
        int maximo = maxTentativas(job);
        if (tentativa >= maximo) {
            if (maximo > 0)
                LOGGER.error("[{}] esgotadas as {} tentativas — fica em {} para intervenção manual",
                        job.getChave(), maximo, estado);
            return;
        }

        var espera = backoff(tentativa);
        var proximo = PedidoExecucao.builder()
                .job(job)
                .disparo(aberta.contexto().getDisparo())
                .solicitante(aberta.contexto().getSolicitante())
                .parametros(aberta.contexto().getParametros())
                .agendadoPara(aberta.contexto().getAgendadoPara())
                .tentativa(tentativa + 1)
                .execucaoPaiId(aberta.id())
                .build();

        LOGGER.warn("[{}] tentativa {}/{} falhou ({}) — nova tentativa dentro de {} min",
                job.getChave(), tentativa, maximo, estado, espera.toMinutes());
        executores.disparador().schedule(() -> runSilencioso(proximo), Instant.now().plus(espera));
    }

    private void runSilencioso(PedidoExecucao pedido) {
        try {
            run(pedido);
        } catch (Exception e) {
            // Tipicamente a guarda de concorrência: entretanto alguém disparou o job à mão.
            LOGGER.warn("[{}] retry não chegou a arrancar: {}", pedido.getJob().getChave(), e.getMessage());
        }
    }

    static Duration backoff(int tentativa) {
        long minutos = BACKOFF_BASE_MINUTOS;
        for (int i = 1; i < tentativa; i++) minutos *= BACKOFF_FACTOR;
        return Duration.ofMinutes(minutos);
    }

    /** Configuração em BD manda sobre o default do código. */
    private int maxTentativas(ScheduledJob job) {
        return jobRepository.findByChave(job.getChave())
                .map(entidade -> entidade.getMaxTentativas() != null
                        ? entidade.getMaxTentativas() : job.getMaxTentativas())
                .orElseGet(job::getMaxTentativas);
    }

    private long segundosDeTimeout(ScheduledJob job) {
        return jobRepository.findByChave(job.getChave())
                .map(entidade -> entidade.getTimeoutSegundos() != null
                        ? entidade.getTimeoutSegundos().longValue() : job.getTimeout().toSeconds())
                .orElseGet(() -> job.getTimeout().toSeconds());
    }

    private String cronAtual(ScheduledJob job) {
        return jobRepository.findByChave(job.getChave())
                .map(entidade -> entidade.getCron())
                .orElseGet(job::getCronPadrao);
    }
}
