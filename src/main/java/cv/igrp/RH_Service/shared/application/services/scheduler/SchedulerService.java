package cv.igrp.RH_Service.shared.application.services.scheduler;

import cv.igrp.RH_Service.shared.application.constants.DiaDaSemana;
import cv.igrp.RH_Service.shared.application.constants.FrequenciaScheduler;
import cv.igrp.RH_Service.shared.application.dto.AtualizarSchedulerRequestDTO;
import cv.igrp.RH_Service.shared.application.dto.DispararSchedulerResponseDTO;
import cv.igrp.RH_Service.shared.application.dto.JobParametroDTO;
import cv.igrp.RH_Service.shared.application.dto.SchedulerConfigDTO;
import cv.igrp.RH_Service.shared.application.dto.SchedulerExecucaoResponseDTO;
import cv.igrp.RH_Service.shared.application.dto.WrapperListaSchedulerExecucoesDTO;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.scheduler.SchedulerExecucaoEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.scheduler.SchedulerJobEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.scheduler.SchedulerExecucaoEntityRepository;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.scheduler.SchedulerJobEntityRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * Motor do agendamento. Descobre os {@link ScheduledJob} por injecção, semeia a configuração de
 * cada um em {@code t_scheduler_job} e agenda-os com {@link CronTrigger}. A partir do arranque,
 * a fonte de verdade do agendamento é a base de dados — o {@code getCronPadrao()} do código é só
 * o valor inicial.
 */
