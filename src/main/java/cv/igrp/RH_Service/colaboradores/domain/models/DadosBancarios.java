package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.DadosBancariosId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import lombok.Getter;

@Getter
public class DadosBancarios {

    private DadosBancariosId id;
    private FuncionarioId funcionarioId;
    private String banco;                  // ckey option_entity ccode=BANCO
    private String numeroConta;
    private String iban;
    private String numeroSegurancaSocial;  // Nº INPS
    private Boolean isActive;

    private DadosBancarios() {}

    public static DadosBancarios criar(FuncionarioId funcionarioId, String banco,
                                       String numeroConta, String iban,
                                       String numeroSegurancaSocial) {
        DadosBancarios d = new DadosBancarios();
        d.id = DadosBancariosId.gerarNovo();
        d.funcionarioId = funcionarioId;
        d.banco = banco;
        d.numeroConta = numeroConta;
        d.iban = iban;
        d.numeroSegurancaSocial = numeroSegurancaSocial;
        d.isActive = true;
        return d;
    }

    public static DadosBancarios reconstituir(DadosBancariosId id, FuncionarioId funcionarioId,
                                               String banco, String numeroConta, String iban,
                                               String numeroSegurancaSocial, Boolean isActive) {
        DadosBancarios d = new DadosBancarios();
        d.id = id;
        d.funcionarioId = funcionarioId;
        d.banco = banco;
        d.numeroConta = numeroConta;
        d.iban = iban;
        d.numeroSegurancaSocial = numeroSegurancaSocial;
        d.isActive = isActive;
        return d;
    }

    public void atualizar(String banco, String numeroConta, String iban, String numeroSegurancaSocial) {
        this.banco = banco;
        this.numeroConta = numeroConta;
        this.iban = iban;
        this.numeroSegurancaSocial = numeroSegurancaSocial;
    }

    public void desativar() { this.isActive = false; }
    public void ativar() { this.isActive = true; }
}
