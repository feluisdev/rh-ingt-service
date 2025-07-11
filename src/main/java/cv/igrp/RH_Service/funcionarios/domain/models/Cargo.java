package cv.igrp.RH_Service.funcionarios.domain.models;

import cv.igrp.RH_Service.shared.application.constants.Estado;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
public class Cargo {

  private Integer id;
  private ExternalID externalId;
  private String nome;
  private String descricao;
  private BigDecimal salarioBase;
  private Integer nivelHierarquico;
  private Estado estado;

  // Construtor privado para forçar uso dos métodos estáticos
  private Cargo(Integer id, ExternalID externalId, String nome, String descricao,
                BigDecimal salarioBase, Integer nivelHierarquico, Estado estado) {
    this.id = id;
    this.externalId = externalId;
    this.nome = nome;
    this.descricao = descricao;
    this.salarioBase = salarioBase;
    this.nivelHierarquico = nivelHierarquico;
    this.estado = estado;
  }

  // Método factory para criar um novo Cargo
  public static Cargo criarNovo(String nome, String descricao, BigDecimal salarioBase, Integer nivelHierarquico) {
    Objects.requireNonNull(nome, "Nome não pode ser nulo");
    // Outros validations que desejar
    return new Cargo(
        null,
        ExternalID.gerarNovo(), // gera novo UUID
        nome,
        descricao,
        salarioBase,
        nivelHierarquico,
        Estado.A // ativo por padrão ao criar
    );
  }

  // Método para reconstruir a partir de dados existentes (ex: do banco)
  public static Cargo reconstruir(Integer id, ExternalID externalId, String nome, String descricao,
                                  BigDecimal salarioBase, Integer nivelHierarquico, Estado estado) {
    Objects.requireNonNull(id, "ID não pode ser nulo para reconstruir");
    Objects.requireNonNull(externalId, "ExternalID não pode ser nulo para reconstruir");
    return new Cargo(id, externalId, nome, descricao, salarioBase, nivelHierarquico, estado);
  }

  // Método para atualizar os dados do cargo
  public void atualizar(String nome, String descricao, BigDecimal salarioBase, Integer nivelHierarquico) {
    Objects.requireNonNull(nome, "Nome não pode ser nulo");
    this.nome = nome;
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
