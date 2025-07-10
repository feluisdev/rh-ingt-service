package cv.igrp.RH_Service.funcionarios.domain.models;

import cv.igrp.RH_Service.shared.application.constants.Estado;
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
  private String parentesco;
  private String cpf;
  private Estado estado;
  private Funcionario funcionario;

  private Dependente(Integer id, ExternalID externalId, String nome,
                     LocalDate dataNascimento, String parentesco,
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

  public static Dependente criar(String nome, LocalDate dataNascimento, String parentesco,
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

  // Reconstituição do estado (usado no repositório)
  public static Dependente reconstruir(Integer id, ExternalID externalId, String nome,
                                       LocalDate dataNascimento, String parentesco,
                                       String cpf, Estado estado, Funcionario funcionario) {
    Objects.requireNonNull(id, "ID é obrigatório");
    Objects.requireNonNull(externalId, "ExternalID é obrigatório");
    return new Dependente(id, externalId, nome, dataNascimento, parentesco, cpf, estado, funcionario);
  }

  // Atualização de dados
  public void atualizarDados(String nome, LocalDate dataNascimento, String parentesco, String cpf) {
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
