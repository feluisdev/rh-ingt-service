package cv.igrp.RH_Service.estrutura.domain.models;

import cv.igrp.RH_Service.estrutura.domain.valueobject.FunctionId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.UUID;

@Getter
public class OrgFunction {

    private FunctionId id;
    private String code;
    private String name;
    private String description;
    private UUID jobId;
    private boolean active;

    private OrgFunction() {}

    private OrgFunction(FunctionId id, String code, String name, String description, UUID jobId, boolean active) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.jobId = jobId;
        this.active = active;
    }

    public static OrgFunction criar(String code, String name, String description, UUID jobId) {
        return new OrgFunction(FunctionId.gerarNovo(), code, name, description, jobId, true);
    }

    public static OrgFunction reconstruir(FunctionId id, String code, String name, String description, UUID jobId, boolean active) {
        return new OrgFunction(id, code, name, description, jobId, active);
    }

    public void atualizar(String code, String name, String description, UUID jobId) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.jobId = jobId;
    }

    /**
     * Garante que a função é compatível com o cargo indicado no enquadramento.
     * Funções genéricas (jobId == null) são sempre compatíveis.
     */
    public void validarCompatibilidadeComCargo(UUID cargoId) {
        if (this.jobId != null && !this.jobId.equals(cargoId)) {
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "A função '" + this.name + "' não pertence ao cargo indicado.");
        }
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
