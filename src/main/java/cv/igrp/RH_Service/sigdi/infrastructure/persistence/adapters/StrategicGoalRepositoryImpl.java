package cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.InstitutionalIdentityEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.StrategicGoalEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.StrategicGoalEntityRepository;
import cv.igrp.RH_Service.sigdi.domain.models.InstitutionalIdentity;
import cv.igrp.RH_Service.sigdi.domain.models.StrategicGoal;
import cv.igrp.RH_Service.sigdi.domain.repository.StrategicGoalRepository;
import cv.igrp.RH_Service.sigdi.domain.valueobject.StrategicGoalId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.InstitutionalIdentityMapper;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.StrategicGoalMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class StrategicGoalRepositoryImpl implements StrategicGoalRepository {

  private final StrategicGoalEntityRepository jpaRepository;
  private final StrategicGoalMapper goalMapper;
  private final InstitutionalIdentityMapper identityMapper;

  @Override
  public StrategicGoal save(StrategicGoal goal) {
    StrategicGoalEntity entity = goalMapper.toEntity(goal);
    StrategicGoalEntity saved = jpaRepository.save(entity);

    InstitutionalIdentity identity = goal.getIdentity();
    return goalMapper.toDomain(saved, identity);
  }

  @Override
  public Optional<StrategicGoal> findById(StrategicGoalId id) {
    return jpaRepository.findById(id.getValor().getValor())
        .map(entity -> {
          InstitutionalIdentityEntity identityEntity = entity.getIdentityId();
          InstitutionalIdentity identity = identityMapper.toDomain(identityEntity);
          return goalMapper.toDomain(entity, identity);
        });
  }
}
