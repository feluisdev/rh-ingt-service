package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.ContratoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;

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
    private String legalBase;
    private String notes;

    private Contrato() {}

    public static Contrato criar(FuncionarioId funcionarioId, UUID contractTypeId, String contractNumber,
                                  LocalDate startDate, LocalDate endDate, String legalBase, String notes) {
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
        return c;
    }

    public static Contrato reconstituir(ContratoId id, FuncionarioId funcionarioId, UUID contractTypeId,
                                         String contractNumber, LocalDate startDate, LocalDate endDate,
                                         String terminationReason, Boolean isCurrent,
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
        c.legalBase = legalBase;
        c.notes = notes;
        return c;
    }

    public void atualizar(LocalDate endDate, String legalBase, String notes) {
        this.endDate = endDate;
        this.legalBase = legalBase;
        this.notes = notes;
    }

    public void encerrar(LocalDate endDate, String terminationReason) {
        if (Boolean.FALSE.equals(this.isCurrent))
            throw IgrpResponseStatusException.conflict("O contrato já está encerrado.");
        this.endDate = endDate;
        this.terminationReason = terminationReason;
        this.isCurrent = false;
    }
}
