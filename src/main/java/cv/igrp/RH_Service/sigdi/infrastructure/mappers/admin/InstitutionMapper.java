package cv.igrp.RH_Service.sigdi.infrastructure.mappers.admin;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.InstitutionEntity;
import cv.igrp.RH_Service.sigdi.domain.admin.models.Institution;
import cv.igrp.RH_Service.sigdi.domain.admin.valueobject.InstitutionId;
import org.springframework.stereotype.Component;

@Component
public class InstitutionMapper {

  public Institution toDomain(InstitutionEntity entity) {
    if (entity == null) return null;

    return Institution.reconstruct(
        InstitutionId.from(entity.getId()),
        entity.getCode(),
        entity.getName(),
        entity.getType(),
        entity.isActive(),
        entity.getDeactivatedAt(),
        entity.getContactEmail()
    );
  }

  public InstitutionEntity toEntity(Institution domain) {
    if (domain == null) return null;

    InstitutionEntity entity = new InstitutionEntity();
    entity.setId(domain.getId().getValor().getValor());
    entity.setCode(domain.getCode());
    entity.setName(domain.getName());
    entity.setType(domain.getType());
    entity.setActive(domain.isActive());
    entity.setDeactivatedAt(domain.getDeactivatedAt());
    entity.setContactEmail(domain.getContactEmail());
    return entity;
  }
}
