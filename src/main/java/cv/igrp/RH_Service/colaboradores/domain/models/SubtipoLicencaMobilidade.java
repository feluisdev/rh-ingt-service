package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.SubtipoLicencaMobilidadeId;
import cv.igrp.RH_Service.parametrizacoes.domain.models.EfeitoNoLugar;
import cv.igrp.RH_Service.parametrizacoes.domain.models.EfeitoNoRegresso;
import cv.igrp.RH_Service.parametrizacoes.domain.models.TipoRegisto;
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
    /** O que faz ao Lugar enquanto dura (V43). */
    private EfeitoNoLugar positionEffect;
    /** Abre vaga só além deste número de dias; nulo = abre logo (V43). */
    private Integer vacancyAfterDays;
    /** O que acontece ao funcionário no regresso (V43). */
    private EfeitoNoRegresso returnEffect;

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
        return reconstituir(id, nome, codigo, recordType, affectsPay, countsForSeniority, canSelfSubmit,
                isActive, maxDurationDays, maxExtensions, null, null, null);
    }

    public static SubtipoLicencaMobilidade reconstituir(SubtipoLicencaMobilidadeId id, String nome, String codigo,
                                                         String recordType, Boolean affectsPay,
                                                         Boolean countsForSeniority, Boolean canSelfSubmit,
                                                         Boolean isActive, Integer maxDurationDays,
                                                         Integer maxExtensions, String positionEffect,
                                                         Integer vacancyAfterDays, String returnEffect) {
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
        s.positionEffect = EfeitoNoLugar.de(positionEffect);
        s.vacancyAfterDays = vacancyAfterDays;
        s.returnEffect = EfeitoNoRegresso.de(returnEffect);
        return s;
    }

    /** É mobilidade (a pessoa passa a exercer funções noutro sítio)? */
    public boolean isMobilidade() {
        return TipoRegisto.de(this.recordType) == TipoRegisto.MOBILIDADE;
    }

    /**
     * Esta licença, com esta duração, liberta o Lugar? O prazo a partir do qual abre
     * vaga está no catálogo, porque a lei fá-lo variar com o motivo: um ano para o
     * cônjuge no estrangeiro, seis meses para a formação (DL n.º 3/2010, art. 56.º
     * n.º 2 e 67.º n.º 3; Lei n.º 20/X/2023, art. 118.º n.º 2).
     */
    public boolean abreVaga(Long duracaoEmDias) {
        return efeitoNoLugar().abreVaga(duracaoEmDias, this.vacancyAfterDays);
    }

    /** No regresso, o funcionário fica a aguardar vaga em vez de voltar ao seu Lugar? */
    public boolean regressaEmDisponibilidade() {
        return efeitoNoRegresso() == EfeitoNoRegresso.DISPONIBILIDADE;
    }

    public EfeitoNoLugar efeitoNoLugar() {
        return positionEffect != null ? positionEffect : EfeitoNoLugar.MANTEM;
    }

    public EfeitoNoRegresso efeitoNoRegresso() {
        return returnEffect != null ? returnEffect : EfeitoNoRegresso.REGRESSA_LUGAR;
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
