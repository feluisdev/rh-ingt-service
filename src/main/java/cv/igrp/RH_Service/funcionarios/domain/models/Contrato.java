package cv.igrp.RH_Service.funcionarios.domain.models;

import cv.igrp.RH_Service.shared.application.constants.Estado;
import cv.igrp.RH_Service.shared.application.constants.TipoContrato;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Getter
public class Contrato {

  private ExternalID idContrato;
  private TipoContrato tipoContrato;
  private LocalDate dataInicio;
  private LocalDate dataFim;
  private BigDecimal salario;
  private Integer cargaHoraria;
  private String observacoes;
  private Estado estado;

  private ExternalID funcionarioId;
  private Documento contratoAnexo;

  private Departamento departamento;
  private Cargo cargo;


  private Contrato(ExternalID idContrato, TipoContrato tipoContrato,
                   LocalDate dataInicio, LocalDate dataFim, BigDecimal salario,
                   Integer cargaHoraria, String observacoes, Estado estado,ExternalID funcionarioId,
                   Departamento departamento, Cargo cargo, Documento contratoAnexo) {

    this.idContrato = idContrato;
    this.tipoContrato = tipoContrato;
    this.dataInicio = dataInicio;
    this.dataFim = dataFim;
    this.salario = salario;
    this.cargaHoraria = cargaHoraria;
    this.observacoes = observacoes;
    this.estado = estado;
    this.funcionarioId = funcionarioId;
    this.departamento = departamento;
    this.cargo = cargo;
    this.contratoAnexo = contratoAnexo;
  }

  // ===== MÉTODO CRIAR =====
  public static Contrato criar(TipoContrato tipoContrato, LocalDate dataInicio, LocalDate dataFim,
                               BigDecimal salario, Integer cargaHoraria,
                               String observacoes, ExternalID funcionarioId, Cargo cargo, Departamento departamento) {

    Objects.requireNonNull(tipoContrato, "Tipo de contrato é obrigatório");
    Objects.requireNonNull(dataInicio, "Data de início é obrigatória");
    Objects.requireNonNull(dataFim, "Data fim é obrigatória");
    Objects.requireNonNull(salario, "Salário é obrigatório");
    Objects.requireNonNull(funcionarioId, "Funcionário id é obrigatório");

    if (dataInicio.isAfter(dataFim)) {
      throw IgrpResponseStatusException.badRequest("Data de início não pode ser posterior à data fim.");
    }

    // Validação: data fim não pode ser no passado (opcional)
    if (dataFim.isBefore(LocalDate.now())) {
      throw IgrpResponseStatusException.badRequest("Data fim não pode estar no passado.");
    }

    return new Contrato(
        ExternalID.gerarNovo(),
        tipoContrato,
        dataInicio,
        dataFim,
        salario,
        cargaHoraria,
        observacoes,
        Estado.A,
        funcionarioId,
        departamento,
        cargo,
        null
    );
  }


  // ===== MÉTODO RECONSTRUIR =====
  public static Contrato reconstruir(ExternalID idContrato, TipoContrato tipoContrato,
                                     LocalDate dataInicio, LocalDate dataFim, BigDecimal salario,
                                     Integer cargaHoraria, String observacoes, Estado estado,
                                     ExternalID funcionarioId, Cargo cargo, Departamento departamento, Documento contratoAnexo) {


    return new Contrato(
        ExternalID.gerarNovo(),
        tipoContrato,
        dataInicio,
        dataFim,
        salario,
        cargaHoraria,
        observacoes,
        Estado.A,
        funcionarioId,
        departamento,
        cargo,
        contratoAnexo
    );
  }



  public void atualizar(TipoContrato tipoContrato, BigDecimal salario,
                        Integer cargaHoraria, String observacoes,
                        Departamento departamento, Cargo cargo, LocalDate dataInicio, LocalDate dataFim) {

    Objects.requireNonNull(tipoContrato, "Tipo de contrato é obrigatório");
    Objects.requireNonNull(dataInicio, "Data de início é obrigatória");
    Objects.requireNonNull(dataFim, "Data fim é obrigatória");
    Objects.requireNonNull(salario, "Salário é obrigatório");

    // Validação: data início não pode ser depois da data fim
    if (dataInicio.isAfter(dataFim)) {
      throw IgrpResponseStatusException.badRequest("Data de início não pode ser posterior à data fim.");
    }

    // Validação: data fim não pode ser no passado (opcional)
    if (dataFim.isBefore(LocalDate.now())) {
      throw IgrpResponseStatusException.badRequest("Data fim não pode estar no passado.");
    }


    this.tipoContrato = tipoContrato;
    this.salario = salario;
    this.cargaHoraria = cargaHoraria;
    this.observacoes = observacoes;
    this.departamento = departamento;
    this.cargo = cargo;
    this.dataInicio = dataInicio;
    this.dataFim = dataFim;
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

  public void adicionarDocumento(Documento documento) {
    Objects.requireNonNull(documento, "Documento não pode ser nulo");
    this.contratoAnexo = documento;
  }

}
