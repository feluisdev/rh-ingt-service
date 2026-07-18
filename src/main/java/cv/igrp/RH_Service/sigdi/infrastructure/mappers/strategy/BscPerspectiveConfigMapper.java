package cv.igrp.RH_Service.sigdi.infrastructure.mappers.strategy;

import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.BscPerspectiveConfigEntity;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.BscPerspectiveConfig;
import org.springframework.stereotype.Component;

@Component
public class BscPerspectiveConfigMapper {

  public BscPerspectiveConfig toDomain(BscPerspectiveConfigEntity entity) {
    if (entity == null) return null;

    return BscPerspectiveConfig.reconstruct(
        entity.getId(),
        entity.getCode(),
        entity.getLabel(),
        entity.getDisplayOrder()
    );
  }

  public BscPerspectiveConfigEntity toEntity(BscPerspectiveConfig domain) {
    if (domain == null) return null;

    BscPerspectiveConfigEntity entity = new BscPerspectiveConfigEntity();
    entity.setId(domain.getId());
    entity.setCode(domain.getCode());
    entity.setLabel(domain.getLabel());
    entity.setDisplayOrder(domain.getDisplayOrder());
    return entity;
  }
}
