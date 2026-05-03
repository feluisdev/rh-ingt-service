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
        SubtipoLicencaMobilidade s = new SubtipoLicencaMobilidade();
        s.id = id;
        s.nome = nome;
        s.codigo = codigo;
        s.recordType = recordType;
        s.affectsPay = affectsPay;
        s.countsForSeniority = countsForSeniority;
        s.canSelfSubmit = canSelfSubmit;
        s.isActive = isActive;
        return s;
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
