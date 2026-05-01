package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.ColocacaoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
public class Colocacao {

    private ColocacaoId id;
    private FuncionarioId funcionarioId;
    private UUID unitId;
    private UUID jobId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isCurrent;
    private Boolean isActive;
    private TipoAfectacao assignmentType;
    private String notes;

    private Colocacao() {}

    public static Colocacao criar(FuncionarioId funcionarioId, UUID unitId, UUID jobId,
                                  LocalDate startDate, TipoAfectacao assignmentType, String notes) {
        Colocacao c = new Colocacao();
        c.id = ColocacaoId.gerarNovo();
        c.funcionarioId = funcionarioId;
        c.unitId = unitId;
        c.jobId = jobId;
        c.startDate = startDate;
        c.endDate = null;
        c.isCurrent = true;
        c.isActive = true;
        c.assignmentType = assignmentType;
        c.notes = notes;
        return c;
    }

    public static Colocacao reconstituir(ColocacaoId id, FuncionarioId funcionarioId,
                                         UUID unitId, UUID jobId,
                                         LocalDate startDate, LocalDate endDate,
                                         Boolean isCurrent, Boolean isActive,
                                         TipoAfectacao assignmentType, String notes) {
        Colocacao c = new Colocacao();
        c.id = id;
        c.funcionarioId = funcionarioId;
        c.unitId = unitId;
        c.jobId = jobId;
        c.startDate = startDate;
        c.endDate = endDate;
        c.isCurrent = isCurrent;
        c.isActive = isActive;
        c.assignmentType = assignmentType;
        c.notes = notes;
        return c;
    }

    public void fechar(LocalDate endDate) {
        this.endDate = endDate;
        this.isCurrent = false;
    }

    public void atualizar(LocalDate endDate, String notes) {
        this.endDate = endDate;
        this.notes = notes;
    }

    public void desativar() {
        this.isActive = false;
    }
}
