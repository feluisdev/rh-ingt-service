package cv.igrp.RH_Service.shared.domain.valueobject;

import java.util.regex.Pattern;

public class Email {

  private static final Pattern EMAIL_REGEX = Pattern.compile(
      "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
  );

  private final String valor;

  private Email(String valor) {
    if (valor == null || valor.trim().isEmpty()) {
      throw new IllegalArgumentException("Email não pode ser nulo ou vazio.");
    }
    if (!EMAIL_REGEX.matcher(valor).matches()) {
      throw new IllegalArgumentException("Email inválido: " + valor);
    }
    this.valor = valor;
  }

  public static Email from(String valor) {
    return new Email(valor);
  }


  public String getValor() {
    return valor;
  }

  @Override
  public String toString() {
    return valor;
  }

}
