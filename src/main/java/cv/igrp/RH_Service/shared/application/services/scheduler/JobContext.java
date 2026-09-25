package cv.igrp.RH_Service.shared.application.services.scheduler;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Contexto de uma execução, entregue ao {@link ScheduledJob#executar(JobContext)}.
 *
 * <p>O ponto central é o {@link #dataReferencia()}: o dia por omissão deriva de
 * {@link #getAgendadoPara()}, <b>nunca</b> de {@code now()}. Se a aplicação esteve em baixo a
 * 31 de Dezembro e a execução em falta só é repetida a 3 de Janeiro, {@code agendadoPara} continua
 * a ser 31 de Dezembro — e o vencimento de férias trata o ano que estava previsto, não o de hoje.
 * Com {@code now()}, a mesma execução tardia processaria o período errado em silêncio.
 */
@Getter
public final class JobContext {

    public static final String PARAM_DATA = "data";
    /** STRICT: em modo SMART, «31/02/2026» passaria como 28/02 sem ninguém dar por isso. */
    private static final DateTimeFormatter DD_MM_AAAA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private final Map<String, Object> parametros;
    private final LocalDateTime agendadoPara;
    private final TipoDisparo disparo;
    private final String solicitante;
    private final UUID execucaoId;
    private final int tentativa;

    private JobContext(Map<String, Object> parametros, LocalDateTime agendadoPara,
                       TipoDisparo disparo, String solicitante, UUID execucaoId, int tentativa) {
        this.parametros = parametros == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(parametros));
        this.agendadoPara = agendadoPara != null ? agendadoPara : RelogioScheduler.agora();
        this.disparo = disparo != null ? disparo : TipoDisparo.MANUAL;
        this.solicitante = solicitante;
        this.execucaoId = execucaoId;
        this.tentativa = tentativa <= 0 ? 1 : tentativa;
    }

    public static JobContext of(Map<String, Object> parametros, LocalDateTime agendadoPara,
                                TipoDisparo disparo, String solicitante, UUID execucaoId, int tentativa) {
        return new JobContext(parametros, agendadoPara, disparo, solicitante, execucaoId, tentativa);
    }

    /** Contexto mínimo, útil em testes e em jobs sem parâmetros. */
    public static JobContext vazio() {
        return new JobContext(Map.of(), RelogioScheduler.agora(), TipoDisparo.MANUAL, null, null, 1);
    }

    /** Contexto de um dia concreto, útil em testes. */
    public static JobContext para(LocalDate dia) {
        return new JobContext(Map.of(), dia.atStartOfDay(), TipoDisparo.MANUAL, null, null, 1);
    }

    /**
     * Dia a que a execução diz respeito: o parâmetro {@value #PARAM_DATA} se tiver sido indicado,
     * senão o dia do {@link #getAgendadoPara()}.
     */
    public LocalDate dataReferencia() {
        var explicita = getString(PARAM_DATA);
        if (explicita == null || explicita.isBlank()) return agendadoPara.toLocalDate();
        return data(explicita.trim(), "Data de referência");
    }

    public String getString(String nome) {
        var valor = parametros.get(nome);
        return valor == null ? null : String.valueOf(valor);
    }

    public String getStringObrigatorio(String nome) {
        var valor = getString(nome);
        if (valor == null || valor.isBlank())
            throw IgrpResponseStatusException.badRequest("Falta indicar o valor de «" + nome + "».");
        return valor.trim();
    }

    public Integer getInteger(String nome) {
        var valor = getString(nome);
        if (valor == null || valor.isBlank()) return null;
        try {
            return Integer.valueOf(valor.trim());
        } catch (NumberFormatException e) {
            throw IgrpResponseStatusException.badRequest("«" + nome + "» tem de ser um número inteiro.");
        }
    }

    public boolean getBoolean(String nome, boolean porOmissao) {
        var valor = getString(nome);
        return valor == null || valor.isBlank() ? porOmissao : Boolean.parseBoolean(valor.trim());
    }

    public boolean isManual() {
        return TipoDisparo.MANUAL == disparo;
    }

    /** Aceita {@code aaaa-MM-dd} (o que a interface envia) e {@code dd/MM/aaaa} (o que se escreve à mão). */
    static LocalDate data(String valor, String rotulo) {
        try {
            return valor.contains("/") ? LocalDate.parse(valor, DD_MM_AAAA) : LocalDate.parse(valor);
        } catch (DateTimeParseException e) {
            throw IgrpResponseStatusException.badRequest(
                    "«" + rotulo + "» tem de ser uma data válida, no formato dd/MM/aaaa.");
        }
    }
}
