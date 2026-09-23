package cv.igrp.RH_Service.parametrizacoes.domain.models;

import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.ParametroFeriasId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.regex.Pattern;

/**
 * Os valores que a lei fixa para a marcação das férias — DL n.º 3/2010, arts. 5.º e 6.º.
 *
 * <p>Vivem em tabela ({@code t_parametro_ferias}) para mudarem sem tocar em código nem reiniciar
 * a aplicação. São <b>globais</b> — a lei é a mesma para todas as instituições — e têm
 * <b>vigência</b>: cada linha vale a partir de um ano, até à linha seguinte. Um diploma novo é uma
 * linha nova, e os mapas dos anos anteriores continuam a ler as regras do seu tempo.
 *
 * <p>Sem linha nenhuma para um ano valem os da lei ({@link #daLei()}), que também é o que o seed
 * carrega: uma instalação sem dados não fica sem mapa de férias.
 *
 * <p>As datas escrevem-se {@code MM-dd}.
 */
@Getter
public class ParametroFerias {

    /** Primeiro ano de vigência do DL n.º 3/2010 — o da linha que o seed carrega. */
    public static final int VIGENCIA_DA_LEI = 2010;
    public static final String FUNDAMENTO_DA_LEI = "DL n.º 3/2010, arts. 5.º e 6.º";

    private static final Pattern MM_DD = Pattern.compile("\\d{2}-\\d{2}");

    /** Nulo nos valores da lei por omissão, que não vêm da tabela. */
    private ParametroFeriasId id;
    private int vigenteDesde;
    /** Art. 5.º n.º 4: o trabalhador indica a preferência até 31 de Janeiro. */
    private MonthDay prazoPreferencia;
    /** Art. 6.º n.º 1: o serviço elabora o mapa e dá conhecimento até 31 de Março. */
    private MonthDay prazoMapa;
    /** Art. 5.º n.º 5: sem acordo, o dirigente fixa entre 1 de Maio... */
    private MonthDay fixacaoInicio;
    /** ...e 31 de Outubro. */
    private MonthDay fixacaoFim;
    /** Art. 5.º n.º 1: em gozo interpolado, um dos períodos tem pelo menos 11 dias úteis. */
    private int periodoMinimoInterpolado;
    /** O diploma de onde vêm os valores. Texto livre, informativo. */
    private String fundamento;

    private ParametroFerias() {}

    private ParametroFerias(ParametroFeriasId id, int vigenteDesde, MonthDay prazoPreferencia,
                            MonthDay prazoMapa, MonthDay fixacaoInicio, MonthDay fixacaoFim,
                            int periodoMinimoInterpolado, String fundamento) {
        this.id = id;
        this.vigenteDesde = vigenteDesde;
        this.prazoPreferencia = prazoPreferencia;
        this.prazoMapa = prazoMapa;
        this.fixacaoInicio = fixacaoInicio;
        this.fixacaoFim = fixacaoFim;
        this.periodoMinimoInterpolado = periodoMinimoInterpolado;
        this.fundamento = fundamento;
    }

    public static ParametroFerias criar(Integer vigenteDesde, String prazoPreferencia, String prazoMapa,
                                        String fixacaoInicio, String fixacaoFim,
                                        Integer periodoMinimoInterpolado, String fundamento) {
        var p = new ParametroFerias();
        p.id = ParametroFeriasId.gerarNovo();
        p.aplicar(vigenteDesde, prazoPreferencia, prazoMapa, fixacaoInicio, fixacaoFim,
                periodoMinimoInterpolado, fundamento);
        return p;
    }

    public static ParametroFerias reconstruir(ParametroFeriasId id, int vigenteDesde, String prazoPreferencia,
                                              String prazoMapa, String fixacaoInicio, String fixacaoFim,
                                              int periodoMinimoInterpolado, String fundamento) {
        return new ParametroFerias(id, vigenteDesde, MonthDay.parse("--" + prazoPreferencia),
                MonthDay.parse("--" + prazoMapa), MonthDay.parse("--" + fixacaoInicio),
                MonthDay.parse("--" + fixacaoFim), periodoMinimoInterpolado, fundamento);
    }

