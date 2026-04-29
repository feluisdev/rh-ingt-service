package cv.igrp.RH_Service.parametrizacoes.domain.models;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import lombok.Getter;

import java.util.Objects;

@Getter
public class ProfessionalSituation {

    private ExternalID id;
    private String code;
    private String description;
    private boolean active;

    private ProfessionalSituation() {}

    private ProfessionalSituation(ExternalID id, String code, String description, boolean active) {
        this.id = id;
        this.code = code;
        this.description = description;
        this.active = active;
    }

    public static ProfessionalSituation criar(String code, String description) {
        Objects.requireNonNull(code, "code não pode ser nulo");
        return new ProfessionalSituation(ExternalID.gerarNovo(), code, description, true);
    }

    public static ProfessionalSituation reconstruir(ExternalID id, String code, String description, boolean active) {
        return new ProfessionalSituation(id, code, description, active);
    }

    public void atualizar(String description) {
        this.description = description;
    }

    public void desativar() {
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
