package cv.igrp.RH_Service.sigdi.domain.tatical.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import lombok.Getter;

import java.util.UUID;

@Getter
public class TacticalActivityId {

  private final ExternalID valor;

  private TacticalActivityId(ExternalID valor) {
    if (valor == null) {
      throw new IllegalArgumentException("TacticalActivityId não pode ser nulo");
    }
    this.valor = valor;
  }

  public static TacticalActivityId from(ExternalID externalID) {
    return new TacticalActivityId(externalID);
  }

  public static TacticalActivityId from(String uuidString) {
    return new TacticalActivityId(ExternalID.from(uuidString));
  }

  public static TacticalActivityId from(UUID uuid) {
    return new TacticalActivityId(ExternalID.from(uuid));
  }

  public static TacticalActivityId gerarNovo() {
    return new TacticalActivityId(ExternalID.gerarNovo());
  }

  public String getStringValor() {
    return valor.getStringValor();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TacticalActivityId that)) return false;
    return valor.equals(that.valor);
  }

  @Override
  public int hashCode() {
    return valor.hashCode();
  }
}
