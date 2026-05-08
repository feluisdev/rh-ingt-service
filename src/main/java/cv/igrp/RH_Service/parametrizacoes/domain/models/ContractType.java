package cv.igrp.RH_Service.parametrizacoes.domain.models;

import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.ContractTypeId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

@Getter
public class ContractType {

    private ContractTypeId id;
    private String code;
    private String description;
    private UUID vinculoLaboralId;
    private boolean isRenewable;
    private Integer maxRenewals;
    private Integer maxDurationMonths;
    private boolean active;

    private ContractType() {}

    private ContractType(ContractTypeId id, String code, String description,
                         UUID vinculoLaboralId, boolean isRenewable,
                         Integer maxRenewals, Integer maxDurationMonths, boolean active) {
        this.id = id;
        this.code = code;
        this.description = description;
        this.vinculoLaboralId = vinculoLaboralId;
        this.isRenewable = isRenewable;
        this.maxRenewals = maxRenewals;
        this.maxDurationMonths = maxDurationMonths;
        this.active = active;
    }

    public static ContractType criar(String code, String description, UUID vinculoLaboralId,
                                     boolean isRenewable, Integer maxRenewals, Integer maxDurationMonths) {
        Objects.requireNonNull(code, "code não pode ser nulo");
        return new ContractType(ContractTypeId.gerarNovo(), code, description,
                vinculoLaboralId, isRenewable, maxRenewals, maxDurationMonths, true);
    }

    public static ContractType reconstruir(ContractTypeId id, String code, String description,
                                            UUID vinculoLaboralId, boolean isRenewable,
                                            Integer maxRenewals, Integer maxDurationMonths, boolean active) {
        return new ContractType(id, code, description, vinculoLaboralId,
                isRenewable, maxRenewals, maxDurationMonths, active);
    }

    public void atualizar(String description, UUID vinculoLaboralId,
                          boolean isRenewable, Integer maxRenewals, Integer maxDurationMonths) {
        this.description = description;
        this.vinculoLaboralId = vinculoLaboralId;
        this.isRenewable = isRenewable;
        this.maxRenewals = maxRenewals;
        this.maxDurationMonths = maxDurationMonths;
    }

    public void desativar() {
        if (!this.active) throw IgrpResponseStatusException.conflict("Já está inactivo.");
        this.active = false;
    }

    public void reativar() {
        if (this.active) throw IgrpResponseStatusException.conflict("Já está activo.");
        this.active = true;
    }
}
