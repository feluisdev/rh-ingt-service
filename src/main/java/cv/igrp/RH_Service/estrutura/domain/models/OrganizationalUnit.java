package cv.igrp.RH_Service.estrutura.domain.models;

import cv.igrp.RH_Service.estrutura.domain.valueobject.OrganizationalUnitId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;

import java.util.UUID;

@Getter
public class OrganizationalUnit {

    private OrganizationalUnitId id;
    private String code;
    private String name;
    private String acronym;
    private String unitType;
    private String descricao;
    private OrganizationalUnitId parentUnitId;
    // UUID nu e nao um value object tipado (D-09, 109-02-PLAN.md): o alvo e um
    // funcionario de outro modulo, e um identificador tipado importado desse modulo
    // levaria o alcance cross-modulo para dentro do dominio de estrutura.
    private UUID responsibleEmployeeId;
    private boolean active;

    private OrganizationalUnit() {}

    private OrganizationalUnit(OrganizationalUnitId id, String code, String name, String acronym,
                                String unitType, String descricao,
                                OrganizationalUnitId parentUnitId, UUID responsibleEmployeeId, boolean active) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.acronym = acronym;
        this.unitType = unitType;
        this.descricao = descricao;
        this.parentUnitId = parentUnitId;
        this.responsibleEmployeeId = responsibleEmployeeId;
        this.active = active;
    }

    public static OrganizationalUnit criar(String code, String name, String acronym,
                                            String unitType, String descricao,
                                            OrganizationalUnitId parentUnitId, UUID responsibleEmployeeId) {
        return new OrganizationalUnit(OrganizationalUnitId.gerarNovo(), code, name, acronym,
                unitType, descricao, parentUnitId, responsibleEmployeeId, true);
    }

    public static OrganizationalUnit reconstruir(OrganizationalUnitId id, String code, String name, String acronym,
                                                  String unitType, String descricao,
                                                  OrganizationalUnitId parentUnitId, UUID responsibleEmployeeId,
                                                  boolean active) {
        return new OrganizationalUnit(id, code, name, acronym, unitType, descricao, parentUnitId,
                responsibleEmployeeId, active);
    }

    public void atualizar(String code, String name, String acronym, String unitType,
                          String descricao, OrganizationalUnitId parentUnitId, UUID responsibleEmployeeId) {
        this.code = code;
        this.name = name;
        this.acronym = acronym;
        this.unitType = unitType;
        this.descricao = descricao;
        this.parentUnitId = parentUnitId;
        this.responsibleEmployeeId = responsibleEmployeeId;
    }

    public void desativar() {
        if (!this.active) {
            throw IgrpResponseStatusException.conflict("A unidade orgânica já está inactiva.");
        }
        this.active = false;
    }

    public void reativar() {
        if (this.active) {
            throw IgrpResponseStatusException.conflict("A unidade orgânica já está activa.");
        }
        this.active = true;
    }
}
