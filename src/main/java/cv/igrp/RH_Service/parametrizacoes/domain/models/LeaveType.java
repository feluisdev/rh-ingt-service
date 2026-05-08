package cv.igrp.RH_Service.parametrizacoes.domain.models;

import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.LeaveTypeId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;

import java.util.Objects;

@Getter
public class LeaveType {

    private LeaveTypeId id;
    private String code;
    private String description;
    private boolean deductsBalance;
    private boolean requiresApproval;
    private Integer maxDaysPerYear;
    private String category;
    private boolean active;

    private LeaveType() {}

    private LeaveType(LeaveTypeId id, String code, String description, boolean deductsBalance,
                      boolean requiresApproval, Integer maxDaysPerYear, String category, boolean active) {
        this.id = id;
        this.code = code;
        this.description = description;
        this.deductsBalance = deductsBalance;
        this.requiresApproval = requiresApproval;
        this.maxDaysPerYear = maxDaysPerYear;
        this.category = category;
        this.active = active;
    }

    public static LeaveType criar(String code, String description, boolean deductsBalance,
                                  boolean requiresApproval, Integer maxDaysPerYear, String category) {
        Objects.requireNonNull(code, "code não pode ser nulo");
        if (maxDaysPerYear != null && maxDaysPerYear < 0) {
            throw IgrpResponseStatusException.badRequest("maxDaysPerYear não pode ser negativo.");
        }
        return new LeaveType(LeaveTypeId.gerarNovo(), code, description, deductsBalance,
                requiresApproval, maxDaysPerYear, category, true);
    }

    public static LeaveType reconstruir(LeaveTypeId id, String code, String description, boolean deductsBalance,
                                        boolean requiresApproval, Integer maxDaysPerYear,
                                        String category, boolean active) {
        return new LeaveType(id, code, description, deductsBalance, requiresApproval,
                maxDaysPerYear, category, active);
    }

    public void atualizar(String description, boolean deductsBalance, boolean requiresApproval,
                          Integer maxDaysPerYear, String category) {
        this.description = description;
        this.deductsBalance = deductsBalance;
        this.requiresApproval = requiresApproval;
        this.maxDaysPerYear = maxDaysPerYear;
        this.category = category;
    }

    public void desativar() {
        if (!this.active) throw IgrpResponseStatusException.conflict("Tipo de licença já está inactivo.");
        this.active = false;
    }

    public void reativar() {
        if (this.active) throw IgrpResponseStatusException.conflict("Tipo de licença já está activo.");
        this.active = true;
    }
}
