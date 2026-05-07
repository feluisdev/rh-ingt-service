package cv.igrp.RH_Service.carreiras.domain.models;

import cv.igrp.RH_Service.carreiras.domain.valueobject.CareerId;
import lombok.Getter;

@Getter
public class Career {

    private CareerId id;
    private String code;
    private String name;
    private String description;
    private String regime;
    private Boolean isActive;

    private Career() {}

    public static Career criar(String code, String name, String description, String regime) {
        Career career = new Career();
        career.id = CareerId.gerarNovo();
        career.code = code;
        career.name = name;
        career.description = description;
        career.regime = regime;
        career.isActive = true;
        return career;
    }

    public static Career reconstituir(CareerId id, String code, String name, String description,
                                      String regime, Boolean isActive) {
        Career career = new Career();
        career.id = id;
        career.code = code;
        career.name = name;
        career.description = description;
        career.regime = regime;
        career.isActive = isActive;
        return career;
    }

    public void atualizar(String code, String name, String description, String regime) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.regime = regime;
    }

    public void desativar() {
        if (Boolean.FALSE.equals(this.isActive)) {
            throw new IllegalStateException("Carreira já está inativa");
        }
        this.isActive = false;
    }

    public void reativar() {
        if (Boolean.TRUE.equals(this.isActive)) {
            throw new IllegalStateException("Carreira já está ativa");
        }
        this.isActive = true;
    }
}
