package cv.igrp.RH_Service.parametrizacoes.domain.models;

import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.LeaveMobilitySubtypeId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;

import java.util.Objects;
import java.util.Set;

@Getter
public class LeaveMobilitySubtype {

    private LeaveMobilitySubtypeId id;
    private String code;
    private String description;
    private String recordType;
    private boolean affectsPay;
    private boolean countsForSeniority;
    private boolean canSelfSubmit;
    private boolean active;
    /** Duração máxima, em dias (mobilidade transitória: 365 — art. 132.º n.º 5). Nulo = sem limite. */
    private Integer maxDurationDays;
    /** Prorrogações permitidas (em regra, uma). Nulo = sem limite. */
    private Integer maxExtensions;
    /** O que a licença faz ao Lugar enquanto dura (V43). */
    private EfeitoNoLugar positionEffect;
    /** Abre vaga só além deste número de dias; nulo = abre logo (V43). */
    private Integer vacancyAfterDays;
    /** O que acontece ao funcionário no regresso (V43). */
    private EfeitoNoRegresso returnEffect;

    private LeaveMobilitySubtype() {}

    private LeaveMobilitySubtype(LeaveMobilitySubtypeId id, String code, String description, String recordType,
                                  boolean affectsPay, boolean countsForSeniority,
                                  boolean canSelfSubmit, boolean active,
                                  Integer maxDurationDays, Integer maxExtensions,
                                  EfeitoNoLugar positionEffect, Integer vacancyAfterDays,
                                  EfeitoNoRegresso returnEffect) {
        this.positionEffect = positionEffect;
        this.vacancyAfterDays = vacancyAfterDays;
        this.returnEffect = returnEffect;
        this.id = id;
        this.code = code;
        this.description = description;
        this.recordType = recordType;
        this.affectsPay = affectsPay;
        this.countsForSeniority = countsForSeniority;
        this.canSelfSubmit = canSelfSubmit;
        this.active = active;
        this.maxDurationDays = maxDurationDays;
        this.maxExtensions = maxExtensions;
    }

    public static LeaveMobilitySubtype criar(String code, String description, String recordType,
                                              boolean affectsPay, boolean countsForSeniority,
                                              boolean canSelfSubmit) {
        return criar(code, description, recordType, affectsPay, countsForSeniority, canSelfSubmit, null, null);
    }

    public static LeaveMobilitySubtype criar(String code, String description, String recordType,
                                              boolean affectsPay, boolean countsForSeniority,
                                              boolean canSelfSubmit, Integer maxDurationDays,
                                              Integer maxExtensions) {
        return criar(code, description, recordType, affectsPay, countsForSeniority, canSelfSubmit,
                maxDurationDays, maxExtensions, null, null, null);
    }

    public static LeaveMobilitySubtype criar(String code, String description, String recordType,
                                              boolean affectsPay, boolean countsForSeniority,
                                              boolean canSelfSubmit, Integer maxDurationDays,
                                              Integer maxExtensions, String positionEffect,
                                              Integer vacancyAfterDays, String returnEffect) {
        Objects.requireNonNull(code, "code não pode ser nulo");
        TipoRegisto tipo = TipoRegisto.de(recordType);
        if (tipo == null)
            throw IgrpResponseStatusException.badRequest(
                "recordType é obrigatório. Valores aceites: LICENCA, MOBILIDADE.");
        if (maxDurationDays != null && maxDurationDays <= 0)
            throw IgrpResponseStatusException.badRequest("maxDurationDays tem de ser positivo.");
        if (maxExtensions != null && maxExtensions < 0)
            throw IgrpResponseStatusException.badRequest("maxExtensions não pode ser negativo.");
        if (vacancyAfterDays != null && vacancyAfterDays < 0)
            throw IgrpResponseStatusException.badRequest("vacancyAfterDays não pode ser negativo.");

        EfeitoNoLugar efeitoLugar = EfeitoNoLugar.de(positionEffect);
        EfeitoNoRegresso efeitoRegresso = EfeitoNoRegresso.de(returnEffect);

        // A mobilidade nunca liberta o Lugar de origem (art. 135.º n.º 7).
        if (tipo == TipoRegisto.MOBILIDADE && efeitoLugar == EfeitoNoLugar.ABRE_VAGA)
            throw IgrpResponseStatusException.badRequest(
                "A mobilidade não abre vaga: o funcionário mantém o Lugar de origem (art. 135.º n.º 7).");

        return new LeaveMobilitySubtype(LeaveMobilitySubtypeId.gerarNovo(), code, description, tipo.name(),
                affectsPay, countsForSeniority, canSelfSubmit, true, maxDurationDays, maxExtensions,
                efeitoLugar, vacancyAfterDays, efeitoRegresso);
    }

