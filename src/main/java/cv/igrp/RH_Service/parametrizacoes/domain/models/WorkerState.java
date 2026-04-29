package cv.igrp.RH_Service.parametrizacoes.domain.models;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import lombok.Getter;

import java.util.Objects;

@Getter
public class WorkerState {

    private ExternalID id;
    private String code;
    private String description;
    private boolean core;
    private boolean active;

    private WorkerState() {}

    private WorkerState(ExternalID id, String code, String description, boolean core, boolean active) {
        this.id = id;
        this.code = code;
        this.description = description;
        this.core = core;
        this.active = active;
    }

    public static WorkerState criar(String code, String description, Boolean isCore) {
        Objects.requireNonNull(code, "code não pode ser nulo");
        boolean effectiveCore = isCore != null && isCore;
        return new WorkerState(ExternalID.gerarNovo(), code, description, effectiveCore, true);
    }

    public static WorkerState reconstruir(ExternalID id, String code, String description, boolean core, boolean active) {
        return new WorkerState(id, code, description, core, active);
    }

    public void atualizar(String description) {
        this.description = description;
    }

    public void desativar() {
        if (this.core) {
            throw IgrpResponseStatusException.conflict("Não é possível desativar um estado núcleo do sistema.");
        }
        if (!this.active) {
            throw IgrpResponseStatusException.conflict("Já está inactivo.");
        }
        this.active = false;
    }

    public void reativar() {
        if (this.active) {
            throw IgrpResponseStatusException.conflict("Já está activo.");
        }
        this.active = true;
    }
}
