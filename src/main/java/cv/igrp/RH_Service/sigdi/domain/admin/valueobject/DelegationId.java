package cv.igrp.RH_Service.sigdi.domain.admin.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import lombok.Getter;

import java.util.UUID;

@Getter
public class DelegationId {

  private final ExternalID valor;

  private DelegationId(ExternalID valor) {
    if (valor == null) {
      throw new IllegalArgumentException("DelegationId não pode ser nulo");
    }
    this.valor = valor;
  }

  public static DelegationId from(ExternalID externalID) {
    return new DelegationId(externalID);
  }

  public static DelegationId from(UUID uuid) {
    return new DelegationId(ExternalID.from(uuid));
  }

  public static DelegationId from(String uuidString) {
    return new DelegationId(ExternalID.from(uuidString));
  }

  public static DelegationId gerarNovo() {
    return new DelegationId(ExternalID.gerarNovo());
  }

  public String getStringValor() {
    return valor.getStringValor();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DelegationId that)) return false;
    return valor.equals(that.valor);
  }

  @Override
  public int hashCode() {
    return valor.hashCode();
  }
}
