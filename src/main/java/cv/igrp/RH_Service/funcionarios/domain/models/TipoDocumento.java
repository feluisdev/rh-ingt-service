package cv.igrp.RH_Service.funcionarios.domain.models;

import cv.igrp.RH_Service.shared.application.constants.Estado;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
public class TipoDocumento {
  private Integer id ;
  private ExternalID externalId ;
  private String descricao ;
  private String codigo ;
  private Estado estado ;

  private TipoDocumento(){}

  private TipoDocumento(Integer id, ExternalID externalId, String descricao, String codigo, Estado estado) {
    this.id = id;
    this.externalId = externalId;
    this.descricao = descricao;
    this.codigo = codigo;
    this.estado = estado;
  }

  public static TipoDocumento criar( String descricao, String codigo) {
    Objects.requireNonNull(codigo,"codigo nao pode ser nulo");
    return new TipoDocumento(null, ExternalID.gerarNovo(), descricao, codigo, Estado.A);
  }
  public static TipoDocumento reconstruir(Integer id, ExternalID externalId, String descricao, String codigo, Estado estado) {

    return new TipoDocumento( id,externalId, descricao, codigo, estado);
  }

  public void atualizar( String descricao, String codigo) {

    this.descricao = descricao;
    this.codigo = codigo;
  }

  public void inativar() {
    this.estado = Estado.I;
  }

  public void ativar() {
    this.estado = Estado.A;

  }




}
