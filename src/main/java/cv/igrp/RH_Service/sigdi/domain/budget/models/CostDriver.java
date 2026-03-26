package cv.igrp.RH_Service.sigdi.domain.budget.models;

import cv.igrp.RH_Service.sigdi.application.constants.CostDriverType;
import cv.igrp.RH_Service.sigdi.domain.budget.valueobject.CostDriverId;
import cv.igrp.RH_Service.sigdi.domain.budget.valueobject.CostDriverParams;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
public class CostDriver {

  private final CostDriverId id;
  private final CostDriverType driverType;
  private final CostDriverParams parameters;
  private final LocalDate validFrom;
  private final LocalDate validUntil;

  private CostDriver(CostDriverId id, CostDriverType driverType, CostDriverParams parameters,
                     LocalDate validFrom, LocalDate validUntil) {
    if (driverType == null) {
      throw new IllegalArgumentException("driverType é obrigatório");
    }
    if (parameters == null) {
      throw new IllegalArgumentException("parameters é obrigatório");
    }
    if (validFrom == null) {
      throw new IllegalArgumentException("validFrom é obrigatório");
    }
    if (validUntil != null && validUntil.isBefore(validFrom)) {
      throw new IllegalArgumentException("validUntil não pode ser anterior ao validFrom");
    }
    this.id = id;
    this.driverType = driverType;
    this.parameters = parameters;
    this.validFrom = validFrom;
    this.validUntil = validUntil;
  }

  public static CostDriver create(CostDriverType driverType, CostDriverParams parameters,
                                  LocalDate validFrom, LocalDate validUntil) {
    return new CostDriver(CostDriverId.gerarNovo(), driverType, parameters, validFrom, validUntil);
  }

  public static CostDriver reconstruct(CostDriverId id, CostDriverType driverType,
                                       CostDriverParams parameters, LocalDate validFrom,
                                       LocalDate validUntil) {
    return new CostDriver(id, driverType, parameters, validFrom, validUntil);
  }

  // Regras de negócio
  public boolean isValid() {
    LocalDate today = LocalDate.now();
    return !today.isBefore(validFrom) &&
        (validUntil == null || !today.isAfter(validUntil));
  }

  public BigDecimal simulate(int quantity) {
    if (!isValid()) {
      throw new IllegalStateException("CostDriver não está vigente: " + id.getStringValor());
    }
    return parameters.calculate(quantity);
  }
}

