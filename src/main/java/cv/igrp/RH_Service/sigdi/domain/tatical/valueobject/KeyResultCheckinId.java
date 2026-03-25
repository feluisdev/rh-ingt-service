package cv.igrp.RH_Service.sigdi.domain.tatical.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import lombok.Getter;

import java.util.UUID;

@Getter
public class KeyResultCheckinId {

  private final ExternalID valor;

  private KeyResultCheckinId(ExternalID valor) {
    if (valor == null) {
      throw new IllegalArgumentException("KeyResultCheckinId não pode ser nulo");
    }
    this.valor = valor;
  }

  public static KeyResultCheckinId from(ExternalID externalID) {
    return new KeyResultCheckinId(externalID);
  }

  public static KeyResultCheckinId from(String uuidString) {
    return new KeyResultCheckinId(ExternalID.from(uuidString));
  }

  public static KeyResultCheckinId from(UUID uuid) {
    return new KeyResultCheckinId(ExternalID.from(uuid));
  }

  public static KeyResultCheckinId gerarNovo() {
    return new KeyResultCheckinId(ExternalID.gerarNovo());
  }

  public String getStringValor() {
    return valor.getStringValor();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof KeyResultCheckinId that)) return false;
    return valor.equals(that.valor);
  }

  @Override
  public int hashCode() {
    return valor.hashCode();
  }
}
