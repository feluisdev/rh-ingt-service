package cv.igrp.RH_Service.sigdi.domain.compliance.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import lombok.Getter;

import java.util.UUID;

@Getter
public class SiadapEvaluationId {

  private final ExternalID valor;

  private SiadapEvaluationId(ExternalID valor) {
    if (valor == null) {
      throw new IllegalArgumentException("SiadapEvaluationId não pode ser nulo");
    }
    this.valor = valor;
  }

  public static SiadapEvaluationId from(ExternalID externalID) {
    return new SiadapEvaluationId(externalID);
  }

  public static SiadapEvaluationId from(UUID uuid) {
    return new SiadapEvaluationId(ExternalID.from(uuid));
  }

  public static SiadapEvaluationId from(String uuidString) {
    return new SiadapEvaluationId(ExternalID.from(uuidString));
  }

  public static SiadapEvaluationId gerarNovo() {
    return new SiadapEvaluationId(ExternalID.gerarNovo());
  }

  public String getStringValor() {
    return valor.getStringValor();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SiadapEvaluationId that)) return false;
    return valor.equals(that.valor);
  }

  @Override
  public int hashCode() {
    return valor.hashCode();
  }
}

