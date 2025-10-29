package cv.igrp.RH_Service.shared.domain.valueobject;

public class Nib {

  private final String valor;

  private Nib(String valor) {
    if (valor == null || valor.trim().isEmpty()) {
      throw new IllegalArgumentException("NIB não pode ser nulo ou vazio.");
    }
    // Pode validar tamanho ou formato do NIB aqui
    this.valor = valor;
  }

  public static Nib from(String valor) {
    return new Nib(valor);
  }

  public String getValor() {
    return valor;
  }

  @Override
  public String toString() {
    return valor;
  }
}
