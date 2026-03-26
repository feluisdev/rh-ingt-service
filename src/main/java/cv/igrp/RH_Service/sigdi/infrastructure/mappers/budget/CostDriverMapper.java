package cv.igrp.RH_Service.sigdi.infrastructure.mappers.budget;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.CostDriverEntity;
import cv.igrp.RH_Service.sigdi.application.constants.CostDriverType;
import cv.igrp.RH_Service.sigdi.domain.budget.models.CostDriver;
import cv.igrp.RH_Service.sigdi.domain.budget.valueobject.CostDriverId;
import cv.igrp.RH_Service.sigdi.domain.budget.valueobject.CostDriverParams;
import org.springframework.stereotype.Component;

@Component
public class CostDriverMapper {

  public CostDriver toDomain(CostDriverEntity entity) {
    if (entity == null) return null;

    return CostDriver.reconstruct(
        CostDriverId.from(entity.getId()),
        CostDriverType.fromCodeOrThrow(entity.getDriverType()),
        CostDriverParams.fromJson(entity.getDriverType(), entity.getParameters()),
        entity.getValidFrom(),
        entity.getValidUntil()
    );
  }

  public CostDriverEntity toEntity(CostDriver domain) {
    if (domain == null) return null;

    CostDriverEntity entity = new CostDriverEntity();
    entity.setId(domain.getId().getValor().getValor());
    entity.setDriverType(domain.getDriverType().getCode());
    entity.setParameters(domain.getParameters().toJson());
    entity.setValidFrom(domain.getValidFrom());
    entity.setValidUntil(domain.getValidUntil());
    return entity;
  }
}

