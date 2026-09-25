package cv.igrp.RH_Service.shared.application.services.scheduler;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.scheduler.SchedulerExecucaoEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.scheduler.SchedulerJobEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.scheduler.SchedulerExecucaoEntityRepository;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.scheduler.SchedulerJobEntityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistência do ciclo de vida de uma execução. Separado do {@link JobRunner} de propósito: cada
 * escrita corre em {@link Propagation#REQUIRES_NEW}, e a auto-invocação dentro do mesmo bean não
 * passa pelo proxy do Spring — o {@code @Transactional} seria simplesmente ignorado.
 *
 * <p>Porque é que isso importa: se o job rebentou <em>porque</em> a base de dados caiu, a gravação
 * do desfecho não pode ir de boleia na transacção moribunda do job, ou a linha fica presa em
 * {@code A_CORRER} para sempre.
 *
 * <p><b>O lock entre réplicas.</b> O serviço corre com mais do que uma réplica (k8s), e o cron dispara
 * em todas ao mesmo instante. {@link #abrir} e {@link #registarOmissao} começam por bloquear a linha
 * do job ({@code SELECT … FOR UPDATE}); a verificação "já existe?" e a gravação ficam, por isso,
 * numa só secção crítica — a segunda réplica espera, vê o registo da primeira e desiste.
 */
@Service
public class ExecucaoRegistoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecucaoRegistoService.class);
    private static final int MAX_ERRO_CHARS = 8000;
    static final int MAX_ITENS_DETALHE = 200;
    private static final String INSTANCIA = hostname();

    private final SchedulerExecucaoEntityRepository execucaoRepository;
    private final SchedulerJobEntityRepository jobRepository;

    public ExecucaoRegistoService(SchedulerExecucaoEntityRepository execucaoRepository,
                                  SchedulerJobEntityRepository jobRepository) {
        this.execucaoRepository = execucaoRepository;
        this.jobRepository = jobRepository;
    }

    /**
     * Abre o registo da execução.
     *
     * @return vazio quando o pedido é um disparo do cron que outra réplica já registou — não é erro,
     *     é a mesma execução a chegar duas vezes.
     * @throws IgrpResponseStatusException 409 se já houver uma execução em curso para esta chave.
     *     A verificação é genérica de propósito: a maioria dos jobs é idempotente sequencialmente
     *     (correr duas vezes seguidas é seguro) mas não em concorrência.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<SchedulerExecucaoEntity> abrir(PedidoExecucao pedido, String cron) {
        var job = pedido.getJob();
        var chave = job.getChave();
        var agendadoPara = pedido.getAgendadoPara() != null ? pedido.getAgendadoPara() : RelogioScheduler.agora();

        jobRepository.findByChaveParaActualizar(chave);

        if (pedido.isPrimeiroDisparoAgendado() && execucaoRepository.existsByChaveAndAgendadoPara(chave, agendadoPara)) {
            LOGGER.debug("[{}] disparo de {} já registado por outra réplica — ignorado", chave, agendadoPara);
            return Optional.empty();
        }
        if (execucaoRepository.existsByChaveAndEstado(chave, EstadoExecucao.A_CORRER.name()))
            throw IgrpResponseStatusException.conflict(
                    "«" + job.getNomeLegivel() + "» já está a correr. Aguarde que termine e volte a tentar.");

        var execucao = new SchedulerExecucaoEntity();
        execucao.setId(UUID.randomUUID());
        execucao.setChave(chave);
        execucao.setNome(job.getNomeLegivel());
        execucao.setCron(cron);
        execucao.setDisparo(pedido.getDisparo().name());
        execucao.setSolicitante(pedido.getSolicitante());
        execucao.setInstancia(INSTANCIA);
        execucao.setAgendadoPara(agendadoPara);
        execucao.setInicio(RelogioScheduler.agora());
        execucao.setEstado(EstadoExecucao.A_CORRER.name());
        execucao.setTentativa(pedido.getTentativa());
        execucao.setExecucaoPaiId(pedido.getExecucaoPaiId());
        // Gravado AGORA, não no fim: uma excepção não produz JobResult e a linha ficaria sem se saber
        // a que período dizia respeito — que é justamente a linha que o utilizador vai querer repetir.
        execucao.setParametros(pedido.getParametros());
        return Optional.of(execucaoRepository.save(execucao));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public EstadoExecucao fechar(UUID id, JobResult resultado, Throwable erro, boolean timeout, long duracaoMs) {
        var execucao = execucaoRepository.findById(id).orElse(null);
        if (execucao == null) {
            LOGGER.warn("Registo de execução {} não encontrado ao finalizar — desfecho não gravado", id);
            return EstadoExecucao.FALHA;
        }
        execucao.setFim(RelogioScheduler.agora());
        execucao.setDuracaoMs(duracaoMs);

        EstadoExecucao estado;
        if (timeout) {
            estado = EstadoExecucao.TIMEOUT;
            execucao.setMensagem("A execução excedeu o tempo limite e foi interrompida.");
            if (erro != null) execucao.setErro(resumoErro(erro));
        } else if (erro != null) {
            estado = EstadoExecucao.FALHA;
            execucao.setErro(resumoErro(erro));
        } else if (resultado != null) {
            execucao.setProcessados(resultado.getProcessados());
            execucao.setCriados(resultado.getCriados());
            execucao.setRepetidos(resultado.getRepetidos());
            execucao.setSaltados(resultado.getSaltados());
            execucao.setFalhas(resultado.getFalhas());
            execucao.setReferencia(resultado.getReferencia());
            execucao.setMensagem(resultado.getMensagem());
            execucao.setDetalhes(limitarDetalhes(resultado.getDetalhes()));
            estado = resultado.getFalhas() > 0 ? EstadoExecucao.FALHA_PARCIAL : EstadoExecucao.SUCESSO;
        } else {
            // Job devolveu null sem excepção: trata-se como sucesso sem contadores.
            estado = EstadoExecucao.SUCESSO;
        }
        execucao.setEstado(estado.name());
        execucaoRepository.save(execucao);
        return estado;
    }

    /**
     * Actualiza o estado do job após a execução. {@code proximaExecucao} só avança em execuções
     * AGENDADO: um disparo manual não deve deslocar o calendário nem apagar uma omissão por detectar.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void actualizarJob(String chave, EstadoExecucao estado, boolean agendado) {
        jobRepository.findByChave(chave).ifPresent(job -> {
            job.setUltimaExecucao(RelogioScheduler.agora());
            job.setUltimoEstado(estado.name());
            if (agendado) {
                job.setProximaExecucao(RelogioScheduler.proximaDepoisDe(job.getCron(), job.getTimezone(), RelogioScheduler.agora()));
            }
            jobRepository.save(job);
        });
    }

    /** Linha sintética para uma execução agendada que nunca aconteceu. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean registarOmissao(String chave, String nome, String cron,
                                   LocalDateTime agendadoPara, Map<String, Object> parametros) {
        jobRepository.findByChaveParaActualizar(chave);
        if (execucaoRepository.existsByChaveAndAgendadoPara(chave, agendadoPara)) return false;

        var execucao = new SchedulerExecucaoEntity();
        execucao.setId(UUID.randomUUID());
        execucao.setChave(chave);
        execucao.setNome(nome);
        execucao.setCron(cron);
        execucao.setDisparo(TipoDisparo.AGENDADO.name());
        execucao.setInstancia(INSTANCIA);
        execucao.setAgendadoPara(agendadoPara);
        execucao.setInicio(agendadoPara);
        execucao.setEstado(EstadoExecucao.OMITIDA.name());
        execucao.setTentativa(1);
        execucao.setParametros(parametros);
        execucao.setMensagem("A execução agendada não aconteceu: a aplicação estava parada à hora prevista.");
        execucaoRepository.save(execucao);
        LOGGER.warn("[{}] execução agendada para {} não ocorreu — registada como OMITIDA", chave, agendadoPara);
        return true;
    }

    /**
     * Fecha as execuções que <b>esta</b> réplica deixou em {@code A_CORRER} antes de reiniciar.
     *
     * <p>Só as desta instância e só as abertas antes do arranque da JVM: com várias réplicas, uma
     * execução {@code A_CORRER} de outra instância pode estar legitimamente a correr — fechá-la
     * libertaria a guarda de concorrência a meio.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int fecharOrfasDestaInstancia() {
        var arranque = RelogioScheduler.de(Instant.ofEpochMilli(ManagementFactory.getRuntimeMXBean().getStartTime()));
        var presas = execucaoRepository.findByEstadoAndInstancia(EstadoExecucao.A_CORRER.name(), INSTANCIA).stream()
                .filter(e -> e.getInicio() != null && e.getInicio().isBefore(arranque))
                .toList();
        presas.forEach(e -> fecharComoTimeout(e, "A aplicação reiniciou durante a execução."));
        return presas.size();
    }

    /**
     * Fecha execuções presas em {@code A_CORRER} há mais do que o tempo limite do job mais uma
     * margem. O {@link JobRunner} fecha ele próprio como {@code TIMEOUT} tudo o que excede o limite;
     * uma linha que o ultrapassa sem desfecho quer dizer que o processo morreu. O limite é o de cada
     * job — um valor fixo ou era longo demais para os jobs rápidos (a guarda ficava bloqueada horas)
     * ou curto demais para os lentos.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int fecharOrfasExpiradas(Duration margem) {
        var agora = RelogioScheduler.agora();
        var timeoutPorChave = new HashMap<String, Integer>();
        var fechadas = 0;
        for (var execucao : execucaoRepository.findByEstado(EstadoExecucao.A_CORRER.name())) {
            var timeout = timeoutPorChave.computeIfAbsent(execucao.getChave(), chave -> jobRepository.findByChave(chave)
                    .map(SchedulerJobEntity::getTimeoutSegundos)
                    .orElse((int) Duration.ofMinutes(30).toSeconds()));
            var limite = execucao.getInicio().plusSeconds(timeout).plus(margem);
            if (limite.isBefore(agora)) {
                fecharComoTimeout(execucao, "A execução ficou sem desfecho dentro do tempo limite.");
                fechadas++;
            }
        }
        return fechadas;
    }

    private void fecharComoTimeout(SchedulerExecucaoEntity execucao, String mensagem) {
        execucao.setEstado(EstadoExecucao.TIMEOUT.name());
        execucao.setFim(RelogioScheduler.agora());
        execucao.setMensagem(mensagem);
        execucaoRepository.save(execucao);
        LOGGER.warn("[{}] execução {} presa em A_CORRER desde {} — fechada como TIMEOUT",
                execucao.getChave(), execucao.getId(), execucao.getInicio());
    }

    /**
     * Corta listas dentro de {@code detalhes} para não deixar uma noite má escrever um jsonb enorme.
     * Um admin não lê 5000 linhas de falhas; as primeiras 200 chegam para perceber o padrão.
     */
    static Map<String, Object> limitarDetalhes(Map<String, Object> detalhes) {
        if (detalhes == null || detalhes.isEmpty()) return detalhes;
        var limitado = new LinkedHashMap<String, Object>();
        detalhes.forEach((chave, valor) -> {
            if (valor instanceof Collection<?> colecao && colecao.size() > MAX_ITENS_DETALHE) {
                limitado.put(chave, colecao.stream().limit(MAX_ITENS_DETALHE).toList());
                limitado.put(chave + "_truncado", "mostrados %d de %d".formatted(MAX_ITENS_DETALHE, colecao.size()));
            } else {
                limitado.put(chave, valor);
            }
        });
        return limitado;
    }

    static String instancia() {
        return INSTANCIA;
    }

    private static String hostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "desconhecido";
        }
    }

    private static String resumoErro(Throwable erro) {
        var sw = new StringWriter();
        erro.printStackTrace(new PrintWriter(sw));
        var texto = sw.toString();
        return texto.length() > MAX_ERRO_CHARS ? texto.substring(0, MAX_ERRO_CHARS) : texto;
    }
}
