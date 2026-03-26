package cv.igrp.RH_Service.sigdi.domain.intelligence.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import lombok.Getter;

import java.util.UUID;

@Getter
public class SimulationResultId {

  private final ExternalID valor;

  private SimulationResultId(ExternalID valor) {
    if (valor == null) {
      throw new IllegalArgumentException("SimulationResultId não pode ser nulo");
    }
    this.valor = valor;
  }

  public static SimulationResultId from(ExternalID externalID) {
    return new SimulationResultId(externalID);
  }

  public static SimulationResultId from(UUID uuid) {
    return new SimulationResultId(ExternalID.from(uuid));
  }

  public static SimulationResultId from(String uuidString) {
    return new SimulationResultId(ExternalID.from(uuidString));
  }

  public static SimulationResultId gerarNovo() {
    return new SimulationResultId(ExternalID.gerarNovo());
  }

  public String getStringValor() {
    return valor.getStringValor();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SimulationResultId that)) return false;
    return valor.equals(that.valor);
  }

  @Override
  public int hashCode() {
    return valor.hashCode();
  }
}