    public static LeaveMobilitySubtype reconstruir(LeaveMobilitySubtypeId id, String code, String description,
                                                    String recordType, boolean affectsPay,
                                                    boolean countsForSeniority, boolean canSelfSubmit,
                                                    boolean active) {
        return reconstruir(id, code, description, recordType, affectsPay, countsForSeniority,
                canSelfSubmit, active, null, null);
    }

    public static LeaveMobilitySubtype reconstruir(LeaveMobilitySubtypeId id, String code, String description,
                                                    String recordType, boolean affectsPay,
                                                    boolean countsForSeniority, boolean canSelfSubmit,
                                                    boolean active, Integer maxDurationDays,
                                                    Integer maxExtensions) {
        return reconstruir(id, code, description, recordType, affectsPay, countsForSeniority,
                canSelfSubmit, active, maxDurationDays, maxExtensions, null, null, null);
    }

    public static LeaveMobilitySubtype reconstruir(LeaveMobilitySubtypeId id, String code, String description,
                                                    String recordType, boolean affectsPay,
                                                    boolean countsForSeniority, boolean canSelfSubmit,
                                                    boolean active, Integer maxDurationDays,
                                                    Integer maxExtensions, String positionEffect,
                                                    Integer vacancyAfterDays, String returnEffect) {
        return new LeaveMobilitySubtype(id, code, description, recordType,
                affectsPay, countsForSeniority, canSelfSubmit, active, maxDurationDays, maxExtensions,
                EfeitoNoLugar.de(positionEffect), vacancyAfterDays, EfeitoNoRegresso.de(returnEffect));
    }

    /** É um subtipo de mobilidade (a pessoa vai exercer funções noutro sítio)? */
    public boolean isMobilidade() {
        return TipoRegisto.de(this.recordType) == TipoRegisto.MOBILIDADE;
    }

    public EfeitoNoLugar efeitoNoLugar() {
        return positionEffect != null ? positionEffect : EfeitoNoLugar.MANTEM;
    }

    public EfeitoNoRegresso efeitoNoRegresso() {
        return returnEffect != null ? returnEffect : EfeitoNoRegresso.REGRESSA_LUGAR;
    }

    public void atualizar(String description, boolean affectsPay,
                          boolean countsForSeniority, boolean canSelfSubmit) {
        this.description = description;
        this.affectsPay = affectsPay;
        this.countsForSeniority = countsForSeniority;
        this.canSelfSubmit = canSelfSubmit;
    }

    public void atualizar(String description, boolean affectsPay, boolean countsForSeniority,
                          boolean canSelfSubmit, Integer maxDurationDays, Integer maxExtensions) {
        atualizar(description, affectsPay, countsForSeniority, canSelfSubmit);
        this.maxDurationDays = maxDurationDays;
        this.maxExtensions = maxExtensions;
    }

    public void atualizar(String description, boolean affectsPay, boolean countsForSeniority,
                          boolean canSelfSubmit, Integer maxDurationDays, Integer maxExtensions,
                          String positionEffect, Integer vacancyAfterDays, String returnEffect) {
        atualizar(description, affectsPay, countsForSeniority, canSelfSubmit, maxDurationDays, maxExtensions);

        EfeitoNoLugar efeitoLugar = positionEffect == null ? efeitoNoLugar() : EfeitoNoLugar.de(positionEffect);
        if (isMobilidade() && efeitoLugar == EfeitoNoLugar.ABRE_VAGA)
            throw IgrpResponseStatusException.badRequest(
                "A mobilidade não abre vaga: o funcionário mantém o Lugar de origem (art. 135.º n.º 7).");

        this.positionEffect = efeitoLugar;
        this.vacancyAfterDays = vacancyAfterDays;
        if (returnEffect != null) this.returnEffect = EfeitoNoRegresso.de(returnEffect);
    }

    public void desativar() {
        if (!this.active) throw IgrpResponseStatusException.conflict("Subtipo de licença/mobilidade já está inactivo.");
        this.active = false;
    }

    public void reativar() {
        if (this.active) throw IgrpResponseStatusException.conflict("Subtipo de licença/mobilidade já está activo.");
        this.active = true;
    }
}
