package cv.igrp.RH_Service.sigdi.infrastructure.mappers;

import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.InstitutionalIdentityEntity;
import cv.igrp.RH_Service.sigdi.domain.models.InstitutionalIdentity;
import cv.igrp.RH_Service.sigdi.domain.valueobject.InstitutionalIdentityId;
import cv.igrp.RH_Service.sigdi.domain.valueobject.InstitutionalValues;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class InstitutionalIdentityMapper {


  public InstitutionalIdentity toDomain(InstitutionalIdentityEntity entity) {
    if (entity == null) return null;

    return InstitutionalIdentity.reconstruct(
        InstitutionalIdentityId.from(entity.getId()),
        entity.getCycleYear(),
        entity.getMission(),
        entity.getVision(),
        InstitutionalValues.of(entity.getValuesJson()),  // String JSON → VO
        entity.getVersionComment(),
        entity.isActive(),
        new ArrayList<>() // goals carregados separadamente (evitar N+1)
    );
  }


  public InstitutionalIdentityEntity toEntity(InstitutionalIdentity domain) {
    if (domain == null) return null;

    InstitutionalIdentityEntity entity = new InstitutionalIdentityEntity();
    entity.setId(domain.getId().getValor().getValor());
    entity.setCycleYear(domain.getCycleYear());
    entity.setMission(domain.getMission());
    entity.setVision(domain.getVision());
    entity.setValuesJson(domain.getValues().toJson());     // VO → String JSON
    entity.setVersionComment(domain.getVersionComment());
    entity.setActive(domain.isActive());
    return entity;
  }
}
