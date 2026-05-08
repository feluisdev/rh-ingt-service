package cv.igrp.RH_Service.parametrizacoes.domain.models;

import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.VinculoLaboralId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;

import java.util.Objects;

@Getter
public class VinculoLaboral {

    private VinculoLaboralId id;
    private String code;
    private String description;
    private boolean countsSeniority;
    private boolean eligibleForProgression;
    private boolean active;

    private VinculoLaboral() {}

    private VinculoLaboral(VinculoLaboralId id, String code, String description,
                            boolean countsSeniority, boolean eligibleForProgression, boolean active) {
        this.id = id;
        this.code = code;
        this.description = description;
        this.countsSeniority = countsSeniority;
        this.eligibleForProgression = eligibleForProgression;
        this.active = active;
    }

    public static VinculoLaboral criar(String code, String description,
                                       boolean countsSeniority, boolean eligibleForProgression) {
        Objects.requireNonNull(code, "code não pode ser nulo");
        return new VinculoLaboral(VinculoLaboralId.gerarNovo(), code, description,
                countsSeniority, eligibleForProgression, true);
    }

    public static VinculoLaboral reconstruir(VinculoLaboralId id, String code, String description,
                                              boolean countsSeniority, boolean eligibleForProgression, boolean active) {
        return new VinculoLaboral(id, code, description, countsSeniority, eligibleForProgression, active);
    }

    public void atualizar(String description, boolean countsSeniority, boolean eligibleForProgression) {
        this.description = description;
        this.countsSeniority = countsSeniority;
        this.eligibleForProgression = eligibleForProgression;
    }

    public void desativar() {
        if (!this.active) throw IgrpResponseStatusException.conflict("Já está inactivo.");
        this.active = false;
    }

    public void reativar() {
        if (this.active) throw IgrpResponseStatusException.conflict("Já está activo.");
        this.active = true;
    }
}
