package cv.igrp.RH_Service.sigdi.domain.valueobject;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;

public class InstitutionalValues {

  private final List<String> valores;

  private InstitutionalValues(List<String> valores) {
    if (valores == null || valores.isEmpty()) {
      throw new IllegalArgumentException("É obrigatório definir pelo menos um valor institucional.");
    }

    List<String> limpos = valores.stream()
        .map(String::trim)
        .filter(v -> !v.isEmpty())
        .distinct() // sem duplicados
        .toList();

    if (limpos.isEmpty()) {
      throw new IllegalArgumentException("Os valores institucionais não podem ser todos vazios.");
    }

    this.valores = limpos;
  }

  public static InstitutionalValues of(List<String> valores) {
    return new InstitutionalValues(valores);
  }

  public static InstitutionalValues of(String valoresJson) {
    // Para reconstruir a partir do que está guardado no banco (String JSON)
    if (valoresJson == null || valoresJson.isBlank()) {
      throw new IllegalArgumentException("valoresJson não pode ser nulo ou vazio.");
    }
    try {
      com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
      List<String> lista = mapper.readValue(valoresJson,
          new TypeReference<List<String>>() {
          });
      return new InstitutionalValues(lista);
    } catch (Exception e) {
      throw new IllegalArgumentException("valoresJson inválido: " + valoresJson, e);
    }
  }

  public List<String> getValores() {
    return Collections.unmodifiableList(valores);
  }

  // Para persistir no banco como String JSON
  public String toJson() {
    try {
      ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
      return mapper.writeValueAsString(valores);
    } catch (Exception e) {
      throw new IllegalStateException("Erro ao serializar InstitutionalValues", e);
    }
  }

  public int count() {
    return valores.size();
  }

  public boolean contains(String valor) {
    return valores.stream().anyMatch(v -> v.equalsIgnoreCase(valor));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof InstitutionalValues that)) return false;
    return valores.equals(that.valores);
  }

  @Override
  public int hashCode() {
    return valores.hashCode();
  }

  @Override
  public String toString() {
    return valores.toString();
  }
}
