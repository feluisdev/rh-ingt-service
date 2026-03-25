package cv.igrp.RH_Service.sigdi.domain.tatical.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import lombok.Getter;

import java.util.UUID;

@Getter
public class KeyResultId {

  private final ExternalID valor;

  private KeyResultId(ExternalID valor) {
    if (valor == null) {
      throw new IllegalArgumentException("KeyResultId não pode ser nulo");
    }
    this.valor = valor;
  }

  public static KeyResultId from(ExternalID externalID) {
    return new KeyResultId(externalID);
  }

  public static KeyResultId from(String uuidString) {
    return new KeyResultId(ExternalID.from(uuidString));
  }

  public static KeyResultId from(UUID uuid) {
    return new KeyResultId(ExternalID.from(uuid));
  }

  public static KeyResultId gerarNovo() {
    return new KeyResultId(ExternalID.gerarNovo());
  }

  public String getStringValor() {
    return valor.getStringValor();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof KeyResultId that)) return false;
    return valor.equals(that.valor);
  }

  @Override
  public int hashCode() {
    return valor.hashCode();
  }
}
