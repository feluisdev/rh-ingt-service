package cv.igrp.RH_Service.colaboradores.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.MonthDay;

/**
 * Os valores que o DL n.º 3/2010 fixa para a marcação das férias, com a lei por omissão.
 *
 * <p>Em Cabo Verde nenhuma instituição os pode mudar — estão no articulado —, mas mudam com um
 * diploma novo ou noutro país. Vivem por isso em propriedade, e não em código: mudam-se sem
 * recompilar (propriedade ou variável de ambiente, e reiniciar). Uma tabela editável por
 * instituição seria dar a cada uma o que a lei não lhe dá.
 *
 * <p>As datas escrevem-se {@code MM-dd}.
 */
@Component
public class FeriasParametros {

    /** Art. 5.º n.º 4: o trabalhador indica a preferência até 31 de Janeiro. */
    private final MonthDay prazoPreferencia;
    /** Art. 6.º n.º 1: o serviço elabora o mapa e dá conhecimento até 31 de Março. */
    private final MonthDay prazoMapa;
    /** Art. 5.º n.º 5: sem acordo, o dirigente fixa entre 1 de Maio... */
    private final MonthDay fixacaoInicio;
    /** ...e 31 de Outubro. */
    private final MonthDay fixacaoFim;
    /** Art. 5.º n.º 1: em gozo interpolado, um dos períodos tem pelo menos 11 dias úteis. */
    private final int periodoMinimoInterpolado;

    public FeriasParametros(
            @Value("${rh.ferias.mapa.prazo-preferencia:01-31}") String prazoPreferencia,
            @Value("${rh.ferias.mapa.prazo-mapa:03-31}") String prazoMapa,
            @Value("${rh.ferias.mapa.fixacao-inicio:05-01}") String fixacaoInicio,
            @Value("${rh.ferias.mapa.fixacao-fim:10-31}") String fixacaoFim,
            @Value("${rh.ferias.mapa.periodo-minimo-interpolado:11}") int periodoMinimoInterpolado) {
        this.prazoPreferencia = dia(prazoPreferencia);
        this.prazoMapa = dia(prazoMapa);
        this.fixacaoInicio = dia(fixacaoInicio);
        this.fixacaoFim = dia(fixacaoFim);
        this.periodoMinimoInterpolado = periodoMinimoInterpolado;
    }

    /** Os valores da lei, sem propriedades — para os testes. */
    public static FeriasParametros daLei() {
        return new FeriasParametros("01-31", "03-31", "05-01", "10-31", 11);
    }

    public LocalDate prazoPreferencia(int ano) { return prazoPreferencia.atYear(ano); }
    public LocalDate prazoMapa(int ano) { return prazoMapa.atYear(ano); }
    public LocalDate fixacaoInicio(int ano) { return fixacaoInicio.atYear(ano); }
    public LocalDate fixacaoFim(int ano) { return fixacaoFim.atYear(ano); }
    public int periodoMinimoInterpolado() { return periodoMinimoInterpolado; }

    private static MonthDay dia(String mmdd) {
        return MonthDay.parse("--" + mmdd.trim());
    }
}
