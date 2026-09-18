package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.ContratoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
public class Contrato {

    private ContratoId id;
    private FuncionarioId funcionarioId;
    private UUID contractTypeId;
    private String contractNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private String terminationReason;
    private Boolean isCurrent;
    private EstadoContrato status;
    private Integer renewalCount;
    private String regimeTrabalho;   // TEMPO_COMPLETO | TEMPO_PARCIAL | ISENCAO_HORARIO | DEDICACAO_EXCLUSIVA
    private BigDecimal percentagemTempo; // preenchido apenas se regimeTrabalho = TEMPO_PARCIAL
    private String legalBase;
    private String notes;

    private Contrato() {}

    public static Contrato criar(FuncionarioId funcionarioId, UUID contractTypeId, String contractNumber,
                                  LocalDate startDate, LocalDate endDate, String legalBase, String notes,
                                  int renewalCount, String regimeTrabalho, BigDecimal percentagemTempo) {
        Contrato c = new Contrato();
        c.id = ContratoId.gerarNovo();
        c.funcionarioId = funcionarioId;
        c.contractTypeId = contractTypeId;
        c.contractNumber = contractNumber;
        c.startDate = startDate;
        c.endDate = endDate;
        c.legalBase = legalBase;
        c.notes = notes;
        c.isCurrent = true;
        c.status = EstadoContrato.ATIVO;
        c.renewalCount = renewalCount;
        c.regimeTrabalho = regimeTrabalho;
        c.percentagemTempo = percentagemTempo;
        return c;
    }

    public static Contrato reconstituir(ContratoId id, FuncionarioId funcionarioId, UUID contractTypeId,
                                         String contractNumber, LocalDate startDate, LocalDate endDate,
                                         String terminationReason, Boolean isCurrent,
                                         String status, Integer renewalCount,
                                         String regimeTrabalho, BigDecimal percentagemTempo,
                                         String legalBase, String notes) {
        Contrato c = new Contrato();
        c.id = id;
        c.funcionarioId = funcionarioId;
        c.contractTypeId = contractTypeId;
        c.contractNumber = contractNumber;
        c.startDate = startDate;
        c.endDate = endDate;
        c.terminationReason = terminationReason;
        c.isCurrent = isCurrent;
        c.status = EstadoContrato.de(status);
        c.renewalCount = renewalCount;
        c.regimeTrabalho = regimeTrabalho;
        c.percentagemTempo = percentagemTempo;
        c.legalBase = legalBase;
        c.notes = notes;
        return c;
    }

    public void atualizar(LocalDate endDate, String legalBase, String notes,
                          String regimeTrabalho, BigDecimal percentagemTempo) {
        this.endDate = endDate;
        this.legalBase = legalBase;
        this.notes = notes;
        this.regimeTrabalho = regimeTrabalho;
        this.percentagemTempo = percentagemTempo;
    }

    public void encerrar(LocalDate endDate, String terminationReason) {
        if (this.status == EstadoContrato.CESSADO)
            throw IgrpResponseStatusException.conflict("O contrato já está cessado.");
        this.endDate = endDate;
        this.terminationReason = terminationReason;
        this.isCurrent = false;
        this.status = EstadoContrato.CESSADO;
    }

    public void suspender() {
        if (this.status != EstadoContrato.ATIVO)
            throw IgrpResponseStatusException.conflict("Só é possível suspender um contrato ATIVO.");
        this.status = EstadoContrato.SUSPENSO;
    }

    public void reativar() {
        if (this.status != EstadoContrato.SUSPENSO)
            throw IgrpResponseStatusException.conflict("Só é possível reativar um contrato SUSPENSO.");
        this.status = EstadoContrato.ATIVO;
    }

    /** Nome do estado, para persistência e respostas. */
    public String getStatusTexto() {
        return EstadoContrato.texto(this.status);
    }
}
