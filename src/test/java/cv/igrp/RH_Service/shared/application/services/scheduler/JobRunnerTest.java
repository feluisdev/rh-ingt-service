package cv.igrp.RH_Service.shared.application.services.scheduler;

import cv.igrp.RH_Service.shared.config.SystemAuditor;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.scheduler.SchedulerExecucaoEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.scheduler.SchedulerJobEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.scheduler.SchedulerJobEntityRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JobRunnerTest {

    private static final String CHAVE = "JOB_X";
    private static final LocalDateTime AGENDADO = LocalDateTime.of(2026, 12, 31, 0, 5);

    @Mock private ExecucaoRegistoService registo;
    @Mock private SchedulerJobEntityRepository jobRepository;
    @Mock private ThreadPoolTaskScheduler disparador;
    @Mock private ApplicationEventPublisher eventPublisher;

    private ExecutorService trabalho;
    private JobRunner runner;

    /** Estado "persistido", para que abrir() e fechar() operem sobre a mesma instância. */
    private SchedulerExecucaoEntity gravada;
    private EstadoExecucao estadoFinal;

    @BeforeEach
    void setUp() {
        trabalho = Executors.newCachedThreadPool();
        runner = new JobRunner(registo, jobRepository, new JobExecutores(disparador, trabalho), eventPublisher);

        when(registo.abrir(any(), anyString())).thenAnswer(inv -> {
            PedidoExecucao pedido = inv.getArgument(0);
            gravada = new SchedulerExecucaoEntity();
            gravada.setId(UUID.randomUUID());
            gravada.setChave(pedido.getJob().getChave());
            gravada.setDisparo(pedido.getDisparo().name());
            gravada.setAgendadoPara(pedido.getAgendadoPara());
            gravada.setEstado(EstadoExecucao.A_CORRER.name());
            return Optional.of(gravada);
        });
        when(registo.fechar(any(), any(), any(), anyBoolean(), anyLong())).thenAnswer(inv -> {
            JobResult resultado = inv.getArgument(1);
            Throwable erro = inv.getArgument(2);
            boolean timeout = inv.getArgument(3);
            estadoFinal = timeout ? EstadoExecucao.TIMEOUT
                    : erro != null ? EstadoExecucao.FALHA
                    : resultado != null && resultado.getFalhas() > 0 ? EstadoExecucao.FALHA_PARCIAL
                    : EstadoExecucao.SUCESSO;
            return estadoFinal;
        });
    }

    @AfterEach
    void tearDown() {
        trabalho.shutdownNow();
    }

    // ── jobs de teste ────────────────────────────────────────────────────────

    private interface Corpo {
        JobResult correr(JobContext ctx);
    }

    private static ScheduledJob job(int maxTentativas, Duration timeout, Corpo corpo) {
        return new ScheduledJob() {
            @Override public String getChave()       { return CHAVE; }
            @Override public String getNomeLegivel() { return "Job X"; }
            @Override public String getCronPadrao()  { return "0 5 0 * * *"; }
            @Override public int getMaxTentativas()  { return maxTentativas; }
            @Override public Duration getTimeout()   { return timeout; }
            @Override public JobResult executar(JobContext ctx) { return corpo.correr(ctx); }
        };
    }

    private static ScheduledJob job(Corpo corpo) {
        return job(0, Duration.ofMinutes(1), corpo);
    }

    private static PedidoExecucao pedido(ScheduledJob job, TipoDisparo disparo, Map<String, Object> parametros) {
        return PedidoExecucao.builder()
                .job(job).disparo(disparo).solicitante("rh@nosi.cv")
                .parametros(parametros).agendadoPara(AGENDADO)
                .build();
    }

    private static SchedulerJobEntity entidade(int maxTentativas) {
        var entidade = new SchedulerJobEntity();
        entidade.setChave(CHAVE);
        entidade.setCron("0 5 0 * * *");
        entidade.setTimezone("Atlantic/Cape_Verde");
        entidade.setActivo(true);
        entidade.setMaxTentativas(maxTentativas);
        entidade.setTimeoutSegundos(60);
        return entidade;
    }

    // ── desfechos ────────────────────────────────────────────────────────────

    @Test
    void semFalhas_gravaSucesso() {
        runner.run(pedido(job(ctx -> JobResult.builder().processados(10).criados(8).build()),
                TipoDisparo.MANUAL, Map.of()));

        assertEquals(EstadoExecucao.SUCESSO, estadoFinal);
        verify(registo).actualizarJob(CHAVE, EstadoExecucao.SUCESSO, false);
    }

    @Test
    void comFalhasPorItem_gravaFalhaParcial() {
        runner.run(pedido(job(ctx -> JobResult.builder().processados(10).falhas(3).build()),
                TipoDisparo.AGENDADO, Map.of()));

        assertEquals(EstadoExecucao.FALHA_PARCIAL, estadoFinal);
        verify(registo).actualizarJob(CHAVE, EstadoExecucao.FALHA_PARCIAL, true);
    }

    @Test
    @DisplayName("uma excepção do job é gravada como FALHA e nunca propagada")
    void comExcecao_gravaFalhaSemPropagar() {
        runner.run(pedido(job(ctx -> { throw new IllegalStateException("rebentou"); }), TipoDisparo.AGENDADO, Map.of()));

        assertEquals(EstadoExecucao.FALHA, estadoFinal);
        var erro = ArgumentCaptor.forClass(Throwable.class);
        verify(registo).fechar(any(), any(), erro.capture(), eq(false), anyLong());
        assertTrue(erro.getValue().getMessage().contains("rebentou"));
    }

    @Test
    @DisplayName("job que excede o tempo limite é fechado como TIMEOUT")
    void jobLento_gravaTimeout() {
        when(jobRepository.findByChave(CHAVE)).thenReturn(Optional.empty());
        var lento = job(0, Duration.ofMillis(50), ctx -> {
            try {
                Thread.sleep(5_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return JobResult.vazio();
        });

        runner.run(pedido(lento, TipoDisparo.MANUAL, Map.of()));

        assertEquals(EstadoExecucao.TIMEOUT, estadoFinal);
    }

    @Test
    void jobSemResultado_contaComoSucesso() {
        runner.run(pedido(job(ctx -> null), TipoDisparo.MANUAL, Map.of()));
        assertEquals(EstadoExecucao.SUCESSO, estadoFinal);
    }

    // ── contexto e auditoria ─────────────────────────────────────────────────

    @Test
    @DisplayName("o job recebe os parâmetros e o dia deriva do instante agendado, não de hoje")
    void entregaContextoAoJob() {
        var visto = new JobContext[1];
        runner.run(pedido(job(ctx -> { visto[0] = ctx; return JobResult.vazio(); }), TipoDisparo.AGENDADO, Map.of("x", "1")));

        assertNotNull(visto[0]);
        assertEquals("1", visto[0].getString("x"));
        assertEquals(LocalDate.of(2026, 12, 31), visto[0].dataReferencia());
        assertEquals(TipoDisparo.AGENDADO, visto[0].getDisparo());
    }

    @Test
    @DisplayName("o job escreve como scheduler:<chave>, e o âmbito não fica colado ao thread")
    void corre_sobOAutorDeSistema() {
        var autor = new String[1];
        runner.run(pedido(job(ctx -> { autor[0] = SystemAuditor.current().orElse(null); return JobResult.vazio(); }),
                TipoDisparo.MANUAL, Map.of()));

        assertEquals("scheduler:" + CHAVE, autor[0]);
        assertTrue(SystemAuditor.current().isEmpty());
    }

    // ── guarda de concorrência ───────────────────────────────────────────────

    @Test
    @DisplayName("com uma execução em curso, o pedido é recusado e o job não corre")
    void execucaoEmCurso_recusaSemCorrer() {
        var contador = new AtomicInteger();
        doThrow(IgrpResponseStatusException.conflict("já está a correr")).when(registo).abrir(any(), anyString());

        assertThrows(IgrpResponseStatusException.class, () -> runner.run(
                pedido(job(ctx -> { contador.incrementAndGet(); return JobResult.vazio(); }), TipoDisparo.MANUAL, Map.of())));
        assertEquals(0, contador.get());
    }

    @Test
    @DisplayName("um disparo do cron já registado por outra réplica não corre outra vez")
    void disparoDuplicado_naoCorre() {
        var contador = new AtomicInteger();
        doReturn(Optional.empty()).when(registo).abrir(any(), anyString());

        runner.runAgendado(job(ctx -> { contador.incrementAndGet(); return JobResult.vazio(); }), AGENDADO);

        assertEquals(0, contador.get());
        verify(registo, never()).fechar(any(), any(), any(), anyBoolean(), anyLong());
    }

    // ── retry ────────────────────────────────────────────────────────────────

    @Test
    void falha_comTentativasDisponiveis_agendaRetry() {
        when(jobRepository.findByChave(CHAVE)).thenReturn(Optional.of(entidade(3)));

        runner.run(pedido(job(3, Duration.ofMinutes(1), ctx -> { throw new IllegalStateException("BD em baixo"); }),
                TipoDisparo.AGENDADO, Map.of()));

        verify(disparador).schedule(any(Runnable.class), any(Instant.class));
    }

    @Test
    @DisplayName("FALHA_PARCIAL nunca agenda retry — os mesmos itens voltariam a falhar")
    void falhaParcial_naoAgendaRetry() {
        when(jobRepository.findByChave(CHAVE)).thenReturn(Optional.of(entidade(3)));

        runner.run(pedido(job(3, Duration.ofMinutes(1), ctx -> JobResult.builder().falhas(1).build()),
                TipoDisparo.AGENDADO, Map.of()));

        verify(disparador, never()).schedule(any(Runnable.class), any(Instant.class));
    }

    @Test
    void falha_naUltimaTentativa_naoAgendaRetry() {
        when(jobRepository.findByChave(CHAVE)).thenReturn(Optional.of(entidade(3)));

        runner.run(PedidoExecucao.builder()
                .job(job(3, Duration.ofMinutes(1), ctx -> { throw new IllegalStateException("rebentou"); }))
                .disparo(TipoDisparo.AGENDADO).parametros(Map.of()).agendadoPara(AGENDADO).tentativa(3).build());

        verify(disparador, never()).schedule(any(Runnable.class), any(Instant.class));
    }

    @Test
    @DisplayName("a configuração em BD sobrepõe-se ao default do código")
    void maxTentativas_daBd_ganhaAoDoCodigo() {
        when(jobRepository.findByChave(CHAVE)).thenReturn(Optional.of(entidade(2)));

        runner.run(pedido(job(0, Duration.ofMinutes(1), ctx -> { throw new IllegalStateException("rebentou"); }),
                TipoDisparo.AGENDADO, Map.of()));

        verify(disparador).schedule(any(Runnable.class), any(Instant.class));
    }

    @Test
    void backoff_cresce5_15_45() {
        assertEquals(5, JobRunner.backoff(1).toMinutes());
        assertEquals(15, JobRunner.backoff(2).toMinutes());
        assertEquals(45, JobRunner.backoff(3).toMinutes());
    }

    @Test
    void apenasFalhaETimeoutPermitemRetry() {
        assertTrue(EstadoExecucao.FALHA.permiteRetryAutomatico());
        assertTrue(EstadoExecucao.TIMEOUT.permiteRetryAutomatico());
        List.of(EstadoExecucao.SUCESSO, EstadoExecucao.FALHA_PARCIAL, EstadoExecucao.OMITIDA, EstadoExecucao.A_CORRER)
                .forEach(estado -> assertFalse(estado.permiteRetryAutomatico(), estado.name()));
    }

    // ── evento ───────────────────────────────────────────────────────────────

    @Test
    void publicaEventoDeDesfecho() {
        runner.run(pedido(job(ctx -> JobResult.builder().referencia("2026").build()), TipoDisparo.MANUAL, Map.of()));

        var evento = ArgumentCaptor.forClass(JobExecucaoTerminadaEvent.class);
        verify(eventPublisher).publishEvent(evento.capture());
        assertEquals(CHAVE, evento.getValue().chave());
        assertEquals(EstadoExecucao.SUCESSO, evento.getValue().estado());
        assertEquals("2026", evento.getValue().referencia());
    }

    @Test
    @DisplayName("um listener que rebenta não afecta o desfecho já gravado")
    void listenerComErro_naoAfectaODesfecho() {
        doThrow(new RuntimeException("listener mau")).when(eventPublisher).publishEvent(any(JobExecucaoTerminadaEvent.class));

        runner.run(pedido(job(ctx -> JobResult.vazio()), TipoDisparo.MANUAL, Map.of()));

        assertEquals(EstadoExecucao.SUCESSO, estadoFinal);
    }
}
