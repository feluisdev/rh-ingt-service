package cv.igrp.RH_Service.parametrizacoes.domain.models;

import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.WorkerStateId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;

import java.util.Objects;

@Getter
public class WorkerState {

    private WorkerStateId id;
    private String code;
    private String description;
    private boolean core;
    private boolean active;
    /** Estado de cessação: ao ser atribuído, termina a relação de emprego público. */
    private boolean endsEmployment;

    private WorkerState() {}

    private WorkerState(WorkerStateId id, String code, String description, boolean core, boolean active,
                        boolean endsEmployment) {
        this.id = id;
        this.code = code;
        this.description = description;
        this.core = core;
        this.active = active;
        this.endsEmployment = endsEmployment;
    }

    public static WorkerState criar(String code, String description, Boolean isCore, Boolean endsEmployment) {
        Objects.requireNonNull(code, "code não pode ser nulo");
        boolean effectiveCore = isCore != null && isCore;
        return new WorkerState(WorkerStateId.gerarNovo(), code, description, effectiveCore, true,
                endsEmployment != null && endsEmployment);
    }

    public static WorkerState reconstruir(WorkerStateId id, String code, String description, boolean core,
                                          boolean active, boolean endsEmployment) {
        return new WorkerState(id, code, description, core, active, endsEmployment);
    }

    public void atualizar(String description) {
        this.description = description;
    }

    public void atualizar(String description, Boolean endsEmployment) {
        this.description = description;
        if (endsEmployment != null) this.endsEmployment = endsEmployment;
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
