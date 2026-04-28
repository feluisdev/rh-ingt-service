package cv.igrp.RH_Service.sigdi.infrastructure.mappers.strategy;

import cv.igrp.RH_Service.shared.application.constants.Estado;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.InstitutionalIdentityEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.StrategicGoalEntity;
import cv.igrp.RH_Service.sigdi.application.constants.StrategicGoalsPerspective;
import cv.igrp.RH_Service.sigdi.application.dto.StategicGoalResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.StategicGoalSumaryDTO;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.StrategicGoal;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.InstitutionalIdentityId;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StrategicGoalMapper {

  public StrategicGoal toDomain(StrategicGoalEntity entity) {
    if (entity == null) return null;

    return StrategicGoal.reconstruct(
        StrategicGoalId.from(entity.getId()),
        entity.getInstitutionId(),
        InstitutionalIdentityId.from(entity.getIdentityId().getId()),
        entity.getTitle(),
        StrategicGoalsPerspective.fromCodeOrThrow(entity.getPerspective()),
        entity.getWeight(),
        Estado.fromCodeOrThrow(entity.getStatus()),
        entity.getDescription(),
        entity.getPositionX(),
        entity.getPositionY()
    );
  }

  public StategicGoalResponseDTO toResponse(StrategicGoal domain) {
    if (domain == null) return null;
    StategicGoalResponseDTO dto = new StategicGoalResponseDTO();
    dto.setId(domain.getId().getValor().getValor());
    dto.setIdentityId(domain.getIdentityId().getValor().getValor());
    dto.setPerspective(domain.getPerspective().getCode());
    dto.setPerspectiveDesc(domain.getPerspective().getDescription());
    dto.setWeight(domain.getWeight());
    dto.setTitle(domain.getTitle());
    dto.setDescription(domain.getDescription());
    dto.setStatus(domain.getStatus().getCode());
    dto.setStatusDesc(domain.getStatus().getDescription());
    dto.setProgress(0.0);
    dto.setLinkedActivities(0);
    return dto;
  }

  public StategicGoalSumaryDTO toSummary(StrategicGoal domain) {
    if (domain == null) return null;
    StategicGoalSumaryDTO dto = new StategicGoalSumaryDTO();
    dto.setId(domain.getId().getValor().getValor());
    dto.setTitle(domain.getTitle());
    dto.setPerspective(domain.getPerspective().getCode());
    dto.setWeight(domain.getWeight());
    dto.setStatus(domain.getStatus().getCode());
    dto.setStatusDes(domain.getStatus().getDescription());
    dto.setProgress(0.0);
    dto.setLinkedActivities(0);
    return dto;
  }

  public StrategicGoalEntity toEntity(StrategicGoal domain) {
    if (domain == null) return null;

    StrategicGoalEntity entity = new StrategicGoalEntity();
    entity.setId(domain.getId().getValor().getValor());
    entity.setInstitutionId(domain.getInstitutionId());
    entity.setTitle(domain.getTitle());
    entity.setPerspective(domain.getPerspective().getCode());
    entity.setWeight(domain.getWeight());
    entity.setStatus(domain.getStatus().getCode());
    entity.setDescription(domain.getDescription());
    entity.setPositionX(domain.getPositionX());
    entity.setPositionY(domain.getPositionY());

    InstitutionalIdentityEntity identityRef = new InstitutionalIdentityEntity();
    identityRef.setId(domain.getIdentityId().getValor().getValor());
    entity.setIdentityId(identityRef);

    return entity;
  }
}
