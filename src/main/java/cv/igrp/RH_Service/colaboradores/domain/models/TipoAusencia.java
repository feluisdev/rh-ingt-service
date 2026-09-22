package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.TipoAusenciaId;
import cv.igrp.RH_Service.parametrizacoes.domain.models.EfeitoNaRemuneracao;
import cv.igrp.RH_Service.parametrizacoes.domain.models.RegimeAusencia;
import lombok.Getter;

@Getter
public class TipoAusencia {

    private TipoAusenciaId id;
    private String nome;
    private String codigo;
    private Boolean deductsBalance;
    private Boolean requiresApproval;
    private Integer maxDaysPerYear;
    /**
     * Limite por ACONTECIMENTO (V53). O art. 15.º n.º 1 do DL n.º 3/2010 quase só fala assim —
     * 6 dias por ocasião do casamento, 8 por falecimento do cônjuge, 2 por cada prova —, e antes
     * disto não havia onde o guardar: escrito no limite anual, recusava o segundo luto do ano e
     * deixava passar oito dias seguidos de uma só vez.
     */
    private Integer maxDaysPerOccurrence;
    /** Limite por mês civil (V53): art. 15.º n.º 1 al. o) e al. q). */
    private Integer maxDaysPerMonth;
    private String categoryOptionCkey;
    private Boolean isActive;
    /**
     * O regime legal a que obedece (V49). O motivo e o nome sao da instituicao; o regime e da
     * lei, e e por aqui — nao pelo codigo — que se sabe quais destas linhas sao ferias.
     */
    private RegimeAusencia regime;
    /**
     * O que a ausência faz à remuneração (art. 16.º; V54). Não se calcula nada com isto — é
     * informação para quem processa vencimentos.
     */
    private EfeitoNaRemuneracao efeitoRemuneracao;

    private TipoAusencia() {}

    public static TipoAusencia criar(String nome, String codigo, Boolean deductsBalance,
                                     Boolean requiresApproval, Integer maxDaysPerYear,
                                     Integer maxDaysPerOccurrence, Integer maxDaysPerMonth,
                                     String categoryOptionCkey) {
        // O regime e o efeito na remuneração são classificação da instituição: entram pelo
        // catálogo (LeaveType), não por aqui.
        TipoAusencia t = new TipoAusencia();
        t.id = TipoAusenciaId.gerarNovo();
        t.nome = nome;
        t.codigo = codigo;
        t.deductsBalance = deductsBalance;
        t.requiresApproval = requiresApproval;
        t.maxDaysPerYear = maxDaysPerYear;
        t.maxDaysPerOccurrence = maxDaysPerOccurrence;
        t.maxDaysPerMonth = maxDaysPerMonth;
        t.categoryOptionCkey = categoryOptionCkey;
        t.isActive = true;
        return t;
    }

    public static TipoAusencia reconstituir(TipoAusenciaId id, String nome, String codigo,
                                            Boolean deductsBalance, Boolean requiresApproval,
                                            Integer maxDaysPerYear, Integer maxDaysPerOccurrence,
                                            Integer maxDaysPerMonth, String categoryOptionCkey,
                                            Boolean isActive, RegimeAusencia regime,
                                            EfeitoNaRemuneracao efeitoRemuneracao) {
        TipoAusencia t = new TipoAusencia();
        t.id = id;
        t.nome = nome;
        t.codigo = codigo;
        t.deductsBalance = deductsBalance;
        t.requiresApproval = requiresApproval;
        t.maxDaysPerYear = maxDaysPerYear;
        t.maxDaysPerOccurrence = maxDaysPerOccurrence;
        t.maxDaysPerMonth = maxDaysPerMonth;
        t.categoryOptionCkey = categoryOptionCkey;
        t.isActive = isActive;
        t.regime = regime;
        t.efeitoRemuneracao = efeitoRemuneracao;
        return t;
    }

    /** Ferias vencem-se; uma falta acontece. So o primeiro faz nascer saldo sozinho. */
    public boolean isFerias() { return regime == RegimeAusencia.FERIAS; }

    /**
     * Art. 43.º: falta injustificada. Quais o são di-lo a instituição pelo regime — nunca o
     * código —, mas o que daí decorre é da lei e não se configura.
     */
    public boolean isFaltaInjustificada() { return regime == RegimeAusencia.FALTA_INJUSTIFICADA; }

    /**
     * Art. 43.º n.º 2: as injustificadas não contam para antiguidade. Um tipo sem regime
     * classificado <b>conta</b> — descontar por omissão tiraria tempo a quem o tem, que é a
     * mesma regra que vale para os estados sem situação funcional classificada.
     */
    public boolean contaParaAntiguidade() { return regime == null || regime.contaAntiguidade(); }

    /**
     * Excede o que a lei permite <b>de uma vez</b>? Cada pedido é um acontecimento — um
     * casamento, um funeral, uma prova —, e o limite da alínea aplica-se a ele, não ao ano.
     * Quem perde dois familiares no mesmo ano tem direito às duas ausências.
     *
     * <p>Nulo é «a lei não põe limite desta natureza», que é o caso da maior parte das alíneas
     * do art. 15.º n.º 1: greve, obrigações legais, prisão preventiva, calamidade pública.
     */
    public boolean excedeLimitePorOcorrencia(int diasPedidos) {
        return maxDaysPerOccurrence != null && diasPedidos > maxDaysPerOccurrence;
    }

    /** Art. 15.º n.º 1 al. o) e al. q): o que se conta é o mês civil, não o ano. */
    public boolean excedeLimiteMensal(int diasJaUsadosNoMes, int diasPedidos) {
        return maxDaysPerMonth != null && diasJaUsadosNoMes + diasPedidos > maxDaysPerMonth;
    }

    /** Art. 15.º n.º 1 al. j) e al. q): o tecto do ano civil, quando a alínea tem um. */
    public boolean excedeLimiteAnual(int diasJaUsadosNoAno, int diasPedidos) {
        return maxDaysPerYear != null && diasJaUsadosNoAno + diasPedidos > maxDaysPerYear;
    }

    public void atualizar(String nome, String codigo, Boolean deductsBalance,
                          Boolean requiresApproval, Integer maxDaysPerYear,
                          Integer maxDaysPerOccurrence, Integer maxDaysPerMonth,
                          String categoryOptionCkey) {
        this.nome = nome;
        this.codigo = codigo;
        this.deductsBalance = deductsBalance;
        this.requiresApproval = requiresApproval;
        this.maxDaysPerYear = maxDaysPerYear;
        this.maxDaysPerOccurrence = maxDaysPerOccurrence;
        this.maxDaysPerMonth = maxDaysPerMonth;
        this.categoryOptionCkey = categoryOptionCkey;
    }

    public void ativar() { this.isActive = true; }
    public void desativar() { this.isActive = false; }
}