    /** Os valores do DL n.º 3/2010, para quando a tabela não tem linha que valha no ano. */
    public static ParametroFerias daLei() {
        return new ParametroFerias(null, VIGENCIA_DA_LEI, MonthDay.of(1, 31), MonthDay.of(3, 31),
                MonthDay.of(5, 1), MonthDay.of(10, 31), 11, FUNDAMENTO_DA_LEI);
    }

    /** Todos os campos obrigatórios: quem altera envia o valor final de cada um. */
    public void atualizar(Integer vigenteDesde, String prazoPreferencia, String prazoMapa,
                          String fixacaoInicio, String fixacaoFim,
                          Integer periodoMinimoInterpolado, String fundamento) {
        aplicar(vigenteDesde, prazoPreferencia, prazoMapa, fixacaoInicio, fixacaoFim,
                periodoMinimoInterpolado, fundamento);
    }

    public boolean isDaLei() { return id == null; }

    public LocalDate prazoPreferencia(int ano) { return prazoPreferencia.atYear(ano); }
    public LocalDate prazoMapa(int ano) { return prazoMapa.atYear(ano); }
    public LocalDate fixacaoInicio(int ano) { return fixacaoInicio.atYear(ano); }
    public LocalDate fixacaoFim(int ano) { return fixacaoFim.atYear(ano); }

    /** {@code MM-dd}, como se escreve no pedido e na tabela. */
    public static String texto(MonthDay dia) {
        return String.format("%02d-%02d", dia.getMonthValue(), dia.getDayOfMonth());
    }

    private void aplicar(Integer vigenteDesde, String prazoPreferencia, String prazoMapa,
                         String fixacaoInicio, String fixacaoFim,
                         Integer periodoMinimoInterpolado, String fundamento) {
        if (vigenteDesde == null || vigenteDesde < 1900 || vigenteDesde > 9999)
            throw invalido("O ano de início da vigência é obrigatório e tem quatro algarismos.");
        MonthDay preferencia = dia("prazoPreferencia", prazoPreferencia);
        MonthDay mapa = dia("prazoMapa", prazoMapa);
        MonthDay inicio = dia("fixacaoInicio", fixacaoInicio);
        MonthDay fim = dia("fixacaoFim", fixacaoFim);
        if (periodoMinimoInterpolado == null || periodoMinimoInterpolado < 1)
            throw invalido("O período mínimo em gozo interpolado é obrigatório e tem pelo menos 1 dia útil.");

        // O mapa faz-se a partir das preferências (art. 5.º n.º 4 e art. 6.º n.º 1): um prazo de
        // preferência depois do do mapa não deixava a ninguém tempo para as ler.
        if (preferencia.isAfter(mapa))
            throw invalido("O prazo da preferência (" + texto(preferencia)
                    + ") não pode ser depois do prazo do mapa (" + texto(mapa) + ").");
        // A janela de fixação é dentro do ano civil, como a do art. 5.º n.º 5.
        if (inicio.isAfter(fim))
            throw invalido("O início da janela de fixação (" + texto(inicio)
                    + ") não pode ser depois do fim (" + texto(fim) + ").");

        this.vigenteDesde = vigenteDesde;
        this.prazoPreferencia = preferencia;
        this.prazoMapa = mapa;
        this.fixacaoInicio = inicio;
        this.fixacaoFim = fim;
        this.periodoMinimoInterpolado = periodoMinimoInterpolado;
        this.fundamento = fundamento == null || fundamento.isBlank() ? null : fundamento.trim();
    }

    private static MonthDay dia(String campo, String mmdd) {
        if (mmdd == null || !MM_DD.matcher(mmdd.trim()).matches())
            throw invalido("'" + campo + "' é obrigatório e escreve-se MM-dd (ex.: 01-31).");
        MonthDay dia;
        try {
            dia = MonthDay.parse("--" + mmdd.trim());
        } catch (DateTimeException e) {
            throw invalido("'" + campo + "' não é um dia do ano: " + mmdd + ".");
        }
        // Um prazo a 29 de Fevereiro só existiria de quatro em quatro anos.
        if (dia.getMonthValue() == 2 && dia.getDayOfMonth() == 29)
            throw invalido("'" + campo + "' não pode ser 29 de Fevereiro: não há esse dia todos os anos.");
        return dia;
    }

    private static IgrpResponseStatusException invalido(String mensagem) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, mensagem);
    }
}
