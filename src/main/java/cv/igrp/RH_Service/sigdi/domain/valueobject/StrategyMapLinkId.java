package cv.igrp.RH_Service.sigdi.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import lombok.Getter;

@Getter
public class StrategyMapLinkId {

  private final ExternalID valor;

  private StrategyMapLinkId(ExternalID valor) {
    if (valor == null) {
      throw IgrpResponseStatusException.badRequest("StrategyMapLinkId não pode ser nulo");
    }
    this.valor = valor;
  }

  // Factory method a partir de ExternalID existente
  public static StrategyMapLinkId from(ExternalID externalID) {
    return new StrategyMapLinkId(externalID);
  }

  // Factory method a partir de String UUID
  public static StrategyMapLinkId from(String uuidString) {
    return new StrategyMapLinkId(ExternalID.from(uuidString));
  }

  // Factory method para gerar novo ID
  public static StrategyMapLinkId gerarNovo() {
    return new StrategyMapLinkId(ExternalID.gerarNovo());
  }

  // Convenience
  public String getStringValor() {
    return valor.getStringValor();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof StrategyMapLinkId that)) return false;
    return valor.equals(that.valor);
  }

  @Override
  public int hashCode() {
    return valor.hashCode();
  }
}
