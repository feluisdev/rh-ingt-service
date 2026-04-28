package cv.igrp.RH_Service.sigdi.domain.shared.valueobject;

import lombok.Getter;

import java.util.regex.Pattern;

@Getter
public class EconomicClassifier {

  // Padrão hierárquico ex: 02.02.01
  private static final Pattern PATTERN = Pattern.compile("^\\d{2}(\\.\\d{2}){1,3}$");

  private final String code;

  private EconomicClassifier(String code) {
    if (code == null || code.isBlank()) {
      throw new IllegalArgumentException("EconomicClassifier não pode ser nulo ou vazio");
    }
    String trimmed = code.trim();
    if (!PATTERN.matcher(trimmed).matches()) {
      throw new IllegalArgumentException(
          "EconomicClassifier inválido: '" + trimmed + "'. Formato esperado: 02.02.01");
    }
    this.code = trimmed;
  }

  public static EconomicClassifier of(String code) {
    return new EconomicClassifier(code);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof EconomicClassifier that)) return false;
    return code.equals(that.code);
  }

  @Override
  public int hashCode() {
    return code.hashCode();
  }

  @Override
  public String toString() {
    return code;
  }
}
