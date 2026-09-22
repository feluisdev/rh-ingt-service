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

    // origem
    public static final String ADMISSAO = "ADMISSAO";
    public static final String PROGRESSAO = "PROGRESSAO";
    public static final String PROMOCAO = "PROMOCAO";
    public static final String MOBILIDADE = "MOBILIDADE";
    public static final String TRANSFERENCIA = "TRANSFERENCIA";
    public static final String SUBSTITUICAO = "SUBSTITUICAO";
    public static final String MUDANCA_CARREIRA = "MUDANCA_CARREIRA";

    private AssignmentId id;
    private FuncionarioId funcionarioId;
    private UUID positionId;
    private UUID gradeId;              // null = fora de grelha
    private UUID functionId;          // opcional
    private TipoAfectacao assignmentType;
    private String origem;
    private UUID titularAssignmentId; // substituição: a afectação do titular impedido que esta cobre
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private Boolean isCurrent;
    private Boolean isActive;
    private String notes;

    private Assignment() {}

    public static Assignment criar(FuncionarioId funcionarioId, UUID positionId, UUID gradeId,
                                   UUID functionId, TipoAfectacao assignmentType, String origem,
                                   LocalDate dataInicio, String notes) {
        Assignment a = new Assignment();
        a.id = AssignmentId.gerarNovo();
        a.funcionarioId = funcionarioId;
        a.positionId = positionId;
        a.gradeId = gradeId;
        a.functionId = functionId;
        a.assignmentType = assignmentType != null ? assignmentType : TipoAfectacao.PRINCIPAL;
        a.origem = origem;
        a.dataInicio = dataInicio;
        a.dataFim = null;
        a.isCurrent = true;
        a.isActive = true;
        a.notes = notes;
        return a;
    }

    /**
     * Afectação em substituição de um titular temporariamente impedido
     * (Lei n.º 20/X/2023, art. 73.º al. a) a c) e art. 91.º n.º 1 al. a)).
     *
     * <p>Tem factory própria, e não um parâmetro a mais em {@link #criar}, porque a
     * substituição só existe <b>ligada à afectação do titular</b>: sem essa ligação
     * não se sabe quando caduca (art. 77.º n.º 2). O tipo e a origem não se escolhem.
     */
    public static Assignment criarSubstituicao(FuncionarioId funcionarioId, UUID positionId, UUID gradeId,
                                               UUID functionId, AssignmentId titularAssignmentId,
                                               LocalDate dataInicio, String notes) {
        Assignment a = criar(funcionarioId, positionId, gradeId, functionId,
                TipoAfectacao.SUBSTITUICAO, SUBSTITUICAO, dataInicio, notes);
        a.titularAssignmentId = titularAssignmentId.getValor();
        return a;
    }

    public static Assignment reconstituir(AssignmentId id, FuncionarioId funcionarioId, UUID positionId,
                                          UUID gradeId, UUID functionId, String assignmentType,
                                          String origem, UUID titularAssignmentId,
                                          LocalDate dataInicio, LocalDate dataFim, Boolean isCurrent,
                                          Boolean isActive, String notes) {
        Assignment a = new Assignment();
        a.id = id;
        a.funcionarioId = funcionarioId;
        a.positionId = positionId;
        a.gradeId = gradeId;
        a.functionId = functionId;
        a.assignmentType = TipoAfectacao.de(assignmentType);
        a.origem = origem;
        a.titularAssignmentId = titularAssignmentId;
        a.dataInicio = dataInicio;
        a.dataFim = dataFim;
        a.isCurrent = isCurrent;
        a.isActive = isActive;
        a.notes = notes;
        return a;
    }

    /** Nome a guardar na coluna {@code assignment_type}. */
    public String getAssignmentTypeTexto() {
        return TipoAfectacao.texto(this.assignmentType);
    }

    /** Titular do Lugar — o que a regra "uma cadeira, um titular" conta. */
    public boolean isPrincipal() {
        return assignmentType != null && assignmentType.isPrincipal();
    }

    public boolean isSubstituicao() {
        return assignmentType == TipoAfectacao.SUBSTITUICAO;
    }

    /** Fecha a versão corrente (SCD Type 2). */
    public void encerrar(LocalDate dataFim) {
        this.dataFim = dataFim;
        this.isCurrent = false;
    }
}
