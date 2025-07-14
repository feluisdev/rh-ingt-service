package cv.igrp.RH_Service.funcionarios.domain.models;

import cv.igrp.RH_Service.shared.application.constants.Estado;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Getter
public class Contrato {

  private Integer id; // gerado pelo banco
  private ExternalID externalId;

  private String tipoContrato;
  private LocalDate dataInicio;
  private LocalDate dataFim;
  private BigDecimal salario;
  private Integer cargaHoraria;
  private String observacoes;
  private Estado estado;

  private Departamento departamento;
  private Funcionario funcionario;
  private Cargo cargo;

  private Contrato(Integer id, ExternalID externalId, String tipoContrato,
                   LocalDate dataInicio, LocalDate dataFim, BigDecimal salario,
                   Integer cargaHoraria, String observacoes, Estado estado,
                   Departamento departamento, Funcionario funcionario, Cargo cargo) {

    this.id = id;
    this.externalId = externalId;
    this.tipoContrato = tipoContrato;
    this.dataInicio = dataInicio;
    this.dataFim = dataFim;
    this.salario = salario;
    this.cargaHoraria = cargaHoraria;
    this.observacoes = observacoes;
    this.estado = estado;
    this.departamento = departamento;
    this.funcionario = funcionario;
    this.cargo = cargo;
  }

  // ===== MÉTODO CRIAR =====
  public static Contrato criar(String tipoContrato, LocalDate dataInicio, LocalDate dataFim,
                               BigDecimal salario, Integer cargaHoraria,
                               String observacoes, Departamento departamento,
                               Funcionario funcionario, Cargo cargo) {

    Objects.requireNonNull(tipoContrato, "Tipo de contrato é obrigatório");
    Objects.requireNonNull(dataInicio, "Data de início é obrigatória");
    Objects.requireNonNull(salario, "Salário é obrigatório");
    Objects.requireNonNull(funcionario, "Funcionário é obrigatório");

    return new Contrato(
        null,
        ExternalID.gerarNovo(),
        tipoContrato,
        dataInicio,
        dataFim,
        salario,
        cargaHoraria,
        observacoes,
        Estado.A,
        departamento,
        funcionario,
        cargo
    );
  }

  // ===== MÉTODO RECONSTRUIR =====
  public static Contrato reconstruir(Integer id, ExternalID externalId, String tipoContrato,
                                     LocalDate dataInicio, LocalDate dataFim, BigDecimal salario,
                                     Integer cargaHoraria, String observacoes, Estado estado,
                                     Departamento departamento, Funcionario funcionario, Cargo cargo) {

    Objects.requireNonNull(id, "ID é obrigatório");
    Objects.requireNonNull(externalId, "ExternalID é obrigatório");
    Objects.requireNonNull(dataInicio, "Data de início é obrigatória");
    Objects.requireNonNull(estado, "Estado é obrigatório");

    return new Contrato(
        id,
        externalId,
        tipoContrato,
        dataInicio,
        dataFim,
        salario,
        cargaHoraria,
        observacoes,
        estado,
        departamento,
        funcionario,
        cargo
    );
  }

  // ===== MÉTODO ATUALIZAR =====
  public void atualizar(String tipoContrato, BigDecimal salario,
                        Integer cargaHoraria, String observacoes,
                        Departamento departamento, Cargo cargo, LocalDate dataInicio, LocalDate dataFim) {

    this.tipoContrato = tipoContrato;
    this.salario = salario;
    this.cargaHoraria = cargaHoraria;
    this.observacoes = observacoes;
    this.departamento = departamento;
    this.cargo = cargo;
    this.dataInicio = dataInicio;
    this.dataFim = dataFim;
  }

  public void atualizar(String tipoContrato, BigDecimal salario,
                        Integer cargaHoraria, String observacoes,
                        Departamento departamento, Cargo cargo) {

    this.tipoContrato = tipoContrato;
    this.salario = salario;
    this.cargaHoraria = cargaHoraria;
    this.observacoes = observacoes;
    this.departamento = departamento;
    this.cargo = cargo;
  }

  // ===== MÉTODO ATIVAR =====
  public void ativar() {
    if (estado == Estado.A) {
      throw new IllegalStateException("Contrato já está ativo.");
    }
    this.estado = Estado.A;
    this.dataFim = null;
  }

  // ===== MÉTODO DESATIVAR =====
  public void desativar(LocalDate dataFim) {
    if (estado == Estado.I) {
      throw new IllegalStateException("Contrato já está inativo.");
    }
    if (dataFim != null && dataFim.isBefore(this.dataInicio)) {
      throw new IllegalArgumentException("Data fim não pode ser antes da data de início.");
    }
    this.estado = Estado.I;
    this.dataFim = dataFim;
  }

  // ===== MÉTODO AUXILIAR =====
  public boolean isAtivo() {
    return this.estado == Estado.A;
  }
}
