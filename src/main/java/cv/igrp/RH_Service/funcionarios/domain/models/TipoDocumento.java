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
  private ExternalID external_id ;
  private String descricao ;
  private String codigo ;
  private Estado estado ;

  public TipoDocumento(){}

  public TipoDocumento( String descricao, String codigo) {
    this.external_id = ExternalID.gerarNovo();
    this.descricao = descricao;
    this.codigo = codigo;
  }

  public static TipoDocumento criar(  String descricao, String codigo) {
    return new TipoDocumento( descricao, codigo);
  }
  public static TipoDocumento reconstruir(Integer id, ExternalID external_id, String descricao, String codigo) {
     Objects.requireNonNull(id, "ID é obrigatório");
    return new TipoDocumento( descricao, codigo);
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
