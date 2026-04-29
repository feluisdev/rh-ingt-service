package cv.igrp.RH_Service.parametrizacoes.domain.models;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import lombok.Getter;

import java.util.Objects;
import java.util.Set;

@Getter
public class LeaveMobilitySubtype {

    private static final Set<String> VALID_RECORD_TYPES = Set.of("LICENCA", "MOBILIDADE", "AMBOS");

    private ExternalID id;
    private String code;
    private String description;
    private String recordType;
    private boolean affectsPay;
    private boolean countsForSeniority;
    private boolean canSelfSubmit;
    private boolean active;

    private LeaveMobilitySubtype() {}

    private LeaveMobilitySubtype(ExternalID id, String code, String description, String recordType,
                                  boolean affectsPay, boolean countsForSeniority,
                                  boolean canSelfSubmit, boolean active) {
        this.id = id;
        this.code = code;
        this.description = description;
        this.recordType = recordType;
        this.affectsPay = affectsPay;
        this.countsForSeniority = countsForSeniority;
        this.canSelfSubmit = canSelfSubmit;
        this.active = active;
    }

    public static LeaveMobilitySubtype criar(String code, String description, String recordType,
                                              boolean affectsPay, boolean countsForSeniority,
                                              boolean canSelfSubmit) {
        Objects.requireNonNull(code, "code não pode ser nulo");
        if (recordType == null || !VALID_RECORD_TYPES.contains(recordType)) {
            throw IgrpResponseStatusException.badRequest(
                "recordType inválido: '" + recordType + "'. Valores aceites: " + VALID_RECORD_TYPES);
        }
        return new LeaveMobilitySubtype(ExternalID.gerarNovo(), code, description, recordType,
                affectsPay, countsForSeniority, canSelfSubmit, true);
    }

    public static LeaveMobilitySubtype reconstruir(ExternalID id, String code, String description,
                                                    String recordType, boolean affectsPay,
                                                    boolean countsForSeniority, boolean canSelfSubmit,
                                                    boolean active) {
        return new LeaveMobilitySubtype(id, code, description, recordType,
                affectsPay, countsForSeniority, canSelfSubmit, active);
    }

    public void atualizar(String description, boolean affectsPay,
                          boolean countsForSeniority, boolean canSelfSubmit) {
        this.description = description;
        this.affectsPay = affectsPay;
        this.countsForSeniority = countsForSeniority;
        this.canSelfSubmit = canSelfSubmit;
    }

    public void desativar() {
        if (!this.active) {
            throw IgrpResponseStatusException.conflict("Subtipo de licença/mobilidade já está inactivo.");
        }
        this.active = false;
    }

    public void reativar() {
        if (this.active) {
            throw IgrpResponseStatusException.conflict("Subtipo de licença/mobilidade já está activo.");
        }
        this.active = true;
    }
}
