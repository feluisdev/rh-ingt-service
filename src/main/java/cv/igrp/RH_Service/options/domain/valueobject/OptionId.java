package cv.igrp.RH_Service.options.domain.valueobject;


import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.Objects;

public class OptionId {

  private final ExternalID identificador;

  private OptionId(ExternalID identificador) {
    if (identificador == null) {
      throw new IllegalArgumentException("OptionId não pode ser nulo.");
    }
    this.identificador = identificador;
  }

  public static OptionId from(ExternalID identificador) {
    return new OptionId(identificador);
  }

  public static OptionId from(String uuid) {
    return new OptionId(ExternalID.from(uuid));
  }

  public static OptionId from(java.util.UUID uuid) {
    return new OptionId(ExternalID.from(uuid));
  }

  public static OptionId gerarNovo() {
    return new OptionId(ExternalID.gerarNovo());
  }

  public ExternalID getIdentificador() {
    return identificador;
  }

  public String getValorComoString() {
    return identificador.getStringValor();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof OptionId optionId)) return false;
    return Objects.equals(identificador, optionId.identificador);
  }

  @Override
  public int hashCode() {
    return Objects.hash(identificador);
  }

  @Override
  public String toString() {
    return getValorComoString();
  }
}
