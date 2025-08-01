package cv.igrp.RH_Service.funcionarios.domain.models;

import cv.igrp.RH_Service.shared.application.constants.Estado;
import cv.igrp.RH_Service.shared.application.constants.GrauParentesco;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Objects;

@Getter
public class Dependente {

  private ExternalID idDependente;
  private String nome;
  private LocalDate dataNascimento;
  private GrauParentesco parentesco;
  private String cpf;
  private Estado estado;
  private ExternalID funcionarioId; // ExternalID do funcionário associado


  private Dependente(ExternalID idDependente, String nome,
                     LocalDate dataNascimento, GrauParentesco parentesco,
                     String cpf, Estado estado, ExternalID funcionarioId) {

    this.idDependente = idDependente;
    this.nome = nome;
    this.dataNascimento = dataNascimento;
    this.parentesco = parentesco;
    this.cpf = cpf;
    this.estado = estado;
    this.funcionarioId = funcionarioId;
  }




  public static Dependente criar(String nome, LocalDate dataNascimento, GrauParentesco parentesco,
                                 String cpf, ExternalID funcionarioId) {
    Objects.requireNonNull(nome, "Nome é obrigatório");
    Objects.requireNonNull(funcionarioId, "Funcionario é obrigatório");

    return new Dependente(
        ExternalID.gerarNovo(),
        nome,
        dataNascimento,
        parentesco,
        cpf,
        Estado.A,
        funcionarioId
    );
  }


  // Reconstituição do usando funcionarioID
  public static Dependente reconstruir(ExternalID idDependente, String nome,
                                       LocalDate dataNascimento, GrauParentesco parentesco,
                                       String cpf, Estado estado,  ExternalID funcionarioId) {
    Objects.requireNonNull(idDependente, "idDependente é obrigatório");
    return reconstruir( idDependente, nome, dataNascimento, parentesco, cpf, estado, funcionarioId);
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
