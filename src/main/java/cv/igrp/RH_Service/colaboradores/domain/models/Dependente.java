package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.DependenteId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class Dependente {

    private DependenteId id;
    private FuncionarioId funcionarioId;
    private String nome;
    private String parentesco;
    private LocalDate dataNascimento;
    private String nif;
    private Boolean isActive;

    private Dependente() {}

    public static Dependente criar(FuncionarioId funcionarioId, String nome, String parentesco,
                                    LocalDate dataNascimento, String nif) {
        Dependente d = new Dependente();
        d.id = DependenteId.gerarNovo();
        d.funcionarioId = funcionarioId;
        d.nome = nome;
        d.parentesco = parentesco;
        d.dataNascimento = dataNascimento;
        d.nif = nif;
        d.isActive = true;
        return d;
    }

    public static Dependente reconstituir(DependenteId id, FuncionarioId funcionarioId, String nome,
                                           String parentesco, LocalDate dataNascimento, String nif, Boolean isActive) {
        Dependente d = new Dependente();
        d.id = id;
        d.funcionarioId = funcionarioId;
        d.nome = nome;
        d.parentesco = parentesco;
        d.dataNascimento = dataNascimento;
        d.nif = nif;
        d.isActive = isActive;
        return d;
    }

    public void atualizar(String nome, String parentesco, LocalDate dataNascimento, String nif) {
        this.nome = nome;
        this.parentesco = parentesco;
        this.dataNascimento = dataNascimento;
        this.nif = nif;
    }

    public void desativar() {
        this.isActive = false;
    }

    public void ativar() {
        this.isActive = true;
    }
}
