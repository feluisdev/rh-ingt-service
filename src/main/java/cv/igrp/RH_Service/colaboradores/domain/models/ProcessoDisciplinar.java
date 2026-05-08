package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.ProcessoDisciplinarId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class ProcessoDisciplinar {

    private ProcessoDisciplinarId id;
    private FuncionarioId funcionarioId;
    private String processNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private String penalty;
    private LocalDate penaltyStartDate;
    private LocalDate penaltyEndDate;
    private String officialBulletin;
    private String notes;

    private ProcessoDisciplinar() {}

    public static ProcessoDisciplinar criar(FuncionarioId funcionarioId, String processNumber,
                                             LocalDate startDate, LocalDate endDate, String penalty,
                                             LocalDate penaltyStartDate, LocalDate penaltyEndDate,
                                             String officialBulletin, String notes) {
        ProcessoDisciplinar p = new ProcessoDisciplinar();
        p.id = ProcessoDisciplinarId.gerarNovo();
        p.funcionarioId = funcionarioId;
        p.processNumber = processNumber;
        p.startDate = startDate;
        p.endDate = endDate;
        p.penalty = penalty;
        p.penaltyStartDate = penaltyStartDate;
        p.penaltyEndDate = penaltyEndDate;
        p.officialBulletin = officialBulletin;
        p.notes = notes;
        return p;
    }

    public static ProcessoDisciplinar reconstituir(ProcessoDisciplinarId id, FuncionarioId funcionarioId,
                                                    String processNumber, LocalDate startDate, LocalDate endDate,
                                                    String penalty, LocalDate penaltyStartDate, LocalDate penaltyEndDate,
                                                    String officialBulletin, String notes) {
        ProcessoDisciplinar p = new ProcessoDisciplinar();
        p.id = id;
        p.funcionarioId = funcionarioId;
        p.processNumber = processNumber;
        p.startDate = startDate;
        p.endDate = endDate;
        p.penalty = penalty;
        p.penaltyStartDate = penaltyStartDate;
        p.penaltyEndDate = penaltyEndDate;
        p.officialBulletin = officialBulletin;
        p.notes = notes;
        return p;
    }

    public void atualizar(String processNumber, LocalDate startDate, LocalDate endDate, String penalty,
                          LocalDate penaltyStartDate, LocalDate penaltyEndDate,
                          String officialBulletin, String notes) {
        if (processNumber != null) this.processNumber = processNumber;
        if (startDate != null) this.startDate = startDate;
        if (endDate != null) this.endDate = endDate;
        if (penalty != null) this.penalty = penalty;
        if (penaltyStartDate != null) this.penaltyStartDate = penaltyStartDate;
        if (penaltyEndDate != null) this.penaltyEndDate = penaltyEndDate;
        if (officialBulletin != null) this.officialBulletin = officialBulletin;
        if (notes != null) this.notes = notes;
    }
}
