package cv.igrp.RH_Service.sigdi.domain.admin.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import lombok.Getter;

import java.util.UUID;

@Getter
public class InstitutionId {

  private final ExternalID valor;

  private InstitutionId(ExternalID valor) {
    if (valor == null) {
      throw new IllegalArgumentException("InstitutionId não pode ser nulo");
    }
    this.valor = valor;
  }

  public static InstitutionId from(ExternalID externalID) {
    return new InstitutionId(externalID);
  }

  public static InstitutionId from(UUID uuid) {
    return new InstitutionId(ExternalID.from(uuid));
  }

  public static InstitutionId from(String uuidString) {
    return new InstitutionId(ExternalID.from(uuidString));
  }

  public static InstitutionId gerarNovo() {
    return new InstitutionId(ExternalID.gerarNovo());
  }

  public String getStringValor() {
    return valor.getStringValor();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof InstitutionId that)) return false;
    return valor.equals(that.valor);
  }

  @Override
  public int hashCode() {
    return valor.hashCode();
  }
}
