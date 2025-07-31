package cv.igrp.RH_Service.funcionarios.domain.models;

import java.util.UUID;

import cv.igrp.RH_Service.shared.application.constants.Estado;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import lombok.Getter;

@Getter
public class Documento {
  private Integer id;
  private ExternalID externalId;
  private String url;
  private String observacao;
  private String objectoTipo;
  private Integer objectId;
  private Estado estado;

  Documento(Integer id, ExternalID externalId, String url, String observacao, String objectoTipo, Integer objectId, Estado estado) {
    this.id = id;
    this.externalId = externalId;
    this.url = url;
    this.observacao = observacao;
    this.objectoTipo = objectoTipo;
    this.objectId = objectId;
    this.estado = estado;
  }

  public static Documento reconstruir(Integer id, ExternalID externalId, String url, String observacao, String objectoTipo, Integer objectId, Estado estado) {
    return new Documento(id, externalId, url, observacao, objectoTipo, objectId, estado);
  }

  public static Documento criar( String url, String observacao, String objectoTipo, Integer objectId, Estado estado) {
    return new Documento(null, ExternalID.gerarNovo(), url, observacao, objectoTipo, objectId, estado);
  }

  public void atualizar(String url, String observacao, Estado estado) {
    this.url = url;
    this.observacao = observacao;
    this.estado = estado;
  }

  public void atualizar(ExternalID externalId, String url, String observacao, String objectoTipo, Integer objectId, Estado estado) {
    this.externalId = externalId;
    this.url = url;
    this.observacao = observacao;
    this.objectoTipo = objectoTipo;
    this.objectId = objectId;
    this.estado = estado;
  }
  public void ativar() {
    this.estado = Estado.A;
  }

  public void desativar() {
    this.estado = Estado.I;
  }

}
