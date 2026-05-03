package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FeriadoId;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class Feriado {

    private FeriadoId id;
    private String nome;
    private LocalDate data;
    private Boolean isNational;
    private String municipioCkey;
    private Boolean isActive;

    private Feriado() {}

    public static Feriado criar(String nome, LocalDate data, Boolean isNational, String municipioCkey) {
        Feriado f = new Feriado();
        f.id = FeriadoId.gerarNovo();
        f.nome = nome;
        f.data = data;
        f.isNational = isNational;
        f.municipioCkey = municipioCkey;
        f.isActive = true;
        return f;
    }

    public static Feriado reconstituir(FeriadoId id, String nome, LocalDate data,
                                       Boolean isNational, String municipioCkey, Boolean isActive) {
        Feriado f = new Feriado();
        f.id = id;
        f.nome = nome;
        f.data = data;
        f.isNational = isNational;
        f.municipioCkey = municipioCkey;
        f.isActive = isActive;
        return f;
    }

    public void atualizar(String nome, LocalDate data, Boolean isNational, String municipioCkey) {
        this.nome = nome;
        this.data = data;
        this.isNational = isNational;
        this.municipioCkey = municipioCkey;
    }

    public void ativar() { this.isActive = true; }
    public void desativar() { this.isActive = false; }
}
