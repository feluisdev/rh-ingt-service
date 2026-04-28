package cv.igrp.RH_Service.sigdi.domain.tatical.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import lombok.Getter;

import java.util.UUID;

@Getter
public class OkrId {

  private final ExternalID valor;

  private OkrId(ExternalID valor) {
    if (valor == null) {
      throw new IllegalArgumentException("OkrId não pode ser nulo");
    }
    this.valor = valor;
  }

  public static OkrId from(ExternalID externalID) {
    return new OkrId(externalID);
  }

  public static OkrId from(String uuidString) {
    return new OkrId(ExternalID.from(uuidString));
  }

  public static OkrId from(UUID uuid) {
    return new OkrId(ExternalID.from(uuid));
  }

  public static OkrId gerarNovo() {
    return new OkrId(ExternalID.gerarNovo());
  }

  public String getStringValor() {
    return valor.getStringValor();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof OkrId that)) return false;
    return valor.equals(that.valor);
  }

  @Override
  public int hashCode() {
    return valor.hashCode();
  }
}