package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.QualificacaoId;
import lombok.Getter;

@Getter
public class Qualificacao {

    private QualificacaoId id;
    private FuncionarioId funcionarioId;
    private String nivelAcademico;
    private String curso;
    private String instituicao;
    private Integer anoConclusao;
    private String pais;
    private Boolean isActive;

    private Qualificacao() {}

    public static Qualificacao criar(FuncionarioId funcionarioId, String nivelAcademico, String curso,
                                      String instituicao, Integer anoConclusao, String pais) {
        Qualificacao q = new Qualificacao();
        q.id = QualificacaoId.gerarNovo();
        q.funcionarioId = funcionarioId;
        q.nivelAcademico = nivelAcademico;
        q.curso = curso;
        q.instituicao = instituicao;
        q.anoConclusao = anoConclusao;
        q.pais = pais != null ? pais : "CV";
        q.isActive = true;
        return q;
    }

    public static Qualificacao reconstituir(QualificacaoId id, FuncionarioId funcionarioId,
                                             String nivelAcademico, String curso, String instituicao,
                                             Integer anoConclusao, String pais, Boolean isActive) {
        Qualificacao q = new Qualificacao();
        q.id = id;
        q.funcionarioId = funcionarioId;
        q.nivelAcademico = nivelAcademico;
        q.curso = curso;
        q.instituicao = instituicao;
        q.anoConclusao = anoConclusao;
        q.pais = pais;
        q.isActive = isActive;
        return q;
    }

    public void atualizar(String nivelAcademico, String curso, String instituicao, Integer anoConclusao, String pais) {
        this.nivelAcademico = nivelAcademico;
        this.curso = curso;
        this.instituicao = instituicao;
        this.anoConclusao = anoConclusao;
        this.pais = pais;
    }

    public void desativar() {
        this.isActive = false;
    }

    public void ativar() {
        this.isActive = true;
    }
}
