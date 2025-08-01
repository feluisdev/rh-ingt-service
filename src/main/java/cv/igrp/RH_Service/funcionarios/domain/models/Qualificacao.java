package cv.igrp.RH_Service.funcionarios.domain.models;

import cv.igrp.RH_Service.shared.application.constants.Estado;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Getter
public class Qualificacao {

  private Integer id;
  private ExternalID externalId;
  private String instituicao;
  private String curso;
  private LocalDate dataInicio;
  private LocalDate dataConclusao;
  private String nivel;
  private String situacao;
  private Integer cargaHoraria;
  private BigDecimal notaFinal;
  private Estado estado;
  private Funcionario funcionario;

  private Documento documento;

  private Qualificacao(Integer id, ExternalID externalId, String instituicao, String curso,
                       LocalDate dataInicio, LocalDate dataConclusao, String nivel, String situacao,
                       Integer cargaHoraria, BigDecimal notaFinal, Estado estado, Funcionario funcionario) {
    this.id = id;
    this.externalId = externalId;
    this.instituicao = instituicao;
    this.curso = curso;
    this.dataInicio = dataInicio;
    this.dataConclusao = dataConclusao;
    this.nivel = nivel;
    this.situacao = situacao;
    this.cargaHoraria = cargaHoraria;
    this.notaFinal = notaFinal;
    this.estado = estado;
    this.funcionario = funcionario;
  }

  // Factory para criação de nova qualificação
  public static Qualificacao criar(String instituicao, String curso, LocalDate dataInicio,
                                   LocalDate dataConclusao, String nivel, String situacao,
                                   Integer cargaHoraria, BigDecimal notaFinal, Funcionario funcionario) {
    Objects.requireNonNull(instituicao, "Instituição é obrigatória");
    Objects.requireNonNull(funcionario, "Funcionario é obrigatório");

    return new Qualificacao(
        null,
        ExternalID.gerarNovo(),
        instituicao,
        curso,
        dataInicio,
        dataConclusao,
        nivel,
        situacao,
        cargaHoraria,
        notaFinal,
        Estado.A,
        funcionario
    );
  }

  // Reconstrução do objeto a partir do banco
  public static Qualificacao reconstruir(Integer id, ExternalID externalId, String instituicao, String curso,
                                         LocalDate dataInicio, LocalDate dataConclusao, String nivel, String situacao,
                                         Integer cargaHoraria, BigDecimal notaFinal, Estado estado, Funcionario funcionario) {
    Objects.requireNonNull(id, "ID é obrigatório");
    Objects.requireNonNull(externalId, "ExternalID é obrigatório");
    Objects.requireNonNull(funcionario, "Funcionario é obrigatório");

    return new Qualificacao(id, externalId, instituicao, curso, dataInicio, dataConclusao,
        nivel, situacao, cargaHoraria, notaFinal, estado, funcionario);
  }

  // Atualizar campos
  public void atualizar(String instituicao, String curso, LocalDate dataInicio,
                        LocalDate dataConclusao, String nivel, String situacao,
                        Integer cargaHoraria, BigDecimal notaFinal) {
    if (instituicao != null && !instituicao.isBlank()) this.instituicao = instituicao;
    if (curso != null) this.curso = curso;
    this.dataInicio = dataInicio;
    this.dataConclusao = dataConclusao;
    if (nivel != null) this.nivel = nivel;
    if (situacao != null) this.situacao = situacao;
    this.cargaHoraria = cargaHoraria;
    this.notaFinal = notaFinal;
  }

  public void ativar() {
    this.estado = Estado.A;
  }

  public void desativar() {
    this.estado = Estado.I;
  }

  public void adicionarDocumento(Documento documento) {
    Objects.requireNonNull(documento, "Documento não pode ser nulo");
    this.documento = documento;
  }
  
}
