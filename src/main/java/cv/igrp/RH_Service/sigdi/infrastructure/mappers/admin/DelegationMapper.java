package cv.igrp.RH_Service.sigdi.infrastructure.mappers.admin;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.UserDelegationEntity;
import cv.igrp.RH_Service.sigdi.domain.admin.models.Delegation;
import cv.igrp.RH_Service.sigdi.domain.admin.valueobject.DelegationId;
import org.springframework.stereotype.Component;

@Component
public class DelegationMapper {

  public Delegation toDomain(UserDelegationEntity entity) {
    if (entity == null) return null;

    return Delegation.reconstruct(
        DelegationId.from(entity.getId()),
        entity.getInstitutionId(),
        entity.getDelegatorId(),
        entity.getDelegateId(),
        entity.getScope(),
        entity.getStartDate(),
        entity.getEndDate(),
        entity.getReason(),
        entity.isActive()
    );
  }

  public UserDelegationEntity toEntity(Delegation domain) {
    if (domain == null) return null;

    UserDelegationEntity entity = new UserDelegationEntity();
    entity.setId(domain.getId().getValor().getValor());
    entity.setInstitutionId(domain.getInstitutionId());
    entity.setDelegatorId(domain.getDelegatorId());
    entity.setDelegateId(domain.getDelegateId());
    entity.setScope(domain.getScope());
    entity.setStartDate(domain.getStartDate());
    entity.setEndDate(domain.getEndDate());
    entity.setReason(domain.getReason());
    entity.setActive(domain.isActive());
    return entity;
  }
}
