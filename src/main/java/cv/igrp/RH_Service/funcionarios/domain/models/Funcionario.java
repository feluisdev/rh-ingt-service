package cv.igrp.RH_Service.funcionarios.domain.models;

import cv.igrp.RH_Service.funcionarios.domain.valueobject.NumSegurado;
import cv.igrp.RH_Service.shared.application.constants.Estado;
import cv.igrp.RH_Service.shared.application.constants.EstadoCivil;
import cv.igrp.RH_Service.shared.application.constants.Sexo;
import cv.igrp.RH_Service.shared.domain.valueobject.Email;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.RH_Service.shared.domain.valueobject.Nib;
import cv.igrp.RH_Service.shared.domain.valueobject.Nif;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public class Funcionario {

  private ExternalID idFuncionario;    // identificador externo
  private String nome;
  private Nif nif;
  private NumSegurado numSegurado;
  private Nib nib;
  private Email email;
  private Estado estado;

  private Sexo sexo;
  private EstadoCivil estadoCivil;
  private String endereco;

  private List<Dependente> dependentes;
  private List<Qualificacao> qualificacoes;
  private List<Contrato> contratos;
  private List<Documento> documentos;


  private Funcionario(ExternalID idFuncionario, String nome, Nif nif,
                      NumSegurado numSegurado, Nib nib, Email email, Estado estado, Sexo sexo,
                      EstadoCivil estadoCivil, String endereco, List<Dependente> dependentes, List<Qualificacao> qualificacoes,
                      List<Contrato> contratos, List<Documento> documentos) {
    this.idFuncionario = idFuncionario;
    this.nome = nome;
    this.nif = nif;
    this.numSegurado = numSegurado;
    this.nib = nib;
    this.email = email;
    this.estado = estado;
    this.sexo = sexo;
    this.estadoCivil = estadoCivil;
    this.endereco = endereco;
    this.dependentes = dependentes != null ? dependentes : new ArrayList<>();
    this.qualificacoes = qualificacoes != null ? qualificacoes : new ArrayList<>();
    this.contratos = contratos != null ? contratos : new ArrayList<>();
    this.documentos = documentos != null ? documentos : new ArrayList<>();

  }

  public static Funcionario criar(String nome, String nifRaw,
                                  String numSeguradoRaw, String nibRaw,
                                  String emailRaw, Sexo sexo,
                                  EstadoCivil estadoCivil, String endereco) {
    Objects.requireNonNull(nome, "Nome é obrigatório");
    Objects.requireNonNull(nifRaw, "NIF é obrigatório");
    Objects.requireNonNull(numSeguradoRaw, "Número de Segurado é obrigatório");
    Objects.requireNonNull(nibRaw, "NIB é obrigatório");
    Objects.requireNonNull(emailRaw, "Email é obrigatório");

    Nif nif = Nif.from(nifRaw);
    NumSegurado numSegurado = NumSegurado.from(numSeguradoRaw);
    Nib nib = Nib.from(nibRaw);
    Email email = Email.from(emailRaw);

    return new Funcionario(ExternalID.gerarNovo(), nome, nif,
        numSegurado, nib, email, Estado.A, sexo, estadoCivil, endereco, null, null, null, null);
  }


  public static Funcionario reconstruir(ExternalID idFuncionario, String nome,
                                        String nifRaw, String numSeguradoRaw, String nibRaw,
                                        String emailRaw, Estado estado, Sexo sexo,
                                        EstadoCivil estadoCivil, String endereco, List<Documento> documentos) {
    Objects.requireNonNull(idFuncionario, "idFuncionario é obrigatório");
    Objects.requireNonNull(estado, "Estado é obrigatório");

    Nif nif = (nifRaw != null) ? Nif.from(nifRaw) : null;
    NumSegurado numSegurado = (numSeguradoRaw != null) ? NumSegurado.from(numSeguradoRaw) : null;
    Nib nib = (nibRaw != null) ? Nib.from(nibRaw) : null;
    Email email = (emailRaw != null) ? Email.from(emailRaw) : null;

    return new Funcionario(idFuncionario, nome, nif,
        numSegurado, nib, email, estado, sexo, estadoCivil, endereco, null, null, null, documentos);
  }

  public static Funcionario reconstruir(ExternalID idFuncionario, String nome,
                                        String nifRaw, String numSeguradoRaw, String nibRaw,
                                        String emailRaw, Estado estado, Sexo sexo,
                                        EstadoCivil estadoCivil, String endereco) {
    Objects.requireNonNull(idFuncionario, "idFuncionario é obrigatório");
    Objects.requireNonNull(estado, "Estado é obrigatório");

    Nif nif = (nifRaw != null) ? Nif.from(nifRaw) : null;
    NumSegurado numSegurado = (numSeguradoRaw != null) ? NumSegurado.from(numSeguradoRaw) : null;
    Nib nib = (nibRaw != null) ? Nib.from(nibRaw) : null;
    Email email = (emailRaw != null) ? Email.from(emailRaw) : null;

    return new Funcionario(idFuncionario, nome, nif,
        numSegurado, nib, email, estado, sexo, estadoCivil, endereco, null, null, null, null);
  }

  public void adicionarDocumento(Documento documento) {
    Objects.requireNonNull(documento, "Documento não pode ser nulo");
    documentos.add(documento);
  }

  public void adicionarOuAtualizarDocumento(Documento documento) {
    Objects.requireNonNull(documento, "Documento não pode ser nulo");

    if(!documentos.isEmpty()) {
      for (Documento existente : documentos) {
        System.out.println("existente : "+existente.getIdDocumento());
        if (existente.getIdDocumento().equals(documento.getIdDocumento())) {
          existente.atualizar(
              documento.getUrl(),
              documento.getObservacao(),
              documento.getTipoDocumento()
          );
          return;
        }
      }
    }

    documentos.add(documento);
  }

  public void atualizar(String nome, String nifRaw, String numSeguradoRaw,
                        String nibRaw, String emailRaw, Sexo sexo,
                        EstadoCivil estadoCivil, String endereco) {
    if (nome == null || nome.trim().isEmpty()) {
      throw new IllegalArgumentException("Nome não pode ser nulo ou vazio.");
    }
    if (nifRaw == null || nifRaw.trim().isEmpty()) {
      throw new IllegalArgumentException("NIF não pode ser nulo ou vazio.");
    }

    if (numSeguradoRaw == null || numSeguradoRaw.trim().isEmpty()) {
      throw new IllegalArgumentException("Número de Segurado não pode ser nulo ou vazio.");
    }

    if (nibRaw == null || nibRaw.trim().isEmpty()) {
      throw new IllegalArgumentException("NIB não pode ser nulo ou vazio.");
    }

    if (emailRaw == null || emailRaw.trim().isEmpty()) {
      throw new IllegalArgumentException("Email não pode ser nulo ou vazio.");
    }

    this.nome = nome;
    this.nif = Nif.from(nifRaw);
    this.numSegurado = NumSegurado.from(numSeguradoRaw);
    this.nib = Nib.from(nibRaw);
    this.email = Email.from(emailRaw);
    this.sexo = sexo;
    this.estadoCivil = estadoCivil;
    this.endereco = endereco;
  }

  public void inativar() {
    this.estado = Estado.I;

    for (Dependente dependente : this.dependentes) {
      dependente.desativar();
    }

    for (Qualificacao qualificacao : this.qualificacoes) {
      qualificacao.desativar();
    }
  }

  public void ativar() {
    this.estado = Estado.A;

    for (Dependente dependente : this.dependentes) {
      dependente.ativar();
    }

    for (Qualificacao qualificacao : this.qualificacoes) {
      qualificacao.ativar();
    }
  }

  public void adicionarDependente(Dependente dependente) {
    Objects.requireNonNull(dependente, "Dependente não pode ser nulo");
    dependentes.add(dependente);
  }

  public void adicionarQualificacao(Qualificacao qualificacao) {
    Objects.requireNonNull(qualificacao, "Qualificacao não pode ser nulo");
    qualificacoes.add(qualificacao);
  }

  public void adicionarContrato(Contrato contrato) {
    Objects.requireNonNull(contrato, "Contrato não pode ser nulo");
    contratos.add(contrato);
  }

  public Dependente getDependenteByExternalId(ExternalID idDependente) {
    return dependentes.stream()
        .filter(d -> d.getIdDependente().equals(idDependente))
        .findFirst()
        .orElse(null);
  }

  public Qualificacao getQualificacaoByExternalId(ExternalID idQualificacao) {
    return qualificacoes.stream()
        .filter(q -> q.getIdQualificacao().equals(idQualificacao))
        .findFirst()
        .orElse(null);
  }

  public Contrato getContratoByExternalId(ExternalID idContrato) {
    return contratos.stream()
        .filter(c -> c.getIdContrato().equals(idContrato))
        .findFirst()
        .orElse(null);
  }


}
