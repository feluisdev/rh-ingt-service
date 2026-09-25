package cv.igrp.RH_Service.shared.application.services.scheduler;

import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.scheduler.SchedulerJobEntityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Manutenção periódica do scheduler. Três tarefas de casa que nenhum job devia ter de conhecer:
 *
 * <ol>
 *   <li><b>Zombies</b> — execuções presas em {@code A_CORRER} porque o processo morreu antes de
 *       gravar o desfecho. Sem isto ficam eternamente "a correr" e bloqueiam a guarda de
 *       concorrência.</li>
 *   <li><b>Omissões</b> — execuções agendadas que nunca aconteceram. É o único mecanismo que
 *       torna visível uma execução que <em>não existe</em>: um ecrã que lista execuções nunca
 *       mostra a que faltou.</li>
 *   <li><b>Reload</b> — reagenda o que foi alterado em base de dados por outra réplica.</li>
 * </ol>
 *
 * <p>Corre em todas as réplicas, e pode: cada escrita corre na sua própria transacção (as do
 * {@code ExecucaoRegistoService} são {@code REQUIRES_NEW}) e as omissões são registadas sob o lock
 * da linha do job, por isso duas réplicas a varrer ao mesmo tempo não duplicam nada.
 *
 * <p>Usa {@code @Scheduled} e não o framework: é a manutenção do próprio framework, e registá-la
 * como job encheria o histórico com uma linha a cada cinco minutos.
 */
@Component
public class SchedulerSweeper {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerSweeper.class);

    /**
     * Tecto de omissões registadas por job em cada varrimento. Uma paragem de um mês não deve
     * despejar trinta linhas de um job diário: as primeiras já dizem ao admin o que ele precisa de saber.
     */
    static final int MAX_OMISSOES_POR_VARRIMENTO = 24;

    private final SchedulerJobEntityRepository jobRepository;
    private final ExecucaoRegistoService registo;
    private final SchedulerService schedulerService;

    /** Margem antes de considerar que um disparo previsto não aconteceu, ou que uma execução morreu. */
    @Value("${scheduler.sweeper.tolerancia-minutos:10}")
    private long toleranciaMinutos = 10;

    public SchedulerSweeper(SchedulerJobEntityRepository jobRepository, ExecucaoRegistoService registo,
                            SchedulerService schedulerService) {
        this.jobRepository = jobRepository;
        this.registo = registo;
        this.schedulerService = schedulerService;
    }

    /**
     * Varrimento de arranque: o que <b>esta</b> réplica deixou {@code A_CORRER} ao morrer é fechado de
     * imediato, sem esperar pelo tempo limite. As das outras réplicas ficam para o varrimento
     * periódico — podem estar legitimamente a correr.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void aoArrancar() {
        try {
            int fechadas = registo.fecharOrfasDestaInstancia();
            if (fechadas > 0)
                LOGGER.warn("Arranque: {} execução(ões) desta réplica presas em A_CORRER fechadas como TIMEOUT", fechadas);
        } catch (Exception e) {
            LOGGER.error("Arranque: falha ao fechar execuções órfãs: {}", e.getMessage(), e);
        }
    }

    @Scheduled(
            initialDelayString = "${scheduler.sweeper.arranque-ms:60000}",
            fixedDelayString = "${scheduler.sweeper.intervalo-ms:300000}")
    public void varrer() {
        executarEtapa("fechar zombies", this::fecharZombies);
        executarEtapa("detectar omissões", this::detectarOmissoes);
        executarEtapa("recarregar configuração", schedulerService::recarregar);
    }

    /** Uma etapa que rebente não pode impedir as seguintes: são independentes entre si. */
    private void executarEtapa(String nome, EtapaSweeper etapa) {
        try {
            etapa.correr();
        } catch (Exception e) {
            LOGGER.error("Sweeper: falha ao {} — {}", nome, e.getMessage(), e);
        }
    }

    @FunctionalInterface
    private interface EtapaSweeper {
        void correr();
    }

    int fecharZombies() {
        return registo.fecharOrfasExpiradas(Duration.ofMinutes(toleranciaMinutos));
    }

    /**
     * Para cada job activo, compara o instante previsto com o relógio. Cada instante previsto que
     * ficou para trás sem execução gera uma linha {@code OMITIDA} — com os parâmetros derivados
     * desse instante, para que a re-execução processe o período certo e não o de hoje.
     */
    public int detectarOmissoes() {
        int total = 0;
        var limite = RelogioScheduler.agora().minusMinutes(toleranciaMinutos);

        for (var entidade : jobRepository.findByActivoTrue()) {
            var original = entidade.getProximaExecucao();
            if (original == null) continue;

            var previsto = original;
            int registadas = 0;
            while (previsto != null && previsto.isBefore(limite) && registadas < MAX_OMISSOES_POR_VARRIMENTO) {
                if (registo.registarOmissao(entidade.getChave(), entidade.getNome(), entidade.getCron(),
                        previsto, parametrosDerivados(entidade.getChave(), previsto))) {
                    registadas++;
                }
                previsto = RelogioScheduler.proximaDepoisDe(entidade.getCron(), entidade.getTimezone(), previsto);
            }

            if (!Objects.equals(original, previsto)) {
                avancarPrevisao(entidade.getChave(), original, previsto);
            }
            total += registadas;
        }
        if (total > 0) LOGGER.warn("Sweeper: {} execução(ões) em falta registadas como OMITIDA", total);
        return total;
    }

    /**
     * Avança a previsão só se ninguém a mexeu entretanto: se o cron disparou (ou outra réplica
     * varreu) enquanto este varrimento corria, a previsão dessa escrita é mais recente e ganha.
     */
    void avancarPrevisao(String chave, LocalDateTime vista, LocalDateTime nova) {
        jobRepository.findByChave(chave).ifPresent(actual -> {
            if (!Objects.equals(actual.getProximaExecucao(), vista)) return;
            actual.setProximaExecucao(nova);
            jobRepository.save(actual);
        });
    }

    /**
     * Parâmetros de uma execução que não chegou a acontecer. Só se preenche a data se o job a
     * declarar — um job que não tem esse parâmetro não deve receber lixo no contexto. Deriva-se do
     * instante previsto, não de {@code now()}, pela mesma razão que no {@link JobContext}.
     */
    private Map<String, Object> parametrosDerivados(String chave, LocalDateTime previsto) {
        var job = schedulerService.getJobsByChave().get(chave);
        if (job == null) return Map.of();
        boolean aceitaData = job.getParametros().stream()
                .anyMatch(p -> JobContext.PARAM_DATA.equals(p.getNome()));
        if (!aceitaData) return Map.of();

        var parametros = new HashMap<String, Object>();
        parametros.put(JobContext.PARAM_DATA, previsto.toLocalDate().toString());
        return parametros;
    }

    /** Visível para testes: evita depender do carregamento das properties. */
    void configurarTolerancia(long toleranciaMinutos) {
        this.toleranciaMinutos = toleranciaMinutos;
    }
}
