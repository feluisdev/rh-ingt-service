package cv.igrp.RH_Service.sigdi.domain.budget.valueobject;

import com.fasterxml.jackson.databind.ObjectMapper;
import cv.igrp.RH_Service.sigdi.application.constants.CostDriverType;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
public class FuelParams extends CostDriverParams {

  private static final ObjectMapper mapper = new ObjectMapper();

  private final BigDecimal pricePerLiter;   // preço por litro (CVE)
  private final BigDecimal consumptionRate; // litros por km

  private FuelParams(BigDecimal pricePerLiter, BigDecimal consumptionRate) {
    if (pricePerLiter == null || pricePerLiter.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("pricePerLiter deve ser maior que zero");
    }
    if (consumptionRate == null || consumptionRate.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("consumptionRate deve ser maior que zero");
    }
    this.pricePerLiter = pricePerLiter;
    this.consumptionRate = consumptionRate;
  }

  public static FuelParams of(BigDecimal pricePerLiter, BigDecimal consumptionRate) {
    return new FuelParams(pricePerLiter, consumptionRate);
  }

  public static FuelParams fromJson(String json) {
    try {
      Map<String, Object> map = mapper.readValue(json, Map.class);
      return new FuelParams(
          new BigDecimal(map.get("pricePerLiter").toString()),
          new BigDecimal(map.get("consumptionRate").toString())
      );
    } catch (Exception e) {
      throw new IllegalArgumentException("JSON inválido para FuelParams: " + json, e);
    }
  }

  @Override
  public CostDriverType getType() {
    return CostDriverType.FUEL;
  }

  // Calcula custo total: pricePerLiter × consumptionRate × km
  @Override
  public BigDecimal calculate(int km) {
    if (km <= 0) throw new IllegalArgumentException("km deve ser maior que zero");
    return pricePerLiter.multiply(consumptionRate).multiply(BigDecimal.valueOf(km));
  }

  @Override
  public String toJson() {
    try {
      return mapper.writeValueAsString(Map.of(
          "pricePerLiter", pricePerLiter,
          "consumptionRate", consumptionRate
      ));
    } catch (Exception e) {
      throw new IllegalStateException("Erro ao serializar FuelParams", e);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof FuelParams that)) return false;
    return pricePerLiter.compareTo(that.pricePerLiter) == 0 &&
        consumptionRate.compareTo(that.consumptionRate) == 0;
  }

  // equals uses compareTo on both amounts (scale-insensitive), so the hash must be computed on a
  // scale-canonical form or 150 and 150.00 land in different buckets while comparing equal. Same
  // contract violation as Budget.java, same fix. FUT-08 / DEB-01, Fase 106.
  @Override
  public int hashCode() {
    return 31 * pricePerLiter.stripTrailingZeros().hashCode()
        + consumptionRate.stripTrailingZeros().hashCode();
  }
}
