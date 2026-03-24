package cv.igrp.RH_Service.sigdi.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import lombok.Getter;

import java.util.UUID;

@Getter
public class StrategicGoalId {

  private final ExternalID valor;

  private StrategicGoalId(ExternalID valor) {
    if (valor == null) {
      throw new IllegalArgumentException("StrategicGoalId não pode ser nulo");
    }
    this.valor = valor;
  }


  // Factory method a partir de ExternalID existente
  public static StrategicGoalId from(UUID uuid) {
    return new StrategicGoalId(ExternalID.from(uuid));
  }

  // Factory method a partir de String UUID
  public static StrategicGoalId from(String uuidString) {
    return new StrategicGoalId(ExternalID.from(uuidString));
  }

  // Factory method para gerar novo ID
  public static StrategicGoalId gerarNovo() {
    return new StrategicGoalId(ExternalID.gerarNovo());
  }

  // Convenience
  public String getStringValor() {
    return valor.getStringValor();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof StrategicGoalId that)) return false;
    return valor.equals(that.valor);
  }

  @Override
  public int hashCode() {
    return valor.hashCode();
  }
}
