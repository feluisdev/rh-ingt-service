package cv.igrp.RH_Service.funcionarios.domain.models.read;

import cv.igrp.RH_Service.shared.application.constants.Estado;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DepartamentoRead {

  /**
   * Read-only projection of Departamento for query and display purposes,
   * including related data not present in the core domain model.
   */

  private ExternalID idDepartamento;
  private String nome;
  private String codigo;
  private String descricao;
  private String localizacao;
  private BigDecimal orcamento;
  private Estado estado;
  private ExternalID responsavelID;
  private String nomeResponsavel;

  public DepartamentoRead() {
  }

  public DepartamentoRead(
      ExternalID idDepartamento,
      String nome,
      String codigo,
      String descricao,
      String localizacao,
      BigDecimal orcamento,
      Estado estado,
      ExternalID responsavelID,
      String nomeResponsavel
  ) {
    this.idDepartamento = idDepartamento;
    this.nome = nome;
    this.codigo = codigo;
    this.descricao = descricao;
    this.localizacao = localizacao;
    this.orcamento = orcamento;
    this.estado = estado;
    this.responsavelID = responsavelID;
    this.nomeResponsavel = nomeResponsavel;
  }

}