@Component
public class SchedulerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerService.class);
    private static final Locale LOCALE_PT = Locale.forLanguageTag("pt-PT");
    static final String TIMEZONE_PADRAO = RelogioScheduler.ZONA.getId();

    /**
     * Quanto pode um disparo do cron chegar atrasado e ainda contar como o disparo previsto. Para lá
     * disto, a {@code proximaExecucao} gravada é de um disparo antigo que o sweeper ainda não tratou.
     */
    private static final Duration ATRASO_ACEITE = Duration.ofMinutes(10);

    private final List<ScheduledJob> jobs;
    private final SchedulerJobEntityRepository jobRepository;
    private final SchedulerExecucaoEntityRepository execucaoRepository;
    private final JobExecutores executores;
    private final JobRunner jobRunner;

    private final Map<String, ScheduledJob> jobsByChave = new HashMap<>();
    private final Map<String, ScheduledFuture<?>> tasks = new ConcurrentHashMap<>();
    /** Assinatura (cron + timezone + activo) do que está agendado, para o reload só mexer no que mudou. */
    private final Map<String, String> assinaturas = new ConcurrentHashMap<>();

    public SchedulerService(List<ScheduledJob> jobs, SchedulerJobEntityRepository jobRepository,
                            SchedulerExecucaoEntityRepository execucaoRepository, JobExecutores executores,
                            JobRunner jobRunner) {
        this.jobs = jobs;
        this.jobRepository = jobRepository;
        this.execucaoRepository = execucaoRepository;
        this.executores = executores;
        this.jobRunner = jobRunner;
    }

    @PostConstruct
    public void inicializar() {
        LOGGER.info("SchedulerService: a iniciar {} job(s)", jobs.size());
        for (var job : jobs) {
            jobsByChave.put(job.getChave(), job);
            try {
                semear(job);
                reagendar(job);
            } catch (Exception e) {
                LOGGER.error("SchedulerService: falha ao agendar job '{}' — ignorado: {}",
                        job.getChave(), e.getMessage(), e);
            }
        }
        LOGGER.info("SchedulerService: {} job(s) agendados (de {})", tasks.size(), jobs.size());
    }

    // ── configuração ─────────────────────────────────────────────────────────

    @Transactional
    public void atualizarScheduler(String chave, AtualizarSchedulerRequestDTO dto) {
        var frequencia = FrequenciaScheduler.fromCodeOrThrow(dto.getFrequencia());
        validarCampos(frequencia, dto);

        var cron = buildCron(frequencia, dto);
        if (!CronExpression.isValidExpression(cron))
            throw IgrpResponseStatusException.badRequest("Não foi possível montar o agendamento com estes valores.");

        var job = jobExigido(chave);
        var entidade = entidadeExigida(chave);
        var timezone = validarTimezone(dto.getTimezone(), entidade.getTimezone());

        entidade.setCron(cron);
        entidade.setFrequencia(frequencia.getCode());
        entidade.setTimezone(timezone);
        // O calendário mudou: a próxima execução prevista tem de ser recalculada, senão o sweeper
        // acusaria como omissão um instante que já não faz parte do agendamento.
        entidade.setProximaExecucao(Boolean.TRUE.equals(entidade.getActivo())
                ? RelogioScheduler.proximaDepoisDe(cron, timezone, RelogioScheduler.agora()) : null);
        jobRepository.save(entidade);

        reagendar(job);
        LOGGER.info("Scheduler '{}' actualizado para cron '{}' ({} / {})",
                chave, cron, frequencia.getCode(), timezone);
    }

    /**
     * Liga ou desliga o job sem perder a configuração. Ao reactivar, a próxima execução é recalculada
     * a partir de agora — o período em que esteve desligado não gera omissões, porque não foram
     * omissões: foi uma decisão.
     */
    @Transactional
    public void alterarEstado(String chave, boolean activo) {
        var job = jobExigido(chave);
        var entidade = entidadeExigida(chave);
        entidade.setActivo(activo);
        entidade.setProximaExecucao(activo
                ? RelogioScheduler.proximaDepoisDe(entidade.getCron(), entidade.getTimezone(), RelogioScheduler.agora())
                : null);
        jobRepository.save(entidade);
        reagendar(job);
        LOGGER.info("Scheduler '{}' {}", chave, activo ? "activado" : "desactivado");
    }

    // ── disparo ──────────────────────────────────────────────────────────────

    /**
     * Dispara um job imediatamente, de forma assíncrona (sem Open-Session-In-View, comportando-se
     * como o cron). Os parâmetros são validados e o registo é aberto de forma síncrona, para que um
     * valor inválido chegue ao cliente como 400 e um conflito de concorrência como 409, em vez de se
     * perderem num thread de fundo.
     */
    public DispararSchedulerResponseDTO dispararManual(String chave, Map<String, Object> parametros,
                                                       String solicitante) {
        var job = jobExigido(chave);
        var pedido = PedidoExecucao.builder()
                .job(job)
                .disparo(TipoDisparo.MANUAL)
                .solicitante(solicitante)
                .parametros(validarParametros(job, parametros))
                .agendadoPara(RelogioScheduler.agora())
                .build();
        return dispararAssincrono(job, pedido);
    }

    /**
     * Repete uma execução do histórico com <b>os mesmos parâmetros</b> e o mesmo instante agendado.
     *
     * <p>É esta a diferença para o {@code dispararManual}: o admin não escolhe o período, o sistema
     * lê-o da linha que falhou. Serve tanto uma {@code FALHA} como uma {@code OMITIDA} — em ambos os
     * casos o instante e os parâmetros da execução original já estão gravados, e a nova execução
     * fica ligada à anterior por {@code execucaoPaiId}.
     */
    public DispararSchedulerResponseDTO reexecutar(String execucaoId, String solicitante) {
        var original = execucaoRepository.findById(idValido(execucaoId))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Execução não encontrada."));
        var job = jobExigido(original.getChave());

        var pedido = PedidoExecucao.builder()
                .job(job)
                .disparo(TipoDisparo.MANUAL)
                .solicitante(solicitante)
                .parametros(original.getParametros())
                .agendadoPara(original.getAgendadoPara() != null ? original.getAgendadoPara() : RelogioScheduler.agora())
                .execucaoPaiId(original.getId())
                .build();

        LOGGER.info("Re-execução de {} ({}) pedida por {}", original.getId(), original.getChave(), solicitante);
        return dispararAssincrono(job, pedido);
    }

    private DispararSchedulerResponseDTO dispararAssincrono(ScheduledJob job, PedidoExecucao pedido) {
        // Um pedido manual nunca é deduplicado (só o primeiro disparo do cron o é), por isso há sempre registo.
        var aberta = jobRunner.iniciar(pedido).orElseThrow();
        executores.disparador().schedule(() -> jobRunner.executar(aberta, job), Instant.now());

        var dto = new DispararSchedulerResponseDTO();
        dto.setId(aberta.id());
        dto.setChave(job.getChave());
        dto.setEstado(EstadoExecucao.A_CORRER.name());
        dto.setParametros(aberta.contexto().getParametros());
        dto.setExecucaoPaiId(pedido.getExecucaoPaiId());
        return dto;
    }

    // ── consulta ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public WrapperListaSchedulerExecucoesDTO listarExecucoes(
            String chave, String estado, String disparo, String referencia,
            Integer pageNumber, Integer pageSize) {

        int pn = pageNumber != null ? Math.max(pageNumber, 0) : 0;
        int ps = pageSize != null ? Math.max(pageSize, 1) : 20;
        var pageRequest = PageRequest.of(pn, ps, Sort.by(Sort.Direction.DESC, "inicio", "id"));

        Specification<SchedulerExecucaoEntity> spec = (root, cq, cb) -> {
            var predicates = cb.conjunction();
            if (chave != null && !chave.isBlank())
                predicates = cb.and(predicates, cb.equal(root.get("chave"), chave));
            if (estado != null && !estado.isBlank())
                predicates = cb.and(predicates, cb.equal(root.get("estado"), estado));
            if (disparo != null && !disparo.isBlank())
                predicates = cb.and(predicates, cb.equal(root.get("disparo"), disparo));
            if (referencia != null && !referencia.isBlank())
                predicates = cb.and(predicates, cb.equal(root.get("referencia"), referencia));
            return predicates;
        };

        var page = execucaoRepository.findAll(spec, pageRequest);

        var wrapper = new WrapperListaSchedulerExecucoesDTO();
        wrapper.setContent(page.getContent().stream().map(SchedulerService::toExecucaoDTO).toList());
        wrapper.setPageNumber(page.getNumber());
        wrapper.setPageSize(page.getSize());
        wrapper.setTotalElements(page.getTotalElements());
        wrapper.setTotalPages(page.getTotalPages());
        wrapper.setFirst(page.isFirst());
        wrapper.setLast(page.isLast());
        return wrapper;
    }

    @Transactional(readOnly = true)
    public SchedulerExecucaoResponseDTO getExecucao(String id) {
        return execucaoRepository.findById(idValido(id))
                .map(SchedulerService::toExecucaoDTO)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Execução não encontrada."));
    }

    @Transactional(readOnly = true)
    public SchedulerConfigDTO getConfiguracao(String chave) {
        var job = jobExigido(chave);
        return toConfigDTO(job, jobRepository.findByChave(chave).orElse(null));
    }

    @Transactional(readOnly = true)
    public List<SchedulerConfigDTO> listarConfiguracoes() {
        return jobs.stream()
                .map(job -> toConfigDTO(job, jobRepository.findByChave(job.getChave()).orElse(null)))
                .toList();
    }

    // ── reload (usado pelo sweeper) ──────────────────────────────────────────

    /**
     * Reagenda os jobs cuja configuração mudou em BD desde o último agendamento.
     *
     * <p>Sem isto, uma alteração feita através da API só produzia efeito na réplica que recebeu o
     * pedido — as restantes continuavam com o cron antigo até reiniciarem, e o utilizador via
     * "guardado com sucesso" enquanto o job continuava a correr à hora velha.
     */
    @Transactional(readOnly = true)
    public int recarregar() {
        int alterados = 0;
        for (var job : jobs) {
            var entidade = jobRepository.findByChave(job.getChave()).orElse(null);
            if (entidade == null) continue;
            if (!assinatura(entidade).equals(assinaturas.get(job.getChave()))) {
                reagendar(job);
                alterados++;
            }
        }
        if (alterados > 0) LOGGER.info("SchedulerService: {} job(s) reagendados após alteração externa", alterados);
        return alterados;
    }

    Map<String, ScheduledJob> getJobsByChave() {
        return jobsByChave;
    }

    // ── validação ────────────────────────────────────────────────────────────

    static void validarCampos(FrequenciaScheduler freq, AtualizarSchedulerRequestDTO dto) {
        validarHoraMinuto(dto.getHora(), dto.getMinuto());
        switch (freq) {
            case DIARIO, QUINZENAL -> { }
            case SEMANAL -> {
                if (dto.getDiaDaSemana() == null || dto.getDiaDaSemana().isBlank())
                    throw IgrpResponseStatusException.badRequest("Escolha o dia da semana.");
                DiaDaSemana.fromCodeOrThrow(dto.getDiaDaSemana());
            }
            case MENSAL, TRIMESTRAL, SEMESTRAL -> validarDiaDoMes(dto.getDiaDoMes());
            case ANUAL -> {
                validarDiaDoMes(dto.getDiaDoMes());
                if (dto.getMes() == null || dto.getMes() < 1 || dto.getMes() > 12)
                    throw IgrpResponseStatusException.badRequest("Escolha o mês (de 1 a 12).");
            }
        }
    }

    private static void validarHoraMinuto(Integer hora, Integer minuto) {
        if (hora == null || hora < 0 || hora > 23)
            throw IgrpResponseStatusException.badRequest("A hora tem de estar entre 0 e 23.");
        if (minuto == null || minuto < 0 || minuto > 59)
            throw IgrpResponseStatusException.badRequest("O minuto tem de estar entre 0 e 59.");
    }

    /** Até 28, para o agendamento existir em todos os meses — Fevereiro incluído. */
    private static void validarDiaDoMes(Integer diaDoMes) {
        if (diaDoMes == null || diaDoMes < 1 || diaDoMes > 28)
            throw IgrpResponseStatusException.badRequest("O dia do mês tem de estar entre 1 e 28.");
    }

    static String validarTimezone(String pedido, String actual) {
        if (pedido == null || pedido.isBlank()) return actual != null ? actual : TIMEZONE_PADRAO;
        try {
            return ZoneId.of(pedido.trim()).getId();
        } catch (Exception e) {
            throw IgrpResponseStatusException.badRequest("Fuso horário desconhecido: " + pedido.trim() + ".");
        }
    }

    /**
     * Confere os parâmetros do disparo manual contra os que o job declara, e devolve-os normalizados
     * (datas em {@code aaaa-MM-dd}, vazios retirados). Validar aqui, e não dentro do job, é o que faz
     * um valor inválido voltar ao utilizador como 400 — dentro do job seria só mais uma FALHA no
     * histórico.
     */
    static Map<String, Object> validarParametros(ScheduledJob job, Map<String, Object> recebidos) {
        var declarados = new LinkedHashMap<String, JobParametro>();
        job.getParametros().forEach(p -> declarados.put(p.getNome(), p));

        var entrada = recebidos == null ? Map.<String, Object>of() : recebidos;
        for (var nome : entrada.keySet()) {
            if (!declarados.containsKey(nome))
                throw IgrpResponseStatusException.badRequest(
                        "«" + job.getNomeLegivel() + "» não aceita o campo «" + nome + "».");
        }

        var normalizados = new LinkedHashMap<String, Object>();
        for (var parametro : declarados.values()) {
            var bruto = entrada.get(parametro.getNome());
            var texto = bruto == null ? "" : String.valueOf(bruto).trim();
            if (texto.isEmpty()) {
                if (parametro.isObrigatorio())
                    throw IgrpResponseStatusException.badRequest("Indique «" + parametro.getRotulo() + "».");
                continue;
            }
            normalizados.put(parametro.getNome(), normalizar(parametro, texto));
        }
        return normalizados;
    }

    private static Object normalizar(JobParametro parametro, String texto) {
        var rotulo = parametro.getRotulo();
        return switch (parametro.getTipo()) {
            case DATA -> JobContext.data(texto, rotulo).toString();
            case ANO -> {
                var ano = inteiro(texto, rotulo);
                if (ano < 1900 || ano > 2999)
                    throw IgrpResponseStatusException.badRequest("«" + rotulo + "» tem de ser um ano com quatro algarismos.");
                yield ano;
            }
            case INTEIRO -> inteiro(texto, rotulo);
            case BOOLEANO -> {
                if (!texto.equalsIgnoreCase("true") && !texto.equalsIgnoreCase("false"))
                    throw IgrpResponseStatusException.badRequest("«" + rotulo + "» tem de ser sim ou não.");
                yield Boolean.parseBoolean(texto);
            }
            case UUID -> {
                try {
                    yield java.util.UUID.fromString(texto).toString();
                } catch (IllegalArgumentException e) {
                    throw IgrpResponseStatusException.badRequest("«" + rotulo + "» não é um identificador válido.");
                }
            }
            case TEXTO -> texto;
        };
    }

    private static int inteiro(String texto, String rotulo) {
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            throw IgrpResponseStatusException.badRequest("«" + rotulo + "» tem de ser um número inteiro.");
        }
    }

    private static UUID idValido(String valor) {
        try {
            return UUID.fromString(valor);
        } catch (IllegalArgumentException e) {
            throw IgrpResponseStatusException.badRequest("Identificador de execução inválido.");
        }
    }

    // ── construção do cron ───────────────────────────────────────────────────

    static String buildCron(FrequenciaScheduler freq, AtualizarSchedulerRequestDTO dto) {
        int m = dto.getMinuto();
        int h = dto.getHora();
        return switch (freq) {
            case DIARIO     -> "0 %d %d * * *".formatted(m, h);
            case SEMANAL    -> "0 %d %d * * %s".formatted(m, h, dto.getDiaDaSemana());
            case QUINZENAL  -> "0 %d %d 1,15 * *".formatted(m, h);
            case MENSAL     -> "0 %d %d %d * *".formatted(m, h, dto.getDiaDoMes());
            case TRIMESTRAL -> "0 %d %d %d 1,4,7,10 *".formatted(m, h, dto.getDiaDoMes());
            case SEMESTRAL  -> "0 %d %d %d 1,7 *".formatted(m, h, dto.getDiaDoMes());
            case ANUAL      -> "0 %d %d %d %d *".formatted(m, h, dto.getDiaDoMes(), dto.getMes());
        };
    }

    // ── mapeamento ───────────────────────────────────────────────────────────

    static SchedulerConfigDTO toConfigDTO(ScheduledJob job, SchedulerJobEntity entidade) {
        var cron = entidade != null ? entidade.getCron() : job.getCronPadrao();
        var freqHint = entidade != null ? entidade.getFrequencia() : null;
        var dto = descreverCron(job, cron, freqHint);

        dto.setTimezone(entidade != null ? entidade.getTimezone() : TIMEZONE_PADRAO);
        dto.setActivo(entidade == null || Boolean.TRUE.equals(entidade.getActivo()));
        dto.setMaxTentativas(entidade != null && entidade.getMaxTentativas() != null
                ? entidade.getMaxTentativas() : job.getMaxTentativas());
        dto.setTimeoutSegundos(entidade != null && entidade.getTimeoutSegundos() != null
                ? entidade.getTimeoutSegundos() : (int) job.getTimeout().toSeconds());
        if (entidade != null) {
            dto.setUltimaExecucao(entidade.getUltimaExecucao());
            dto.setUltimoEstado(entidade.getUltimoEstado());
            dto.setProximaExecucao(entidade.getProximaExecucao());
        }
        dto.setParametros(job.getParametros().stream().map(SchedulerService::toParametroDTO).toList());
        return dto;
    }

    static JobParametroDTO toParametroDTO(JobParametro parametro) {
        var dto = new JobParametroDTO();
        dto.setNome(parametro.getNome());
        dto.setRotulo(parametro.getRotulo());
        dto.setTipo(parametro.getTipo().name());
        dto.setObrigatorio(parametro.isObrigatorio());
        dto.setAjuda(parametro.getAjuda());
        dto.setExemplo(parametro.getExemplo());
        return dto;
    }

    static SchedulerExecucaoResponseDTO toExecucaoDTO(SchedulerExecucaoEntity e) {
        var dto = new SchedulerExecucaoResponseDTO();
        dto.setId(e.getId());
        dto.setChave(e.getChave());
        dto.setNome(e.getNome());
        dto.setCron(e.getCron());
        dto.setDisparo(e.getDisparo());
        dto.setSolicitante(e.getSolicitante());
        dto.setInstancia(e.getInstancia());
        dto.setAgendadoPara(e.getAgendadoPara());
        dto.setInicio(e.getInicio());
        dto.setFim(e.getFim());
        dto.setDuracaoMs(e.getDuracaoMs());
        dto.setEstado(e.getEstado());
        dto.setErro(e.getErro());
        dto.setProcessados(e.getProcessados());
        dto.setCriados(e.getCriados());
        dto.setRepetidos(e.getRepetidos());
        dto.setSaltados(e.getSaltados());
        dto.setFalhas(e.getFalhas());
        dto.setReferencia(e.getReferencia());
        dto.setMensagem(e.getMensagem());
        dto.setParametros(e.getParametros());
        dto.setDetalhes(e.getDetalhes());
        dto.setTentativa(e.getTentativa());
        dto.setExecucaoPaiId(e.getExecucaoPaiId());
        return dto;
    }

    // ── parsing reverso do cron ──────────────────────────────────────────────

    static SchedulerConfigDTO descreverCron(ScheduledJob job, String cron, String freqHint) {
        var dto = new SchedulerConfigDTO();
        dto.setChave(job.getChave());
        dto.setNome(job.getNomeLegivel());

        var partes = cron == null ? new String[0] : cron.trim().split("\\s+");
        if (partes.length != 6) {
            dto.setDescricao("Agendamento personalizado");
            return dto;
        }

        Integer minuto = parseIntOrNull(partes[1]);
        Integer hora = parseIntOrNull(partes[2]);
        dto.setMinuto(minuto);
        dto.setHora(hora);
        dto.setHoraDesc(hora != null && minuto != null ? "%02d:%02d".formatted(hora, minuto) : null);

        // Hora ou minuto não fixos ("*/5", "0-30") não cabem em nenhuma frequência da interface.
        var freq = hora != null && minuto != null ? detectFrequencia(partes, freqHint) : null;
        if (freq == null) {
            dto.setDescricao("Agendamento personalizado");
            return dto;
        }

        dto.setFrequencia(freq.getCode());
        dto.setFrequenciaDesc(freq.getDescription());

        switch (freq) {
            case DIARIO -> dto.setDescricao("Diário às %s".formatted(dto.getHoraDesc()));
            case SEMANAL -> {
                var diaSemana = partes[5];
                dto.setDiaDaSemana(diaSemana);
                DiaDaSemana.fromCode(diaSemana).ifPresent(d -> dto.setDiaDaSemanaDesc(d.getDescription()));
                dto.setDescricao("Semanal — %s às %s".formatted(
                        dto.getDiaDaSemanaDesc() != null ? dto.getDiaDaSemanaDesc() : diaSemana,
                        dto.getHoraDesc()));
            }
            case QUINZENAL -> dto.setDescricao("Quinzenal — dias 1 e 15 às %s".formatted(dto.getHoraDesc()));
            case MENSAL -> {
                preencherDiaDoMes(dto, partes);
                dto.setDescricao("Mensal — dia %s às %s".formatted(diaOuInterrogacao(dto), dto.getHoraDesc()));
            }
            case TRIMESTRAL -> {
                preencherDiaDoMes(dto, partes);
                dto.setDescricao("Trimestral — dia %s às %s (Jan, Abr, Jul, Out)"
                        .formatted(diaOuInterrogacao(dto), dto.getHoraDesc()));
            }
            case SEMESTRAL -> {
                preencherDiaDoMes(dto, partes);
                dto.setDescricao("Semestral — dia %s às %s (Jan, Jul)"
                        .formatted(diaOuInterrogacao(dto), dto.getHoraDesc()));
            }
            case ANUAL -> {
                preencherDiaDoMes(dto, partes);
                dto.setMes(parseIntOrNull(partes[4]));
                if (dto.getMes() != null && dto.getMes() >= 1 && dto.getMes() <= 12) {
                    dto.setMesDesc(Month.of(dto.getMes()).getDisplayName(TextStyle.FULL, LOCALE_PT));
                }
                dto.setDescricao("Anual — %s dia %s às %s".formatted(
                        dto.getMesDesc() != null ? dto.getMesDesc() : "mês " + partes[4],
                        diaOuInterrogacao(dto),
                        dto.getHoraDesc()));
            }
        }
        return dto;
    }

    private static void preencherDiaDoMes(SchedulerConfigDTO dto, String[] partes) {
        dto.setDiaDoMes(parseIntOrNull(partes[3]));
        dto.setDiaDoMesDesc(dto.getDiaDoMes() != null ? "Dia " + dto.getDiaDoMes() + " do mês" : null);
    }

    private static Object diaOuInterrogacao(SchedulerConfigDTO dto) {
        return dto.getDiaDoMes() != null ? dto.getDiaDoMes() : "?";
    }

    /**
     * Frequência a partir do cron. {@code ?} vale o mesmo que {@code *} — o Spring aceita ambos, e um
     * cron semeado com {@code 0 5 0 * * ?} não pode passar por semanal.
     */
    private static FrequenciaScheduler detectFrequencia(String[] partes, String freqHint) {
        if (freqHint != null) {
            var fromHint = FrequenciaScheduler.fromCode(freqHint);
            if (fromHint.isPresent()) return fromHint.get();
        }

        String dayOfMonth = qualquer(partes[3]);
        String month      = qualquer(partes[4]);
        String dayOfWeek  = qualquer(partes[5]);

        if (!"*".equals(dayOfWeek))                             return FrequenciaScheduler.SEMANAL;
        if ("1,15".equals(dayOfMonth) && "*".equals(month))     return FrequenciaScheduler.QUINZENAL;
        if ("1,4,7,10".equals(month))                           return FrequenciaScheduler.TRIMESTRAL;
        if ("1,7".equals(month))                                return FrequenciaScheduler.SEMESTRAL;
        if (!"*".equals(month) && !"*".equals(dayOfMonth))      return FrequenciaScheduler.ANUAL;
        if ("*".equals(dayOfMonth) && "*".equals(month))        return FrequenciaScheduler.DIARIO;
        if (!"*".equals(dayOfMonth) && "*".equals(month))       return FrequenciaScheduler.MENSAL;

        return null;
    }

    private static String qualquer(String campo) {
        return "?".equals(campo) ? "*" : campo;
    }

    // ── internals ────────────────────────────────────────────────────────────

    private ScheduledJob jobExigido(String chave) {
        var job = jobsByChave.get(chave);
        if (job == null) throw IgrpResponseStatusException.notFound("Tarefa agendada não encontrada.");
        return job;
    }

    private SchedulerJobEntity entidadeExigida(String chave) {
        return jobRepository.findByChave(chave)
                .orElseThrow(() -> IgrpResponseStatusException.internalServerError(
                        "A configuração desta tarefa não foi encontrada. Contacte o suporte."));
    }

    /**
     * Cria a configuração do job, se ainda não existir. Duas réplicas a arrancar ao mesmo tempo podem
     * tentar ambas: a chave é única, a segunda falha a gravar, e isso quer só dizer que a outra já
     * semeou — não pode impedir esta réplica de agendar o job.
     */
    private void semear(ScheduledJob job) {
        if (jobRepository.findByChave(job.getChave()).isPresent()) return;

        var cron = job.getCronPadrao();
        var entidade = new SchedulerJobEntity();
        entidade.setId(UUID.randomUUID());
        entidade.setChave(job.getChave());
        entidade.setNome(job.getNomeLegivel());
        entidade.setCron(cron);
        entidade.setFrequencia(descreverCron(job, cron, null).getFrequencia());
        entidade.setTimezone(TIMEZONE_PADRAO);
        entidade.setActivo(job.isActivoPorOmissao());
        entidade.setMaxTentativas(job.getMaxTentativas());
        entidade.setTimeoutSegundos((int) job.getTimeout().toSeconds());
        entidade.setProximaExecucao(job.isActivoPorOmissao()
                ? RelogioScheduler.proximaDepoisDe(cron, TIMEZONE_PADRAO, RelogioScheduler.agora())
                : null);
        entidade.setDescricao(job.getNomeLegivel());
        try {
            jobRepository.save(entidade);
            LOGGER.info("Scheduler '{}' semeado com cron padrão '{}'", job.getChave(), cron);
        } catch (DataIntegrityViolationException e) {
            LOGGER.info("Scheduler '{}' já semeado por outra réplica", job.getChave());
        }
    }

    private synchronized void reagendar(ScheduledJob job) {
        var chave = job.getChave();
        var anterior = tasks.remove(chave);
        if (anterior != null) anterior.cancel(false);
        assinaturas.remove(chave);

        var entidade = entidadeExigida(chave);
        if (!Boolean.TRUE.equals(entidade.getActivo())) {
            LOGGER.info("Scheduler '{}' está inactivo — não agendado", chave);
            assinaturas.put(chave, assinatura(entidade));
            return;
        }

        var fuso = TimeZone.getTimeZone(ZoneId.of(entidade.getTimezone()));
        var future = executores.disparador().schedule(
                () -> dispararAgendado(job), new CronTrigger(entidade.getCron(), fuso));
        if (future != null) tasks.put(chave, future);
        assinaturas.put(chave, assinatura(entidade));
    }

    /**
     * O que o {@link CronTrigger} corre. O {@code agendadoPara} é o instante que estava previsto
     * ({@code proximaExecucao}), e não {@code now()}: o período processado é o previsto mesmo que o
     * disparo chegue com atraso, e é esse instante — igual em todas as réplicas — que deixa o registo
     * reconhecer o mesmo disparo a chegar duas vezes.
     */
    private void dispararAgendado(ScheduledJob job) {
        var previsto = jobRepository.findByChave(job.getChave())
                .map(SchedulerJobEntity::getProximaExecucao)
                .orElse(null);
        try {
            jobRunner.runAgendado(job, instanteDoDisparo(previsto, RelogioScheduler.agora()));
        } catch (Exception e) {
            // Tipicamente a guarda de concorrência: a execução anterior ainda não terminou.
            LOGGER.warn("[{}] disparo agendado não arrancou: {}", job.getChave(), e.getMessage());
        }
    }

    /**
     * O instante previsto, se for plausível para este disparo; senão, a hora actual ao minuto.
     *
     * <p>A {@code proximaExecucao} pode estar desactualizada — a aplicação esteve parada e o sweeper
     * ainda não passou. Usá-la nesse caso atribuía este disparo a um dia antigo (e o registo
     * trataria como duplicado, e ignoraria, um disparo que é novo).
     */
    static LocalDateTime instanteDoDisparo(LocalDateTime previsto, LocalDateTime agora) {
        var aoMinuto = agora.withSecond(0).withNano(0);
        if (previsto == null) return aoMinuto;
        boolean plausivel = !previsto.isAfter(agora.plusMinutes(1)) && !previsto.isBefore(agora.minus(ATRASO_ACEITE));
        return plausivel ? previsto : aoMinuto;
    }

    private static String assinatura(SchedulerJobEntity entidade) {
        return "%s|%s|%s".formatted(entidade.getCron(), entidade.getTimezone(), entidade.getActivo());
    }

    private static Integer parseIntOrNull(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
