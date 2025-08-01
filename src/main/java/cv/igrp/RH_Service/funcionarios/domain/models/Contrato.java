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

  private ExternalID departamentoId;
  private ExternalID funcionarioId;
  private ExternalID cargoId;

  private Documento contratoAnexo;

  private Contrato(ExternalID idContrato, TipoContrato tipoContrato,
                   LocalDate dataInicio, LocalDate dataFim, BigDecimal salario,
                   Integer cargaHoraria, String observacoes, Estado estado,
                   ExternalID departamentoId, ExternalID funcionarioId, ExternalID cargoId, Documento contratoAnexo) {

    this.idContrato = idContrato;
    this.tipoContrato = tipoContrato;
    this.dataInicio = dataInicio;
    this.dataFim = dataFim;
    this.salario = salario;
    this.cargaHoraria = cargaHoraria;
    this.observacoes = observacoes;
    this.estado = estado;
    this.departamentoId = departamentoId;
    this.funcionarioId = funcionarioId;
    this.cargoId = cargoId;
    this.contratoAnexo = contratoAnexo;
  }

  // ===== MÉTODO CRIAR =====
  public static Contrato criar(TipoContrato tipoContrato, LocalDate dataInicio, LocalDate dataFim,
                               BigDecimal salario, Integer cargaHoraria,
                               String observacoes, ExternalID departamentoId, ExternalID funcionarioId, ExternalID cargoId,
                               Documento contratoAnexo) {

    Objects.requireNonNull(tipoContrato, "Tipo de contrato é obrigatório");
    Objects.requireNonNull(dataInicio, "Data de início é obrigatória");
    Objects.requireNonNull(dataFim, "Data fim é obrigatória");
    Objects.requireNonNull(salario, "Salário é obrigatório");
    Objects.requireNonNull(funcionarioId, "Funcionário id é obrigatório");

   if (dataFim.isBefore(dataInicio)) {
      throw IgrpResponseStatusException.badRequest("Data fim não pode ser antes da data de início.");
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
        departamentoId,
        funcionarioId,
        cargoId,
        contratoAnexo
    );
  }

  // ===== MÉTODO CRIAR =====
  public static Contrato criar(TipoContrato tipoContrato, LocalDate dataInicio, LocalDate dataFim,
                               BigDecimal salario, Integer cargaHoraria,
                               String observacoes, ExternalID departamentoId, ExternalID funcionarioId, ExternalID cargoId) {

    Objects.requireNonNull(tipoContrato, "Tipo de contrato é obrigatório");
    Objects.requireNonNull(dataInicio, "Data de início é obrigatória");
    Objects.requireNonNull(dataFim, "Data fim é obrigatória");
    Objects.requireNonNull(salario, "Salário é obrigatório");
    Objects.requireNonNull(funcionarioId, "Funcionário é obrigatório");

    if (dataFim.isBefore(dataInicio)) {
      throw IgrpResponseStatusException.badRequest("Data fim não pode ser antes da data de início.");
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
        departamentoId,
        funcionarioId,
        cargoId,
        null
    );
  }

  // ===== MÉTODO RECONSTRUIR =====
  public static Contrato reconstruir(ExternalID idContrato, TipoContrato tipoContrato,
                                     LocalDate dataInicio, LocalDate dataFim, BigDecimal salario,
                                     Integer cargaHoraria, String observacoes, Estado estado,
                                     ExternalID departamentoId, ExternalID funcionarioId, ExternalID cargoId, Documento contratoAnexo) {

    Objects.requireNonNull(idContrato, "idContrato é obrigatório");
    Objects.requireNonNull(dataInicio, "Data de início é obrigatória");
    Objects.requireNonNull(estado, "Estado é obrigatório");

    return new Contrato(
        idContrato,
        tipoContrato,
        dataInicio,
        dataFim,
        salario,
        cargaHoraria,
        observacoes,
        estado,
        departamentoId,
        funcionarioId,
        cargoId,
        contratoAnexo
    );
  }

  // ===== MÉTODO RECONSTRUIR =====
  public static Contrato reconstruir(ExternalID idContrato, TipoContrato tipoContrato,
                                     LocalDate dataInicio, LocalDate dataFim, BigDecimal salario,
                                     Integer cargaHoraria, String observacoes, Estado estado,
                                     ExternalID departamentoId, ExternalID funcionarioId, ExternalID cargoId) {

    Objects.requireNonNull(idContrato, "ExternalID é obrigatório");
    Objects.requireNonNull(dataInicio, "Data de início é obrigatória");
    Objects.requireNonNull(estado, "Estado é obrigatório");

    return new Contrato(
        idContrato,
        tipoContrato,
        dataInicio,
        dataFim,
        salario,
        cargaHoraria,
        observacoes,
        estado,
        departamentoId,
        funcionarioId,
        cargoId,
        null
    );
  }

  // ===== MÉTODO ATUALIZAR =====
  public void atualizar(TipoContrato tipoContrato, BigDecimal salario,
                        Integer cargaHoraria, String observacoes,
                        ExternalID departamentoId, ExternalID cargoId, LocalDate dataInicio, LocalDate dataFim, Documento contratoAnexo) {

    this.tipoContrato = tipoContrato;
    this.salario = salario;
    this.cargaHoraria = cargaHoraria;
    this.observacoes = observacoes;
    this.departamentoId = departamentoId;
    this.cargoId = cargoId;
    this.dataInicio = dataInicio;
    this.dataFim = dataFim;
    this.contratoAnexo = contratoAnexo;
  }

  public void atualizar(TipoContrato tipoContrato, BigDecimal salario,
                        Integer cargaHoraria, String observacoes,
                        ExternalID departamentoId, ExternalID cargoId, Documento contratoAnexo) {

    this.tipoContrato = tipoContrato;
    this.salario = salario;
    this.cargaHoraria = cargaHoraria;
    this.observacoes = observacoes;
    this.departamentoId = departamentoId;
    this.cargoId = cargoId;
    this.contratoAnexo = contratoAnexo;

  }

  public void atualizar(TipoContrato tipoContrato, BigDecimal salario,
                        Integer cargaHoraria, String observacoes,
                        ExternalID departamentoId, ExternalID cargoId, LocalDate dataInicio, LocalDate dataFim) {

    this.tipoContrato = tipoContrato;
    this.salario = salario;
    this.cargaHoraria = cargaHoraria;
    this.observacoes = observacoes;
    this.departamentoId = departamentoId;
    this.cargoId = cargoId;
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
