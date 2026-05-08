package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.QualificacaoId;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class Qualificacao {

    private QualificacaoId id;
    private FuncionarioId funcionarioId;
    private String level;
    private String courseName;
    private String institution;
    private String country;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean completed;
    private Boolean isActive;

    private Qualificacao() {}

    public static Qualificacao criar(FuncionarioId funcionarioId, String level, String courseName,
                                     String institution, String country,
                                     LocalDate startDate, LocalDate endDate, Boolean completed) {
        Qualificacao q = new Qualificacao();
        q.id = QualificacaoId.gerarNovo();
        q.funcionarioId = funcionarioId;
        q.level = level;
        q.courseName = courseName;
        q.institution = institution;
        q.country = country;
        q.startDate = startDate;
        q.endDate = endDate;
        q.completed = Boolean.TRUE.equals(completed);
        q.isActive = true;
        return q;
    }

    public static Qualificacao reconstituir(QualificacaoId id, FuncionarioId funcionarioId,
                                             String level, String courseName, String institution,
                                             String country, LocalDate startDate, LocalDate endDate,
                                             Boolean completed, Boolean isActive) {
        Qualificacao q = new Qualificacao();
        q.id = id;
        q.funcionarioId = funcionarioId;
        q.level = level;
        q.courseName = courseName;
        q.institution = institution;
        q.country = country;
        q.startDate = startDate;
        q.endDate = endDate;
        q.completed = completed;
        q.isActive = isActive;
        return q;
    }

    public void atualizar(String level, String courseName, String institution, String country,
                          LocalDate startDate, LocalDate endDate, Boolean completed) {
        if (level != null) this.level = level;
        if (courseName != null) this.courseName = courseName;
        if (institution != null) this.institution = institution;
        if (country != null) this.country = country;
        if (startDate != null) this.startDate = startDate;
        if (endDate != null) this.endDate = endDate;
        if (completed != null) this.completed = completed;
    }

    public void desativar() { this.isActive = false; }
    public void ativar() { this.isActive = true; }
}
