package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.AssignmentId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Afectação (Assignment) — liga o funcionário a um Lugar (Position) ao longo do
 * tempo. Funde o antigo enquadramento + colocação. A unidade, o cargo e a carreira
 * NÃO vivem aqui — herdam-se do Lugar (position_id). Aqui vive o que é da pessoa:
 * escalão, função exercida, período (SCD Type 2) e a origem do movimento.
 */
@Getter
public class Assignment {

    // assignment_type
    public static final String PRINCIPAL = "PRINCIPAL";
    public static final String ACUMULACAO = "ACUMULACAO";
    public static final String SUBSTITUICAO = "SUBSTITUICAO";

    // origem
    public static final String ADMISSAO = "ADMISSAO";
    public static final String PROGRESSAO = "PROGRESSAO";
    public static final String PROMOCAO = "PROMOCAO";
    public static final String MOBILIDADE = "MOBILIDADE";
    public static final String TRANSFERENCIA = "TRANSFERENCIA";

    private AssignmentId id;
    private FuncionarioId funcionarioId;
    private UUID positionId;
    private UUID gradeId;              // null = fora de grelha
    private UUID functionId;          // opcional
    private String assignmentType;
    private String origem;
    private UUID originAssignmentId;  // mobilidade temporária: afectação a restaurar
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private Boolean isCurrent;
    private Boolean isActive;
    private String notes;

    private Assignment() {}

    public static Assignment criar(FuncionarioId funcionarioId, UUID positionId, UUID gradeId,
                                   UUID functionId, String assignmentType, String origem,
                                   LocalDate dataInicio, UUID originAssignmentId, String notes) {
        Assignment a = new Assignment();
        a.id = AssignmentId.gerarNovo();
        a.funcionarioId = funcionarioId;
        a.positionId = positionId;
        a.gradeId = gradeId;
        a.functionId = functionId;
        a.assignmentType = assignmentType != null ? assignmentType : PRINCIPAL;
        a.origem = origem;
        a.originAssignmentId = originAssignmentId;
        a.dataInicio = dataInicio;
        a.dataFim = null;
        a.isCurrent = true;
        a.isActive = true;
        a.notes = notes;
        return a;
    }

    public static Assignment reconstituir(AssignmentId id, FuncionarioId funcionarioId, UUID positionId,
                                          UUID gradeId, UUID functionId, String assignmentType,
                                          String origem, UUID originAssignmentId, LocalDate dataInicio,
                                          LocalDate dataFim, Boolean isCurrent, Boolean isActive,
                                          String notes) {
        Assignment a = new Assignment();
        a.id = id;
        a.funcionarioId = funcionarioId;
        a.positionId = positionId;
        a.gradeId = gradeId;
        a.functionId = functionId;
        a.assignmentType = assignmentType;
        a.origem = origem;
        a.originAssignmentId = originAssignmentId;
        a.dataInicio = dataInicio;
        a.dataFim = dataFim;
        a.isCurrent = isCurrent;
        a.isActive = isActive;
        a.notes = notes;
        return a;
    }

    /** Fecha a versão corrente (SCD Type 2). */
    public void encerrar(LocalDate dataFim) {
        this.dataFim = dataFim;
        this.isCurrent = false;
    }
}
