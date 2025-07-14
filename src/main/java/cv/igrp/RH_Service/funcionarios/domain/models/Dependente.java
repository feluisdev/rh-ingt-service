package cv.igrp.RH_Service.funcionarios.domain.models;

import cv.igrp.RH_Service.shared.application.constants.Estado;
import cv.igrp.RH_Service.shared.application.constants.GrauParentesco;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Objects;

@Getter
public class Dependente {

  private Integer id;
  private ExternalID externalId;
  private String nome;
  private LocalDate dataNascimento;
  private GrauParentesco parentesco;
  private String cpf;
  private Estado estado;
  private Integer funcionarioId; // ID do funcionário associado
  private ExternalID funcionarioExternalId; // ExternalID do funcionário associado
  private Funcionario funcionario;

  private Dependente(Integer id, ExternalID externalId, String nome,
                     LocalDate dataNascimento, GrauParentesco parentesco,
                     String cpf, Estado estado, Funcionario funcionario) {
    this.id = id;
    this.externalId = externalId;
    this.nome = nome;
    this.dataNascimento = dataNascimento;
    this.parentesco = parentesco;
    this.cpf = cpf;
    this.estado = estado;
    this.funcionario = funcionario;
  }
  private Dependente(Integer id, ExternalID externalId, String nome,
                     LocalDate dataNascimento, GrauParentesco parentesco,
                     String cpf, Estado estado, Integer funcionarioId) {
    this.id = id;
    this.externalId = externalId;
    this.nome = nome;
    this.dataNascimento = dataNascimento;
    this.parentesco = parentesco;
    this.cpf = cpf;
    this.estado = estado;
    this.funcionarioId = funcionarioId;
  }

  private Dependente(Integer id, ExternalID externalId, String nome,
                     LocalDate dataNascimento, GrauParentesco parentesco,
                     String cpf, Estado estado, ExternalID funcionarioExternalId) {
    this.id = id;
    this.externalId = externalId;
    this.nome = nome;
    this.dataNascimento = dataNascimento;
    this.parentesco = parentesco;
    this.cpf = cpf;
    this.estado = estado;
    this.funcionarioExternalId = funcionarioExternalId;
  }

  public static Dependente criar(String nome, LocalDate dataNascimento, GrauParentesco parentesco,
                                 String cpf, Funcionario funcionario) {
    Objects.requireNonNull(nome, "Nome é obrigatório");
    Objects.requireNonNull(funcionario, "Funcionario é obrigatório");

    return new Dependente(
        null,
        ExternalID.gerarNovo(),
        nome,
        dataNascimento,
        parentesco,
        cpf,
        Estado.A,
        funcionario
    );
  }

  public static Dependente criar(String nome, LocalDate dataNascimento, GrauParentesco parentesco,
                                 String cpf, Integer funcionarioId) {
    Objects.requireNonNull(nome, "Nome é obrigatório");
    Objects.requireNonNull(funcionarioId, "Funcionario é obrigatório");

    return new Dependente(
        null,
        ExternalID.gerarNovo(),
        nome,
        dataNascimento,
        parentesco,
        cpf,
        Estado.A,
        funcionarioId
    );
  }

  public static Dependente criar(String nome, LocalDate dataNascimento, GrauParentesco parentesco,
                                 String cpf, ExternalID funcionarioExternalId) {
    Objects.requireNonNull(nome, "Nome é obrigatório");
    Objects.requireNonNull(funcionarioExternalId, "Funcionario é obrigatório");

    return new Dependente(
        null,
        ExternalID.gerarNovo(),
        nome,
        dataNascimento,
        parentesco,
        cpf,
        Estado.A,
        funcionarioExternalId
    );
  }

  // Reconstituição do estado (usado no repositório)
  public static Dependente reconstruir(Integer id, ExternalID externalId, String nome,
                                       LocalDate dataNascimento, GrauParentesco parentesco,
                                       String cpf, Estado estado, Funcionario funcionario) {
    Objects.requireNonNull(id, "ID é obrigatório");
    Objects.requireNonNull(externalId, "ExternalID é obrigatório");
    return new Dependente(id, externalId, nome, dataNascimento, parentesco, cpf, estado, funcionario);
  }

  // Reconstituição do estado (usado no repositório)
  public static Dependente reconstruir(Integer id, ExternalID externalId, String nome,
                                       LocalDate dataNascimento, GrauParentesco parentesco,
                                       String cpf, Estado estado,  Integer funcionarioId) {
    return reconstruir(id, externalId, nome, dataNascimento, parentesco, cpf, estado, funcionarioId);
  }

  // Reconstituição do estado (usado no repositório)
  public static Dependente reconstruir(Integer id, ExternalID externalId, String nome,
                                       LocalDate dataNascimento, GrauParentesco parentesco,
                                       String cpf, Estado estado,  ExternalID funcionarioExternalId) {
    return reconstruir(id, externalId, nome, dataNascimento, parentesco, cpf, estado, funcionarioExternalId);
  }

  // Atualização de dados
  public void atualizarDados(String nome, LocalDate dataNascimento, GrauParentesco parentesco, String cpf) {
    this.nome = nome;
    this.dataNascimento = dataNascimento;
    this.parentesco = parentesco;
    this.cpf = cpf;
  }


  public void desativar() {
    this.estado = Estado.I; // Inativo
  }

  public void ativar() {

    this.estado = Estado.A; // Ativo
  }
}
