package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.ContratoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class Contrato {

    private ContratoId id;
    private FuncionarioId funcionarioId;
    private String tipoContrato;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String numeroContrato;
    private Boolean isActive;

    private Contrato() {}

    public static Contrato criar(FuncionarioId funcionarioId, String tipoContrato,
                                  LocalDate dataInicio, LocalDate dataFim, String numeroContrato) {
        Contrato c = new Contrato();
        c.id = ContratoId.gerarNovo();
        c.funcionarioId = funcionarioId;
        c.tipoContrato = tipoContrato;
        c.dataInicio = dataInicio;
        c.dataFim = dataFim;
        c.numeroContrato = numeroContrato;
        c.isActive = true;
        return c;
    }

    public static Contrato reconstituir(ContratoId id, FuncionarioId funcionarioId, String tipoContrato,
                                         LocalDate dataInicio, LocalDate dataFim,
                                         String numeroContrato, Boolean isActive) {
        Contrato c = new Contrato();
        c.id = id;
        c.funcionarioId = funcionarioId;
        c.tipoContrato = tipoContrato;
        c.dataInicio = dataInicio;
        c.dataFim = dataFim;
        c.numeroContrato = numeroContrato;
        c.isActive = isActive;
        return c;
    }

    public void atualizar(String tipoContrato, LocalDate dataInicio, LocalDate dataFim, String numeroContrato) {
        this.tipoContrato = tipoContrato;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.numeroContrato = numeroContrato;
    }

    public void desativar() {
        this.isActive = false;
    }

    public void ativar() {
        this.isActive = true;
    }
}
