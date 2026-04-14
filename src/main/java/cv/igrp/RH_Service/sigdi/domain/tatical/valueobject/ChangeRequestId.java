package cv.igrp.RH_Service.sigdi.domain.tatical.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import lombok.Getter;

import java.util.UUID;

@Getter
public class ChangeRequestId {

  private final ExternalID valor;

  private ChangeRequestId(ExternalID valor) {
    if (valor == null) {
      throw new IllegalArgumentException("ChangeRequestId não pode ser nulo");
    }
    this.valor = valor;
  }

  public static ChangeRequestId from(ExternalID externalID) {
    return new ChangeRequestId(externalID);
  }

  public static ChangeRequestId from(String uuidString) {
    return new ChangeRequestId(ExternalID.from(uuidString));
  }

  public static ChangeRequestId from(UUID uuid) {
    return new ChangeRequestId(ExternalID.from(uuid));
  }

  public static ChangeRequestId gerarNovo() {
    return new ChangeRequestId(ExternalID.gerarNovo());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ChangeRequestId that)) return false;
    return valor.equals(that.valor);
  }

  @Override
  public int hashCode() {
    return valor.hashCode();
  }
}
