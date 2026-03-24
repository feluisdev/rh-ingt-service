package cv.igrp.RH_Service.sigdi.infrastructure.mappers;


import cv.igrp.RH_Service.shared.application.constants.Estado;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.InstitutionalIdentityEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.StrategicGoalEntity;
import cv.igrp.RH_Service.sigdi.application.constants.StrategicGoalsPerspective;
import cv.igrp.RH_Service.sigdi.domain.models.InstitutionalIdentity;
import cv.igrp.RH_Service.sigdi.domain.models.StrategicGoal;
import cv.igrp.RH_Service.sigdi.domain.valueobject.StrategicGoalId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StrategicGoalMapper {

  private final InstitutionalIdentityMapper identityMapper;


  public StrategicGoal toDomain(StrategicGoalEntity entity, InstitutionalIdentity identity) {
    if (entity == null) return null;

    return StrategicGoal.reconstruct(
        StrategicGoalId.from(entity.getId()),
        identity,
        entity.getTitle(),
        StrategicGoalsPerspective.fromCodeOrThrow(entity.getPerspective()),
        entity.getWeight(),
        Estado.fromCodeOrThrow(entity.getStatus()),
        entity.getDescription()
    );
  }

  /**
   * Domain Model → Entity (persistência)
   */
  public StrategicGoalEntity toEntity(StrategicGoal domain) {
    if (domain == null) return null;

    StrategicGoalEntity entity = new StrategicGoalEntity();
    entity.setId(domain.getId().getValor().getValor());
    entity.setTitle(domain.getTitle());
    entity.setPerspective(domain.getPerspective().getCode());
    entity.setWeight(domain.getWeight());
    entity.setStatus(domain.getStatus().getCode());
    entity.setDescription(domain.getDescription());

    if (domain.getIdentity() != null) {
      InstitutionalIdentityEntity identityRef = new InstitutionalIdentityEntity();
      identityRef.setId(domain.getIdentity().getId().getValor().getValor());
      entity.setIdentityId(identityRef);
    }

    return entity;
  }
}
