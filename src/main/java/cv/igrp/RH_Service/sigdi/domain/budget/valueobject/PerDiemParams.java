package cv.igrp.RH_Service.sigdi.domain.budget.valueobject;

import com.fasterxml.jackson.databind.ObjectMapper;
import cv.igrp.RH_Service.sigdi.application.constants.CostDriverType;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
public class PerDiemParams extends CostDriverParams{

  private static final ObjectMapper mapper = new ObjectMapper();

  private final String island;       // ex: "SAL", "SANTIAGO"
  private final String level;        // ex: "TECNICO", "DIRIGENTE", "MEMBRO_GOV"
  private final BigDecimal dailyRate; // valor diário

  private PerDiemParams(String island, String level, BigDecimal dailyRate) {
    if (island == null || island.isBlank()) {
      throw new IllegalArgumentException("island é obrigatório para PER_DIEM");
    }
    if (level == null || level.isBlank()) {
      throw new IllegalArgumentException("level é obrigatório para PER_DIEM");
    }
    if (dailyRate == null || dailyRate.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("dailyRate deve ser maior que zero");
    }
    this.island = island;
    this.level = level;
    this.dailyRate = dailyRate;
  }

  public static PerDiemParams of(String island, String level, BigDecimal dailyRate) {
    return new PerDiemParams(island, level, dailyRate);
  }

  public static PerDiemParams fromJson(String json) {
    try {
      Map<String, Object> map = mapper.readValue(json, Map.class);
      return new PerDiemParams(
          (String) map.get("island"),
          (String) map.get("level"),
          new BigDecimal(map.get("value").toString())
      );
    } catch (Exception e) {
      throw new IllegalArgumentException("JSON inválido para PerDiemParams: " + json, e);
    }
  }

  @Override
  public CostDriverType getType() {
    return CostDriverType.PER_DIEM;
  }

  // Calcula custo total: dailyRate × dias
  @Override
  public BigDecimal calculate(int days) {
    if (days <= 0) throw new IllegalArgumentException("days deve ser maior que zero");
    return dailyRate.multiply(BigDecimal.valueOf(days));
  }

  @Override
  public String toJson() {
    try {
      return mapper.writeValueAsString(Map.of(
          "island", island,
          "level", level,
          "value", dailyRate
      ));
    } catch (Exception e) {
      throw new IllegalStateException("Erro ao serializar PerDiemParams", e);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PerDiemParams that)) return false;
    return island.equals(that.island) && level.equals(that.level) &&
        dailyRate.compareTo(that.dailyRate) == 0;
  }

  // This was NOT a contract violation: dailyRate was omitted from the hash entirely, so equal
  // objects already had equal hashes. It is included here for hash quality -- two per diems for
  // the same island and level but different daily rates were collapsing into one bucket. The
  // amount is canonicalised because equals compares it with compareTo, which ignores scale.
  // Fase 106, mesma varredura de Budget.java e FuelParams.java.
  @Override
  public int hashCode() {
    return 31 * (31 * island.hashCode() + level.hashCode())
        + dailyRate.stripTrailingZeros().hashCode();
  }
}
