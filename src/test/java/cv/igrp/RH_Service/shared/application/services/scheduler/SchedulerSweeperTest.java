package cv.igrp.RH_Service.shared.application.services.scheduler;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.scheduler.SchedulerJobEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.scheduler.SchedulerJobEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * O sweeper é a única peça que torna visível uma execução que <em>não existe</em>. Estes testes
 * cobrem sobretudo isso: que uma omissão é registada com o instante e o dia correctos, e que um job
 * em dia não gera ruído.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SchedulerSweeperTest {

    private static final String CHAVE = "JOB_DIARIO";
    private static final String CRON_DIARIO = "0 5 0 * * *";

    @Mock private SchedulerJobEntityRepository jobRepository;
    @Mock private ExecucaoRegistoService registo;
    @Mock private SchedulerService schedulerService;

    private SchedulerSweeper sweeper;

    private final ScheduledJob jobComData = new ScheduledJob() {
        @Override public String getChave()       { return CHAVE; }
        @Override public String getNomeLegivel() { return "Job diário"; }
        @Override public String getCronPadrao()  { return CRON_DIARIO; }
        @Override public List<JobParametro> getParametros() { return List.of(JobParametro.dataReferencia()); }
        @Override public JobResult executar(JobContext ctx) { return JobResult.vazio(); }
    };

    private final ScheduledJob jobSemParametros = new ScheduledJob() {
        @Override public String getChave()       { return CHAVE; }
        @Override public String getNomeLegivel() { return "Job sem parâmetros"; }
        @Override public String getCronPadrao()  { return CRON_DIARIO; }
        @Override public JobResult executar(JobContext ctx) { return JobResult.vazio(); }
    };

    private SchedulerJobEntity guardada;

    @BeforeEach
    void setUp() {
        sweeper = new SchedulerSweeper(jobRepository, registo, schedulerService);
        sweeper.configurarTolerancia(10);
        when(schedulerService.getJobsByChave()).thenReturn(Map.of(CHAVE, jobComData));
        when(registo.registarOmissao(anyString(), any(), anyString(), any(), any())).thenReturn(true);
    }

    private SchedulerJobEntity entidade(LocalDateTime proxima) {
        guardada = new SchedulerJobEntity();
        guardada.setChave(CHAVE);
        guardada.setNome("Job diário");
        guardada.setCron(CRON_DIARIO);
        guardada.setTimezone("Atlantic/Cape_Verde");
        guardada.setActivo(true);
        guardada.setProximaExecucao(proxima);
        when(jobRepository.findByActivoTrue()).thenReturn(List.of(guardada));
        when(jobRepository.findByChave(CHAVE)).thenReturn(Optional.of(guardada));
        return guardada;
    }

    @Test
    void previsaoNoFuturo_naoRegistaNada() {
        entidade(RelogioScheduler.agora().plusHours(5));

        assertEquals(0, sweeper.detectarOmissoes());
        verify(registo, never()).registarOmissao(anyString(), any(), anyString(), any(), any());
    }

    @Test
    void previsaoRecente_dentroDaTolerancia_naoRegista() {
        entidade(RelogioScheduler.agora().minusMinutes(3));

        assertEquals(0, sweeper.detectarOmissoes());
    }

    @Test
    @DisplayName("regista OMITIDA com o instante previsto e o dia desse instante como parâmetro")
    void previsaoPassada_registaComInstanteEDia() {
        var previsto = LocalDateTime.of(2026, 9, 20, 0, 5);
        entidade(previsto);

        sweeper.detectarOmissoes();

        var instante = ArgumentCaptor.forClass(LocalDateTime.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> params = ArgumentCaptor.forClass((Class<Map<String, Object>>) (Class<?>) Map.class);
        verify(registo, atLeastOnce()).registarOmissao(eq(CHAVE), any(), eq(CRON_DIARIO), instante.capture(), params.capture());
        assertEquals(previsto, instante.getAllValues().get(0));
        assertEquals("2026-09-20", params.getAllValues().get(0).get(JobContext.PARAM_DATA));
    }

    @Test
    void jobSemParametroData_naoRecebeParametrosInventados() {
        when(schedulerService.getJobsByChave()).thenReturn(Map.of(CHAVE, jobSemParametros));
        entidade(RelogioScheduler.agora().minusDays(2));

        sweeper.detectarOmissoes();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> params = ArgumentCaptor.forClass((Class<Map<String, Object>>) (Class<?>) Map.class);
        verify(registo, atLeastOnce()).registarOmissao(anyString(), any(), anyString(), any(), params.capture());
        assertTrue(params.getAllValues().get(0).isEmpty());
    }

    @Test
    @DisplayName("três dias parados dão três omissões, e a previsão avança para o futuro")
    void tresDiasParados_tresOmissoes() {
        var hoje = RelogioScheduler.agora();
        var previsto = hoje.toLocalDate().minusDays(3).atTime(0, 5);
        entidade(previsto);

        int registadas = sweeper.detectarOmissoes();

        int esperadas = hoje.isAfter(hoje.toLocalDate().atTime(0, 15)) ? 4 : 3;
        assertEquals(esperadas, registadas);
        assertTrue(guardada.getProximaExecucao().isAfter(hoje.minusMinutes(10)));
    }

    @Test
    void paragemMuitoLonga_respeitaOTecto() {
        entidade(RelogioScheduler.agora().minusYears(1));

        assertEquals(SchedulerSweeper.MAX_OMISSOES_POR_VARRIMENTO, sweeper.detectarOmissoes());
    }

    @Test
    void semPrevisao_ignorado() {
        entidade(null);

        assertEquals(0, sweeper.detectarOmissoes());
    }

    @Test
    @DisplayName("não pisa uma previsão que o cron ou outra réplica actualizou durante o varrimento")
    void avancarPrevisao_naoPisaEscritaMaisRecente() {
        var vista = LocalDateTime.of(2026, 9, 20, 0, 5);
        var entretanto = LocalDateTime.of(2026, 9, 26, 0, 5);
        entidade(entretanto);

        sweeper.avancarPrevisao(CHAVE, vista, LocalDateTime.of(2026, 9, 25, 0, 5));

        assertEquals(entretanto, guardada.getProximaExecucao());
        verify(jobRepository, never()).save(any());
    }

    @Test
    void fecharZombies_passaAMargemDeTolerancia() {
        sweeper.fecharZombies();
        verify(registo).fecharOrfasExpiradas(Duration.ofMinutes(10));
    }

    @Test
    void aoArrancar_fechaAsOrfasDestaInstancia() {
        sweeper.aoArrancar();
        verify(registo).fecharOrfasDestaInstancia();
    }

    @Test
    @DisplayName("uma etapa que rebenta não impede as seguintes")
    void varrer_etapaComErro_naoTravaAsRestantes() {
        when(registo.fecharOrfasExpiradas(any())).thenThrow(new IllegalStateException("BD em baixo"));
        when(jobRepository.findByActivoTrue()).thenReturn(List.of());

        sweeper.varrer();

        verify(jobRepository).findByActivoTrue();
        verify(schedulerService).recarregar();
    }
}
