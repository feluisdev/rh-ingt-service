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
    private UUID unitTypeOptionId;
    private OrganizationalUnitId parentUnitId;
    private boolean active;

    private OrganizationalUnit() {}

    private OrganizationalUnit(OrganizationalUnitId id, String code, String name, String acronym,
                                UUID unitTypeOptionId, OrganizationalUnitId parentUnitId, boolean active) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.acronym = acronym;
        this.unitTypeOptionId = unitTypeOptionId;
        this.parentUnitId = parentUnitId;
        this.active = active;
    }

    public static OrganizationalUnit criar(String code, String name, String acronym,
                                            UUID unitTypeOptionId, OrganizationalUnitId parentUnitId) {
        return new OrganizationalUnit(OrganizationalUnitId.gerarNovo(), code, name, acronym,
                unitTypeOptionId, parentUnitId, true);
    }

    public static OrganizationalUnit reconstruir(OrganizationalUnitId id, String code, String name, String acronym,
                                                  UUID unitTypeOptionId, OrganizationalUnitId parentUnitId, boolean active) {
        return new OrganizationalUnit(id, code, name, acronym, unitTypeOptionId, parentUnitId, active);
    }

    public void atualizar(String code, String name, String acronym, UUID unitTypeOptionId, OrganizationalUnitId parentUnitId) {
        this.code = code;
        this.name = name;
        this.acronym = acronym;
        this.unitTypeOptionId = unitTypeOptionId;
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
