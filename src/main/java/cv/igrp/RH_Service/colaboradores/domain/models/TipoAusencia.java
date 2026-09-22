package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.TipoAusenciaId;
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
    private String categoryOptionCkey;
    private Boolean isActive;
    /**
     * O regime legal a que obedece (V49). O motivo e o nome sao da instituicao; o regime e da
     * lei, e e por aqui — nao pelo codigo — que se sabe quais destas linhas sao ferias.
     */
    private RegimeAusencia regime;

    private TipoAusencia() {}

    public static TipoAusencia criar(String nome, String codigo, Boolean deductsBalance,
                                     Boolean requiresApproval, Integer maxDaysPerYear,
                                     String categoryOptionCkey) {
        TipoAusencia t = new TipoAusencia();
        t.id = TipoAusenciaId.gerarNovo();
        t.nome = nome;
        t.codigo = codigo;
        t.deductsBalance = deductsBalance;
        t.requiresApproval = requiresApproval;
        t.maxDaysPerYear = maxDaysPerYear;
        t.categoryOptionCkey = categoryOptionCkey;
        t.isActive = true;
        return t;
    }

    public static TipoAusencia reconstituir(TipoAusenciaId id, String nome, String codigo,
                                            Boolean deductsBalance, Boolean requiresApproval,
                                            Integer maxDaysPerYear, String categoryOptionCkey,
                                            Boolean isActive, RegimeAusencia regime) {
        TipoAusencia t = new TipoAusencia();
        t.id = id;
        t.nome = nome;
        t.codigo = codigo;
        t.deductsBalance = deductsBalance;
        t.requiresApproval = requiresApproval;
        t.maxDaysPerYear = maxDaysPerYear;
        t.categoryOptionCkey = categoryOptionCkey;
        t.isActive = isActive;
        t.regime = regime;
        return t;
    }

    /** Ferias vencem-se; uma falta acontece. So o primeiro faz nascer saldo sozinho. */
    public boolean isFerias() { return regime == RegimeAusencia.FERIAS; }

    public void atualizar(String nome, String codigo, Boolean deductsBalance,
                          Boolean requiresApproval, Integer maxDaysPerYear,
                          String categoryOptionCkey) {
        this.nome = nome;
        this.codigo = codigo;
        this.deductsBalance = deductsBalance;
        this.requiresApproval = requiresApproval;
        this.maxDaysPerYear = maxDaysPerYear;
        this.categoryOptionCkey = categoryOptionCkey;
    }

    public void ativar() { this.isActive = true; }
    public void desativar() { this.isActive = false; }
}
