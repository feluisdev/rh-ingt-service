package cv.igrp.RH_Service.estrutura.domain.models;

import cv.igrp.RH_Service.estrutura.domain.valueobject.JobId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;

@Getter
public class Job {

    private JobId id;
    private String code;
    private String name;
    private String description;
    private Integer nivel;
    private boolean active;

    private Job() {}

    private Job(JobId id, String code, String name, String description, Integer nivel, boolean active) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.nivel = nivel;
        this.active = active;
    }

    public static Job criar(String code, String name, String description, Integer nivel) {
        return new Job(JobId.gerarNovo(), code, name, description, nivel, true);
    }

    public static Job reconstruir(JobId id, String code, String name, String description, Integer nivel, boolean active) {
        return new Job(id, code, name, description, nivel, active);
    }

    public void atualizar(String code, String name, String description, Integer nivel) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.nivel = nivel;
    }

    public void desativar() {
        if (!this.active) {
            throw IgrpResponseStatusException.conflict("O cargo já está inactivo.");
        }
        this.active = false;
    }

    public void reativar() {
        if (this.active) {
            throw IgrpResponseStatusException.conflict("O cargo já está activo.");
        }
        this.active = true;
    }
}
