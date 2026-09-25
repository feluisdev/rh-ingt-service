package cv.igrp.RH_Service.shared.application.services.scheduler;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.scheduler.SchedulerExecucaoEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.scheduler.SchedulerJobEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.scheduler.SchedulerExecucaoEntityRepository;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.scheduler.SchedulerJobEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ExecucaoRegistoServiceTest {

    private static final String CHAVE = "JOB_X";
    private static final LocalDateTime AGENDADO = LocalDateTime.of(2026, 9, 25, 0, 5);

    @Mock private SchedulerExecucaoEntityRepository execucaoRepository;
    @Mock private SchedulerJobEntityRepository jobRepository;

    private ExecucaoRegistoService registo;

    private final ScheduledJob job = new ScheduledJob() {
        @Override public String getChave()       { return CHAVE; }
        @Override public String getNomeLegivel() { return "Vencimento do direito a férias"; }
        @Override public String getCronPadrao()  { return "0 5 0 * * *"; }
        @Override public JobResult executar(JobContext ctx) { return JobResult.vazio(); }
    };

    @BeforeEach
    void setUp() {
        registo = new ExecucaoRegistoService(execucaoRepository, jobRepository);
        when(execucaoRepository.save(any(SchedulerExecucaoEntity.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private PedidoExecucao pedido(TipoDisparo disparo, int tentativa) {
        return PedidoExecucao.builder().job(job).disparo(disparo).agendadoPara(AGENDADO)
                .parametros(Map.of("data", "2026-09-25")).tentativa(tentativa).build();
    }

    // ── abrir ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("abre o registo já com os parâmetros e o instante agendado")
    void abrir_gravaParametrosNoArranque() {
        var aberta = registo.abrir(pedido(TipoDisparo.MANUAL, 1), "0 5 0 * * *").orElseThrow();

        assertEquals(EstadoExecucao.A_CORRER.name(), aberta.getEstado());
        assertEquals(AGENDADO, aberta.getAgendadoPara());
        assertEquals("2026-09-25", aberta.getParametros().get("data"));
    }

    @Test
    @DisplayName("bloqueia a linha do job ANTES de verificar — é o que torna a verificação atómica entre réplicas")
    void abrir_bloqueiaAntesDeVerificar() {
        registo.abrir(pedido(TipoDisparo.AGENDADO, 1), "0 5 0 * * *");

        InOrder ordem = inOrder(jobRepository, execucaoRepository);
        ordem.verify(jobRepository).findByChaveParaActualizar(CHAVE);
        ordem.verify(execucaoRepository).existsByChaveAndAgendadoPara(CHAVE, AGENDADO);
        ordem.verify(execucaoRepository).existsByChaveAndEstado(CHAVE, EstadoExecucao.A_CORRER.name());
        ordem.verify(execucaoRepository).save(any());
    }

    @Test
    @DisplayName("o mesmo disparo do cron a chegar de outra réplica é ignorado, sem erro")
    void abrir_disparoJaRegistado_devolveVazio() {
        when(execucaoRepository.existsByChaveAndAgendadoPara(CHAVE, AGENDADO)).thenReturn(true);

        assertTrue(registo.abrir(pedido(TipoDisparo.AGENDADO, 1), "0 5 0 * * *").isEmpty());
        verify(execucaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("um retry ou uma re-execução não são deduplicados pelo instante — partilham-no com a original")
    void abrir_retryComMesmoInstante_abre() {
        when(execucaoRepository.existsByChaveAndAgendadoPara(CHAVE, AGENDADO)).thenReturn(true);

        assertTrue(registo.abrir(pedido(TipoDisparo.AGENDADO, 2), "0 5 0 * * *").isPresent());
        assertTrue(registo.abrir(pedido(TipoDisparo.MANUAL, 1), "0 5 0 * * *").isPresent());
    }

    @Test
    @DisplayName("com uma execução em curso, recusa com 409 e uma mensagem para o utilizador")
    void abrir_emCurso_recusa() {
        when(execucaoRepository.existsByChaveAndEstado(CHAVE, EstadoExecucao.A_CORRER.name())).thenReturn(true);

        var erro = assertThrows(IgrpResponseStatusException.class,
                () -> registo.abrir(pedido(TipoDisparo.MANUAL, 1), "0 5 0 * * *"));
        assertEquals(409, erro.getStatusCode().value());
        assertTrue(erro.getBody().getTitle().contains("Vencimento do direito a férias"));
        assertFalse(erro.getBody().getTitle().contains(CHAVE));
    }

    // ── fechar ───────────────────────────────────────────────────────────────

    private SchedulerExecucaoEntity emCurso() {
        var execucao = new SchedulerExecucaoEntity();
        execucao.setId(UUID.randomUUID());
        execucao.setChave(CHAVE);
        execucao.setEstado(EstadoExecucao.A_CORRER.name());
        execucao.setInicio(AGENDADO);
        when(execucaoRepository.findById(execucao.getId())).thenReturn(Optional.of(execucao));
        return execucao;
    }

    @Test
    void fechar_decideOEstadoPeloDesfecho() {
        var e = emCurso();
        assertEquals(EstadoExecucao.SUCESSO, registo.fechar(e.getId(), JobResult.builder().processados(3).build(), null, false, 10));
        assertEquals(3, e.getProcessados());
        assertEquals(EstadoExecucao.FALHA_PARCIAL, registo.fechar(e.getId(), JobResult.builder().falhas(1).build(), null, false, 10));
        assertEquals(EstadoExecucao.FALHA, registo.fechar(e.getId(), null, new IllegalStateException("x"), false, 10));
        assertTrue(e.getErro().contains("IllegalStateException"));
        assertEquals(EstadoExecucao.TIMEOUT, registo.fechar(e.getId(), null, null, true, 10));
        assertEquals(EstadoExecucao.SUCESSO, registo.fechar(e.getId(), null, null, false, 10));
    }

    @Test
    @DisplayName("listas enormes nos detalhes são cortadas, com a indicação de quantas havia")
    void limitarDetalhes_cortaListasLongas() {
        var muitos = IntStream.range(0, 500).mapToObj(String::valueOf).toList();
        var limitado = ExecucaoRegistoService.limitarDetalhes(Map.of("itensFalhados", muitos));

        assertEquals(ExecucaoRegistoService.MAX_ITENS_DETALHE, ((List<?>) limitado.get("itensFalhados")).size());
        assertEquals("mostrados 200 de 500", limitado.get("itensFalhados_truncado"));
    }

    // ── omissões e órfãs ─────────────────────────────────────────────────────

    @Test
    void registarOmissao_soUmaVezPorInstante() {
        assertTrue(registo.registarOmissao(CHAVE, "Job X", "0 5 0 * * *", AGENDADO, Map.of()));
        when(execucaoRepository.existsByChaveAndAgendadoPara(CHAVE, AGENDADO)).thenReturn(true);
        assertFalse(registo.registarOmissao(CHAVE, "Job X", "0 5 0 * * *", AGENDADO, Map.of()));
        verify(jobRepository, org.mockito.Mockito.times(2)).findByChaveParaActualizar(CHAVE);
    }

    @Test
    @DisplayName("uma execução só é zombie depois do tempo limite do SEU job mais a margem")
    void fecharOrfasExpiradas_usaOTimeoutDeCadaJob() {
        var agora = RelogioScheduler.agora();
        var recente = execucao("RAPIDO", agora.minusMinutes(15));
        var antiga = execucao("RAPIDO", agora.minusMinutes(25));
        var lenta = execucao("LENTO", agora.minusMinutes(25));
        when(execucaoRepository.findByEstado(EstadoExecucao.A_CORRER.name())).thenReturn(List.of(recente, antiga, lenta));
        when(jobRepository.findByChave("RAPIDO")).thenReturn(Optional.of(jobComTimeout("RAPIDO", 600)));
        when(jobRepository.findByChave("LENTO")).thenReturn(Optional.of(jobComTimeout("LENTO", 3600)));

        assertEquals(1, registo.fecharOrfasExpiradas(Duration.ofMinutes(10)));

        assertEquals(EstadoExecucao.A_CORRER.name(), recente.getEstado());
        assertEquals(EstadoExecucao.TIMEOUT.name(), antiga.getEstado());
        assertEquals(EstadoExecucao.A_CORRER.name(), lenta.getEstado());
    }

    @Test
    @DisplayName("no arranque só fecha o que ESTA réplica deixou a correr, e só de antes do arranque")
    void fecharOrfasDestaInstancia_soAsProprias() {
        var anterior = execucao(CHAVE, RelogioScheduler.agora().minusDays(1));
        var jaDesteArranque = execucao(CHAVE, RelogioScheduler.agora().plusMinutes(1));
        when(execucaoRepository.findByEstadoAndInstancia(EstadoExecucao.A_CORRER.name(), ExecucaoRegistoService.instancia()))
                .thenReturn(List.of(anterior, jaDesteArranque));

        assertEquals(1, registo.fecharOrfasDestaInstancia());
        assertEquals(EstadoExecucao.TIMEOUT.name(), anterior.getEstado());
        assertEquals(EstadoExecucao.A_CORRER.name(), jaDesteArranque.getEstado());
    }

    private static SchedulerExecucaoEntity execucao(String chave, LocalDateTime inicio) {
        var execucao = new SchedulerExecucaoEntity();
        execucao.setId(UUID.randomUUID());
        execucao.setChave(chave);
        execucao.setEstado(EstadoExecucao.A_CORRER.name());
        execucao.setInicio(inicio);
        return execucao;
    }

    private static SchedulerJobEntity jobComTimeout(String chave, int segundos) {
        var entidade = new SchedulerJobEntity();
        entidade.setChave(chave);
        entidade.setTimeoutSegundos(segundos);
        return entidade;
    }

    @Test
    void actualizarJob_soAvancaAPrevisaoNoAgendado() {
        var entidade = jobComTimeout(CHAVE, 60);
        entidade.setCron("0 5 0 * * *");
        entidade.setTimezone("Atlantic/Cape_Verde");
        when(jobRepository.findByChave(anyString())).thenReturn(Optional.of(entidade));

        registo.actualizarJob(CHAVE, EstadoExecucao.SUCESSO, false);
        assertEquals(null, entidade.getProximaExecucao());

        registo.actualizarJob(CHAVE, EstadoExecucao.SUCESSO, true);
        assertTrue(entidade.getProximaExecucao().isAfter(RelogioScheduler.agora()));
    }
}
