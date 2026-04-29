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
  private final String currency;
  private final String legalReference;

  private CostDriver(CostDriverId id, CostDriverType driverType, CostDriverParams parameters,
                     LocalDate validFrom, String currency, String legalReference) {
    if (driverType == null) throw new IllegalArgumentException("driverType é obrigatório");
    if (parameters == null) throw new IllegalArgumentException("parameters é obrigatório");
    if (validFrom == null) throw new IllegalArgumentException("validFrom é obrigatório");
    if (currency == null || currency.isBlank()) throw new IllegalArgumentException("currency é obrigatório");
    this.id = id;
    this.driverType = driverType;
    this.parameters = parameters;
    this.validFrom = validFrom;
    this.currency = currency;
    this.legalReference = legalReference;
  }

  public static CostDriver create(CostDriverType driverType, CostDriverParams parameters,
                                  LocalDate validFrom, String currency, String legalReference) {
    return new CostDriver(CostDriverId.gerarNovo(), driverType, parameters, validFrom,
        currency != null ? currency : "CVE", legalReference);
  }

  public static CostDriver reconstruct(CostDriverId id, CostDriverType driverType,
                                       CostDriverParams parameters, LocalDate validFrom,
                                       String currency, String legalReference) {
    return new CostDriver(id, driverType, parameters, validFrom,
        currency != null ? currency : "CVE", legalReference);
  }

  public CostDriver update(CostDriverParams parameters, LocalDate validFrom, String currency, String legalReference) {
    return new CostDriver(
        this.id,
        this.driverType,
        parameters != null ? parameters : this.parameters,
        validFrom != null ? validFrom : this.validFrom,
        (currency != null && !currency.isBlank()) ? currency : this.currency,
        legalReference
    );
  }

  public boolean isValid() {
    return !LocalDate.now().isBefore(validFrom);
  }

  public BigDecimal simulate(int quantity) {
    if (!isValid()) {
      throw new IllegalStateException("CostDriver não está vigente: " + id.getStringValor());
    }
    return parameters.calculate(quantity);
  }
}
