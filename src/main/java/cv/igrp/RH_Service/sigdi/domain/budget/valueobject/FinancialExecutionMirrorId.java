package cv.igrp.RH_Service.sigdi.domain.budget.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import lombok.Getter;

import java.util.UUID;

@Getter
public class FinancialExecutionMirrorId {

  private final ExternalID valor;

  private FinancialExecutionMirrorId(ExternalID valor) {
    if (valor == null) {
      throw new IllegalArgumentException("FinancialExecutionMirrorId não pode ser nulo");
    }
    this.valor = valor;
  }

  public static FinancialExecutionMirrorId from(ExternalID externalID) {
    return new FinancialExecutionMirrorId(externalID);
  }

  public static FinancialExecutionMirrorId from(String uuidString) {
    return new FinancialExecutionMirrorId(ExternalID.from(uuidString));
  }

  public static FinancialExecutionMirrorId from(UUID uuid) {
    return new FinancialExecutionMirrorId(ExternalID.from(uuid));
  }

  public static FinancialExecutionMirrorId gerarNovo() {
    return new FinancialExecutionMirrorId(ExternalID.gerarNovo());
  }

  public String getStringValor() {
    return valor.getStringValor();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof FinancialExecutionMirrorId that)) return false;
    return valor.equals(that.valor);
  }

  @Override
  public int hashCode() {
    return valor.hashCode();
  }
}
