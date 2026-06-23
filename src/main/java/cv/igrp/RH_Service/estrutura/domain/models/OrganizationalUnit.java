package cv.igrp.RH_Service.estrutura.domain.models;

import cv.igrp.RH_Service.estrutura.domain.valueobject.OrganizationalUnitId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;

@Getter
public class OrganizationalUnit {

    private OrganizationalUnitId id;
    private String code;
    private String name;
    private String acronym;
    private String unitType;
    private String descricao;
    private OrganizationalUnitId parentUnitId;
    private boolean active;

    private OrganizationalUnit() {}

    private OrganizationalUnit(OrganizationalUnitId id, String code, String name, String acronym,
                                String unitType, String descricao,
                                OrganizationalUnitId parentUnitId, boolean active) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.acronym = acronym;
        this.unitType = unitType;
        this.descricao = descricao;
        this.parentUnitId = parentUnitId;
        this.active = active;
    }

    public static OrganizationalUnit criar(String code, String name, String acronym,
                                            String unitType, String descricao,
                                            OrganizationalUnitId parentUnitId) {
        return new OrganizationalUnit(OrganizationalUnitId.gerarNovo(), code, name, acronym,
                unitType, descricao, parentUnitId, true);
    }

    public static OrganizationalUnit reconstruir(OrganizationalUnitId id, String code, String name, String acronym,
                                                  String unitType, String descricao,
                                                  OrganizationalUnitId parentUnitId, boolean active) {
        return new OrganizationalUnit(id, code, name, acronym, unitType, descricao, parentUnitId, active);
    }

    public void atualizar(String code, String name, String acronym, String unitType,
                          String descricao, OrganizationalUnitId parentUnitId) {
        this.code = code;
        this.name = name;
        this.acronym = acronym;
        this.unitType = unitType;
        this.descricao = descricao;
        this.parentUnitId = parentUnitId;
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
