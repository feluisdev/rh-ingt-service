package cv.igrp.RH_Service.funcionarios.domain.valueobject;

public class NumSegurado {

  private final String valor;

  private NumSegurado(String valor) {
    if (valor == null || valor.trim().isEmpty()) {
      throw new IllegalArgumentException("Número de Segurado não pode ser nulo ou vazio.");
    }
    this.valor = valor;
  }

  public static NumSegurado from(String valor) {
    return new NumSegurado(valor);
  }

  public String getValor() {
    return valor;
  }

  @Override
  public String toString() {
    return valor;
  }
}
