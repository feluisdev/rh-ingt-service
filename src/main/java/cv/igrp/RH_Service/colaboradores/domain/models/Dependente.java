package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.DependenteId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class Dependente {

    private DependenteId id;
    private FuncionarioId funcionarioId;
    private String fullName;
    private String relationshipType;
    private LocalDate birthDate;
    private String nif;
    private Boolean isActive;

    private Dependente() {}

    public static Dependente criar(FuncionarioId funcionarioId, String fullName,
                                    String relationshipType, LocalDate birthDate, String nif) {
        Dependente d = new Dependente();
        d.id = DependenteId.gerarNovo();
        d.funcionarioId = funcionarioId;
        d.fullName = fullName;
        d.relationshipType = relationshipType;
        d.birthDate = birthDate;
        d.nif = nif;
        d.isActive = true;
        return d;
    }

    public static Dependente reconstituir(DependenteId id, FuncionarioId funcionarioId,
                                           String fullName, String relationshipType,
                                           LocalDate birthDate, String nif, Boolean isActive) {
        Dependente d = new Dependente();
        d.id = id;
        d.funcionarioId = funcionarioId;
        d.fullName = fullName;
        d.relationshipType = relationshipType;
        d.birthDate = birthDate;
        d.nif = nif;
        d.isActive = isActive;
        return d;
    }

    public void atualizar(String fullName, String relationshipType, LocalDate birthDate, String nif) {
        this.fullName = fullName;
        this.relationshipType = relationshipType;
        this.birthDate = birthDate;
        this.nif = nif;
    }

    public void desativar() { this.isActive = false; }
    public void ativar() { this.isActive = true; }
}
