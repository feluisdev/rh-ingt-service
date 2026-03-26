package cv.igrp.RH_Service.sigdi.domain.compliance.valueobject;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import lombok.Getter;

import java.util.UUID;

@Getter
public class NotificationLogId {

  private final ExternalID valor;

  private NotificationLogId(ExternalID valor) {
    if (valor == null) {
      throw new IllegalArgumentException("NotificationLogId não pode ser nulo");
    }
    this.valor = valor;
  }

  public static NotificationLogId from(ExternalID externalID) {
    return new NotificationLogId(externalID);
  }

  public static NotificationLogId from(UUID uuid) {
    return new NotificationLogId(ExternalID.from(uuid));
  }

  public static NotificationLogId from(String uuidString) {
    return new NotificationLogId(ExternalID.from(uuidString));
  }

  public static NotificationLogId gerarNovo() {
    return new NotificationLogId(ExternalID.gerarNovo());
  }

  public String getStringValor() {
    return valor.getStringValor();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof NotificationLogId that)) return false;
    return valor.equals(that.valor);
  }

  @Override
  public int hashCode() {
    return valor.hashCode();
  }
}

