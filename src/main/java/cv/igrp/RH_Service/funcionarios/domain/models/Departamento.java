package cv.igrp.RH_Service.funcionarios.domain.models;

import cv.igrp.RH_Service.shared.application.constants.Estado;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class Departamento {

  private final ExternalID idDepartamento;
  private String nome;
  private String codigo;
  private String descricao;
  private String localizacao;
  private BigDecimal orcamento;
  private Estado estado;
  private Funcionario responsavel;

  private Departamento(
      ExternalID idDepartamento,
      String nome,
      String codigo,
      String descricao,
      String localizacao,
      BigDecimal orcamento,
      Estado estado,
      Funcionario responsavel
  ) {
    this.idDepartamento = idDepartamento;
    this.nome = nome;
    this.codigo = codigo;
    this.descricao = descricao;
    this.localizacao = localizacao;
    this.orcamento = orcamento;
    this.estado = estado;
    this.responsavel = responsavel;
  }

  public static Departamento criarNovo(String nome, String codigo,String descricao, String localizacao,
                                       BigDecimal orcamento, Funcionario responsavel) {
    return new Departamento(
        ExternalID.gerarNovo(),
        nome,
        codigo,
        descricao,
        localizacao,
        orcamento,
        Estado.A,
        responsavel
    );
  }

  public static Departamento reconstruir(ExternalID idDepartamento, String nome,String codigo,
                                         String descricao, String localizacao,
                                         BigDecimal orcamento, Estado estado,
                                         Funcionario responsavel) {
    return new Departamento(idDepartamento, nome, codigo, descricao, localizacao, orcamento, estado, responsavel);
  }

  public void atualizar(String nome,String codigo, String descricao, String localizacao,
                        BigDecimal orcamento, Funcionario responsavel) {
    this.nome = nome;
    this.descricao = descricao;
    this.localizacao = localizacao;
    this.orcamento = orcamento;
    this.responsavel = responsavel;
    this.codigo = codigo;
  }

  public void desativar() {
    this.estado = Estado.I;
  }

  public void ativar() {
    this.estado = Estado.A;
  }

  public boolean isAtivo() {
    return this.estado == Estado.A;
  }
}
