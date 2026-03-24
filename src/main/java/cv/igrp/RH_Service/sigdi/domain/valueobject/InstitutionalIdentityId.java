package cv.igrp.RH_Service.sigdi.domain.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import lombok.Getter;

import java.util.UUID;

@Getter
public class InstitutionalIdentityId {

  private final ExternalID valor;

  private InstitutionalIdentityId(ExternalID valor) {
    if (valor == null) {
      throw new IllegalArgumentException("InstitutionalIdentityId não pode ser nulo");
    }
    this.valor = valor;
  }

  public static InstitutionalIdentityId from(UUID uuid) {
    return new InstitutionalIdentityId(ExternalID.from(uuid));
  }

  // Factory a partir de String UUID
  public static InstitutionalIdentityId from(String uuidString) {
    return new InstitutionalIdentityId(ExternalID.from(uuidString));
  }

  // Factory para gerar novo ID
  public static InstitutionalIdentityId gerarNovo() {
    return new InstitutionalIdentityId(ExternalID.gerarNovo());
  }

  public String getStringValor() {
    return valor.getStringValor();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof InstitutionalIdentityId that)) return false;
    return valor.equals(that.valor);
  }

  @Override
  public int hashCode() {
    return valor.hashCode();
  }
}
