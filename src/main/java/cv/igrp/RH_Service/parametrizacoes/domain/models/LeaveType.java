package cv.igrp.RH_Service.parametrizacoes.domain.models;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

@Getter
public class LeaveType {

    private ExternalID id;
    private String code;
    private String description;
    private boolean deductsBalance;
    private boolean requiresApproval;
    private Integer maxDaysPerYear;
    private UUID categoryOptionId;
    private boolean active;

    private LeaveType() {}

    private LeaveType(ExternalID id, String code, String description, boolean deductsBalance,
                      boolean requiresApproval, Integer maxDaysPerYear, UUID categoryOptionId, boolean active) {
        this.id = id;
        this.code = code;
        this.description = description;
        this.deductsBalance = deductsBalance;
        this.requiresApproval = requiresApproval;
        this.maxDaysPerYear = maxDaysPerYear;
        this.categoryOptionId = categoryOptionId;
        this.active = active;
    }

    public static LeaveType criar(String code, String description, boolean deductsBalance,
                                  boolean requiresApproval, Integer maxDaysPerYear, UUID categoryOptionId) {
        Objects.requireNonNull(code, "code não pode ser nulo");
        if (maxDaysPerYear != null && maxDaysPerYear < 0) {
            throw IgrpResponseStatusException.badRequest("maxDaysPerYear não pode ser negativo.");
        }
        return new LeaveType(ExternalID.gerarNovo(), code, description, deductsBalance,
                requiresApproval, maxDaysPerYear, categoryOptionId, true);
    }

    public static LeaveType reconstruir(ExternalID id, String code, String description, boolean deductsBalance,
                                        boolean requiresApproval, Integer maxDaysPerYear,
                                        UUID categoryOptionId, boolean active) {
        return new LeaveType(id, code, description, deductsBalance, requiresApproval,
                maxDaysPerYear, categoryOptionId, active);
    }

    public void atualizar(String description, boolean deductsBalance, boolean requiresApproval,
                          Integer maxDaysPerYear, UUID categoryOptionId) {
        this.description = description;
        this.deductsBalance = deductsBalance;
        this.requiresApproval = requiresApproval;
        this.maxDaysPerYear = maxDaysPerYear;
        this.categoryOptionId = categoryOptionId;
    }

    public void desativar() {
        if (!this.active) {
            throw IgrpResponseStatusException.conflict("Tipo de licença já está inactivo.");
        }
        this.active = false;
    }

    public void reativar() {
        if (this.active) {
            throw IgrpResponseStatusException.conflict("Tipo de licença já está activo.");
        }
        this.active = true;
    }
}
