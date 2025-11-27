package cv.igrp.RH_Service.funcionarios.domain.models;

import java.util.UUID;

import cv.igrp.RH_Service.shared.application.constants.Estado;
import cv.igrp.RH_Service.shared.application.constants.ObjetoTipo;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import lombok.Getter;

@Getter
public class Documento {

  private ExternalID idDocumento;
  private String url;
  private String observacao;
  private ObjetoTipo objectoTipo;
  private ExternalID objectId;
  private Estado estado;
  private String tipoDocumento;

  Documento(ExternalID idDocumento, String url, String observacao, ObjetoTipo objectoTipo, ExternalID objectId, Estado estado, String tipoDocumento) {
    this.idDocumento = idDocumento;
    this.url = url;
    this.observacao = observacao;
    this.objectoTipo = objectoTipo;
    this.objectId = objectId;
    this.estado = estado;
    this.tipoDocumento = tipoDocumento;
  }

  public static Documento reconstruir(ExternalID idDocumento, String url, String observacao, ObjetoTipo objectoTipo,
                                      ExternalID objectId, Estado estado, String tipoDocumento) {
    return new Documento(idDocumento, url, observacao, objectoTipo, objectId, estado, tipoDocumento);
  }

  public static Documento criar( String url, String observacao, ObjetoTipo objectoTipo, ExternalID objectId, String tipoDocumento) {
    return new Documento(ExternalID.gerarNovo(), url, observacao, objectoTipo, objectId, Estado.A, tipoDocumento);
  }

  public static Documento criar( ExternalID externalId, String url, String observacao, ObjetoTipo objectoTipo, ExternalID objectId, String tipoDocumento) {
    var uuidExternal = externalId!=null ? externalId : ExternalID.gerarNovo();
    return new Documento(uuidExternal, url, observacao, objectoTipo, objectId, Estado.A, tipoDocumento);
  }

  public void atualizar(String url, String observacao, String tipoDocumento) {
    this.url = url;
    this.observacao = observacao;
    this.tipoDocumento = tipoDocumento;
  }

  public void ativar() {
    this.estado = Estado.A;
  }

  public void desativar() {
    this.estado = Estado.I;
  }

}
