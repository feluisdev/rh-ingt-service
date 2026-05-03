package cv.igrp.RH_Service.estrutura.domain.models;

import cv.igrp.RH_Service.estrutura.domain.valueobject.FunctionId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;

@Getter
public class OrgFunction {

    private FunctionId id;
    private String code;
    private String name;
    private String description;
    private boolean active;

    private OrgFunction() {}

    private OrgFunction(FunctionId id, String code, String name, String description, boolean active) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.active = active;
    }

    public static OrgFunction criar(String code, String name, String description) {
        return new OrgFunction(FunctionId.gerarNovo(), code, name, description, true);
    }

    public static OrgFunction reconstruir(FunctionId id, String code, String name, String description, boolean active) {
        return new OrgFunction(id, code, name, description, active);
    }

    public void atualizar(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public void desativar() {
        if (!this.active) {
            throw IgrpResponseStatusException.conflict("A função já está inactiva.");
        }
        this.active = false;
    }

    public void reativar() {
        if (this.active) {
            throw IgrpResponseStatusException.conflict("A função já está activa.");
        }
        this.active = true;
    }
}
