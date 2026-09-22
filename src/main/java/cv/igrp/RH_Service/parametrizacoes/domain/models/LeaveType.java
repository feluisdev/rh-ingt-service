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
    /**
     * Regime legal do DL n.o 3/2010 (V49). O motivo e o nome sao da instituicao; o regime e da
     * lei, e e por ele -- nunca pelo codigo -- que se sabe quais destas linhas sao ferias.
     * Por omissao FALTA, que e o regime que nao produz efeitos automaticos.
     */
    private RegimeAusencia regime;

    private LeaveType() {}

    private LeaveType(LeaveTypeId id, String code, String description, boolean deductsBalance,
                      boolean requiresApproval, Integer maxDaysPerYear, String category, boolean active,
                      RegimeAusencia regime) {
        this.id = id;
        this.code = code;
        this.description = description;
        this.deductsBalance = deductsBalance;
        this.requiresApproval = requiresApproval;
        this.maxDaysPerYear = maxDaysPerYear;
        this.category = category;
        this.active = active;
        this.regime = regime != null ? regime : RegimeAusencia.FALTA;
    }

    public static LeaveType criar(String code, String description, boolean deductsBalance,
                                  boolean requiresApproval, Integer maxDaysPerYear, String category,
                                  RegimeAusencia regime) {
        Objects.requireNonNull(code, "code não pode ser nulo");
        if (maxDaysPerYear != null && maxDaysPerYear < 0) {
            throw IgrpResponseStatusException.badRequest("maxDaysPerYear não pode ser negativo.");
        }
        return new LeaveType(LeaveTypeId.gerarNovo(), code, description, deductsBalance,
                requiresApproval, maxDaysPerYear, category, true, regime);
    }

    public static LeaveType reconstruir(LeaveTypeId id, String code, String description, boolean deductsBalance,
                                        boolean requiresApproval, Integer maxDaysPerYear,
                                        String category, boolean active, RegimeAusencia regime) {
        return new LeaveType(id, code, description, deductsBalance, requiresApproval,
                maxDaysPerYear, category, active, regime);
    }

    public void atualizar(String description, boolean deductsBalance, boolean requiresApproval,
                          Integer maxDaysPerYear, String category, RegimeAusencia regime) {
        this.description = description;
        this.deductsBalance = deductsBalance;
        this.requiresApproval = requiresApproval;
        this.maxDaysPerYear = maxDaysPerYear;
        this.category = category;
        // Nao se apaga uma classificacao por o pedido vir sem ela: a omissao mantem a que esta.
        if (regime != null) this.regime = regime;
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
