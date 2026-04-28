package cv.igrp.RH_Service.sigdi.domain.budget.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import lombok.Getter;

import java.util.UUID;

@Getter
public class CostDriverId {

  private final ExternalID valor;

  private CostDriverId(ExternalID valor) {
    if (valor == null) {
      throw new IllegalArgumentException("CostDriverId não pode ser nulo");
    }
    this.valor = valor;
  }

  public static CostDriverId from(ExternalID externalID) {
    return new CostDriverId(externalID);
  }

  public static CostDriverId from(String uuidString) {
    return new CostDriverId(ExternalID.from(uuidString));
  }

  public static CostDriverId from(UUID uuid) {
    return new CostDriverId(ExternalID.from(uuid));
  }

  public static CostDriverId gerarNovo() {
    return new CostDriverId(ExternalID.gerarNovo());
  }

  public String getStringValor() {
    return valor.getStringValor();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CostDriverId that)) return false;
    return valor.equals(that.valor);
  }

  @Override
  public int hashCode() {
    return valor.hashCode();
  }
}
