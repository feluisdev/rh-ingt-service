package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ReciboVencimentoId;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
public class ReciboVencimento {

    private ReciboVencimentoId id;
    private FuncionarioId funcionarioId;
    private Integer periodMonth;
    private Integer periodYear;
    private LocalDate issueDate;
    private BigDecimal grossSalary;
    private BigDecimal netSalary;
    private UUID documentId;

    private ReciboVencimento() {}

    public static ReciboVencimento criar(FuncionarioId funcionarioId, Integer periodMonth, Integer periodYear,
                                         LocalDate issueDate, BigDecimal grossSalary, BigDecimal netSalary,
                                         UUID documentId) {
        ReciboVencimento r = new ReciboVencimento();
        r.id = ReciboVencimentoId.gerarNovo();
        r.funcionarioId = funcionarioId;
        r.periodMonth = periodMonth;
        r.periodYear = periodYear;
        r.issueDate = issueDate;
        r.grossSalary = grossSalary;
        r.netSalary = netSalary;
        r.documentId = documentId;
        return r;
    }

    public void associarDocumento(UUID documentId) {
        this.documentId = documentId;
    }

    public static ReciboVencimento reconstituir(ReciboVencimentoId id, FuncionarioId funcionarioId,
                                                 Integer periodMonth, Integer periodYear, LocalDate issueDate,
                                                 BigDecimal grossSalary, BigDecimal netSalary, UUID documentId) {
        ReciboVencimento r = new ReciboVencimento();
        r.id = id;
        r.funcionarioId = funcionarioId;
        r.periodMonth = periodMonth;
        r.periodYear = periodYear;
        r.issueDate = issueDate;
        r.grossSalary = grossSalary;
        r.netSalary = netSalary;
        r.documentId = documentId;
        return r;
    }
}
