package cv.igrp.RH_Service.sigdi.domain.intelligence.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import lombok.Getter;

import java.util.UUID;

@Getter
public class SimulationScenarioId {

  private final ExternalID valor;

  private SimulationScenarioId(ExternalID valor) {
    if (valor == null) {
      throw new IllegalArgumentException("SimulationScenarioId não pode ser nulo");
    }
    this.valor = valor;
  }

  public static SimulationScenarioId from(ExternalID externalID) {
    return new SimulationScenarioId(externalID);
  }

  public static SimulationScenarioId from(UUID uuid) {
    return new SimulationScenarioId(ExternalID.from(uuid));
  }

  public static SimulationScenarioId from(String uuidString) {
    return new SimulationScenarioId(ExternalID.from(uuidString));
  }

  public static SimulationScenarioId gerarNovo() {
    return new SimulationScenarioId(ExternalID.gerarNovo());
  }

  public String getStringValor() {
    return valor.getStringValor();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SimulationScenarioId that)) return false;
    return valor.equals(that.valor);
  }

  @Override
  public int hashCode() {
    return valor.hashCode();
  }
}

