package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.SubtipoLicencaMobilidadeId;
import lombok.Getter;

@Getter
public class SubtipoLicencaMobilidade {

    private SubtipoLicencaMobilidadeId id;
    private String nome;
    private String codigo;
    private String recordType;
    private Boolean affectsPay;
    private Boolean countsForSeniority;
    private Boolean canSelfSubmit;
    private Boolean isActive;
    /** Duração máxima em dias (mobilidade transitória: 365). Nulo = sem limite. */
    private Integer maxDurationDays;
    /** Prorrogações permitidas (em regra, uma). Nulo = sem limite. */
    private Integer maxExtensions;

    private SubtipoLicencaMobilidade() {}

    public static SubtipoLicencaMobilidade criar(String nome, String codigo, String recordType,
                                                  Boolean affectsPay, Boolean countsForSeniority,
                                                  Boolean canSelfSubmit) {
        SubtipoLicencaMobilidade s = new SubtipoLicencaMobilidade();
        s.id = SubtipoLicencaMobilidadeId.gerarNovo();
        s.nome = nome;
        s.codigo = codigo;
        s.recordType = recordType;
        s.affectsPay = affectsPay;
        s.countsForSeniority = countsForSeniority;
        s.canSelfSubmit = canSelfSubmit;
        s.isActive = true;
        return s;
    }

    public static SubtipoLicencaMobilidade reconstituir(SubtipoLicencaMobilidadeId id, String nome, String codigo,
                                                         String recordType, Boolean affectsPay,
                                                         Boolean countsForSeniority, Boolean canSelfSubmit,
                                                         Boolean isActive) {
        return reconstituir(id, nome, codigo, recordType, affectsPay, countsForSeniority,
                canSelfSubmit, isActive, null, null);
    }

    public static SubtipoLicencaMobilidade reconstituir(SubtipoLicencaMobilidadeId id, String nome, String codigo,
                                                         String recordType, Boolean affectsPay,
                                                         Boolean countsForSeniority, Boolean canSelfSubmit,
                                                         Boolean isActive, Integer maxDurationDays,
                                                         Integer maxExtensions) {
        SubtipoLicencaMobilidade s = new SubtipoLicencaMobilidade();
        s.id = id;
        s.nome = nome;
        s.codigo = codigo;
        s.recordType = recordType;
        s.affectsPay = affectsPay;
        s.countsForSeniority = countsForSeniority;
        s.canSelfSubmit = canSelfSubmit;
        s.isActive = isActive;
        s.maxDurationDays = maxDurationDays;
        s.maxExtensions = maxExtensions;
        return s;
    }

    /**
     * É mobilidade (a pessoa passa a exercer funções noutro sítio)? {@code AMBOS} conta como
     * mobilidade — o código antigo comparava só com "MOBILIDADE" e ignorava-o em silêncio.
     */
    public boolean isMobilidade() {
        return "MOBILIDADE".equals(this.recordType) || "AMBOS".equals(this.recordType);
    }

    public void atualizar(String nome, String codigo, String recordType, Boolean affectsPay,
                          Boolean countsForSeniority, Boolean canSelfSubmit) {
        this.nome = nome;
        this.codigo = codigo;
        this.recordType = recordType;
        this.affectsPay = affectsPay;
        this.countsForSeniority = countsForSeniority;
        this.canSelfSubmit = canSelfSubmit;
    }

    public void ativar() { this.isActive = true; }
    public void desativar() { this.isActive = false; }
}
