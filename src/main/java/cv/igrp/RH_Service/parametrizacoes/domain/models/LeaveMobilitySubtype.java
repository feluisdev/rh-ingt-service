package cv.igrp.RH_Service.parametrizacoes.domain.models;

import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.LeaveMobilitySubtypeId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;

import java.util.Objects;
import java.util.Set;

@Getter
public class LeaveMobilitySubtype {

    private static final Set<String> VALID_RECORD_TYPES = Set.of("LICENCA", "MOBILIDADE", "AMBOS");

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

    private LeaveMobilitySubtype() {}

    private LeaveMobilitySubtype(LeaveMobilitySubtypeId id, String code, String description, String recordType,
                                  boolean affectsPay, boolean countsForSeniority,
                                  boolean canSelfSubmit, boolean active,
                                  Integer maxDurationDays, Integer maxExtensions) {
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
        Objects.requireNonNull(code, "code não pode ser nulo");
        if (recordType == null || !VALID_RECORD_TYPES.contains(recordType)) {
            throw IgrpResponseStatusException.badRequest(
                "recordType inválido: '" + recordType + "'. Valores aceites: " + VALID_RECORD_TYPES);
        }
        if (maxDurationDays != null && maxDurationDays <= 0)
            throw IgrpResponseStatusException.badRequest("maxDurationDays tem de ser positivo.");
        if (maxExtensions != null && maxExtensions < 0)
            throw IgrpResponseStatusException.badRequest("maxExtensions não pode ser negativo.");
        return new LeaveMobilitySubtype(LeaveMobilitySubtypeId.gerarNovo(), code, description, recordType,
                affectsPay, countsForSeniority, canSelfSubmit, true, maxDurationDays, maxExtensions);
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
        return new LeaveMobilitySubtype(id, code, description, recordType,
                affectsPay, countsForSeniority, canSelfSubmit, active, maxDurationDays, maxExtensions);
    }

    /** É um subtipo de mobilidade (a pessoa vai exercer funções noutro sítio)? */
    public boolean isMobilidade() {
        return "MOBILIDADE".equals(this.recordType) || "AMBOS".equals(this.recordType);
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

    public void desativar() {
        if (!this.active) throw IgrpResponseStatusException.conflict("Subtipo de licença/mobilidade já está inactivo.");
        this.active = false;
    }

    public void reativar() {
        if (this.active) throw IgrpResponseStatusException.conflict("Subtipo de licença/mobilidade já está activo.");
        this.active = true;
    }
}
