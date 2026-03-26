package cv.igrp.RH_Service.sigdi.domain.budget.valueobject;

import cv.igrp.RH_Service.sigdi.application.constants.CostDriverType;

import java.math.BigDecimal;

/**
 * Base para parâmetros de drivers de custo.
 * Cada subclasse representa um tipo de driver com os seus campos específicos.
 */
public abstract class CostDriverParams {

  public abstract CostDriverType getType();

  public abstract BigDecimal calculate(int quantity);

  public abstract String toJson();

  public static CostDriverParams fromJson(String driverType, String json) {
    CostDriverType type = CostDriverType.fromCodeOrThrow(driverType);
    return switch (type) {
      case PER_DIEM -> PerDiemParams.fromJson(json);
      case FUEL -> FuelParams.fromJson(json);
    };
  }
}
