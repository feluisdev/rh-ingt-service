package cv.igrp.RH_Service.shared.application.services.scheduler;

import lombok.Getter;

/**
 * Descrição de um parâmetro que um {@link ScheduledJob} aceita no disparo manual.
 *
 * <p>É o que torna o framework transversal sem trabalho de frontend: o job declara os parâmetros
 * que aceita, a API expõe essa descrição em {@code GET /schedulers/{chave}}, e a interface gera o
 * formulário a partir dela. Um job novo com parâmetros novos não obriga a tocar no frontend.
 *
 * <pre>{@code
 * @Override
 * public List<JobParametro> getParametros() {
 *     return List.of(JobParametro.dataReferencia()
 *             .ajuda("Se vazio, usa o dia do agendamento."));
 * }
 * }</pre>
 */
@Getter
public class JobParametro {

    private final String nome;
    private final String rotulo;
    private final TipoParametroJob tipo;
    private boolean obrigatorio;
    private String ajuda;
    private String exemplo;

    private JobParametro(String nome, String rotulo, TipoParametroJob tipo) {
        this.nome = nome;
        this.rotulo = rotulo;
        this.tipo = tipo;
    }

    public static JobParametro de(String nome, String rotulo, TipoParametroJob tipo) {
        return new JobParametro(nome, rotulo, tipo);
    }

    /**
     * A data a que a execução diz respeito ({@value JobContext#PARAM_DATA}) — o parâmetro que o
     * {@link JobContext#dataReferencia()} lê e que o sweeper preenche nas execuções em falta.
     */
    public static JobParametro dataReferencia() {
        return new JobParametro(JobContext.PARAM_DATA, "Data de referência", TipoParametroJob.DATA)
                .exemplo("2026-01-01");
    }

    public static JobParametro ano(String nome, String rotulo) {
        return new JobParametro(nome, rotulo, TipoParametroJob.ANO).exemplo("2026");
    }

    public static JobParametro texto(String nome, String rotulo) {
        return new JobParametro(nome, rotulo, TipoParametroJob.TEXTO);
    }

    public static JobParametro inteiro(String nome, String rotulo) {
        return new JobParametro(nome, rotulo, TipoParametroJob.INTEIRO);
    }

    public static JobParametro booleano(String nome, String rotulo) {
        return new JobParametro(nome, rotulo, TipoParametroJob.BOOLEANO);
    }

    public JobParametro obrigatorio() {
        this.obrigatorio = true;
        return this;
    }

    public JobParametro ajuda(String ajuda) {
        this.ajuda = ajuda;
        return this;
    }

    public JobParametro exemplo(String exemplo) {
        this.exemplo = exemplo;
        return this;
    }
}
