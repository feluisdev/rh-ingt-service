package cv.igrp.RH_Service.shared.domain.valueobject;

public class Nif {
  private final String valor;

  private Nif(String valor) {
    if (valor == null || valor.trim().isEmpty()) {
      throw new IllegalArgumentException("NIF não pode ser nulo ou vazio.");
    }
    // Pode colocar regex específica para validar o formato do NIF
    this.valor = valor;
  }

  public static Nif from(String valor) {
    return new Nif(valor);
  }

  public String getValor() {
    return valor;
  }

  @Override
  public String toString() {
    return valor;
  }
}
