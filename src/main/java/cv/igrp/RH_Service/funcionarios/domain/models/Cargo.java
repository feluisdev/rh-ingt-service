package cv.igrp.RH_Service.funcionarios.domain.models;

import cv.igrp.RH_Service.shared.application.constants.Estado;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
public class Cargo {

  private ExternalID idCargo;
  private String nome;
  private String codigo;
  private String descricao;
  private BigDecimal salarioBase;
  private Integer nivelHierarquico;
  private Estado estado;

  // Construtor privado para forçar uso dos métodos estáticos
  private Cargo(ExternalID idCargo, String nome, String codigo, String descricao,
                BigDecimal salarioBase, Integer nivelHierarquico, Estado estado) {

    this.idCargo = idCargo;
    this.nome = nome;
    this.codigo = codigo;
    this.descricao = descricao;
    this.salarioBase = salarioBase;
    this.nivelHierarquico = nivelHierarquico;
    this.estado = estado;
  }

  // Método factory para criar um novo Cargo
  public static Cargo criarNovo(String nome, String codigo, String descricao, BigDecimal salarioBase, Integer nivelHierarquico) {
    Objects.requireNonNull(nome, "Nome não pode ser nulo");

    return new Cargo(
        ExternalID.gerarNovo(), // gera novo UUID
        nome,
        codigo,
        descricao,
        salarioBase,
        nivelHierarquico,
        Estado.A // ativo por padrão ao criar
    );
  }

  // Método para reconstruir a partir de dados existentes (ex: do banco)
  public static Cargo reconstruir(ExternalID idCargo, String nome, String codigo, String descricao,
                                  BigDecimal salarioBase, Integer nivelHierarquico, Estado estado) {
    Objects.requireNonNull(idCargo, "ExternalID não pode ser nulo para reconstruir");
    Objects.requireNonNull(codigo, "Nome não pode ser nulo para reconstruir");
    return new Cargo(idCargo, nome, codigo,descricao, salarioBase, nivelHierarquico, estado);
  }

  // Método para atualizar os dados do cargo
  public void atualizar(String nome, String codigo, String descricao, BigDecimal salarioBase, Integer nivelHierarquico) {
    Objects.requireNonNull(nome, "Nome não pode ser nulo");
    Objects.requireNonNull(codigo, "Nome não pode ser nulo para reconstruir");
    this.nome = nome;
    this.codigo = codigo;
    this.descricao = descricao;
    this.salarioBase = salarioBase;
    this.nivelHierarquico = nivelHierarquico;
  }

  public void ativar() {
    this.estado = Estado.A;
  }

  public void inativar() {
    this.estado = Estado.I;
  }

}
