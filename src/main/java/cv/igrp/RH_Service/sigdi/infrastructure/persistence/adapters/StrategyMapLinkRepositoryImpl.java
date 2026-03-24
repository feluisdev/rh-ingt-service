package cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.StrategicGoalEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.StrategyMapLinkEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.StrategyMapLinkEntityRepository;
import cv.igrp.RH_Service.sigdi.domain.models.InstitutionalIdentity;
import cv.igrp.RH_Service.sigdi.domain.models.StrategicGoal;
import cv.igrp.RH_Service.sigdi.domain.models.StrategyMapLink;
import cv.igrp.RH_Service.sigdi.domain.repository.StrategyMapLinkRepository;
import cv.igrp.RH_Service.sigdi.domain.valueobject.StrategyMapLinkId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.InstitutionalIdentityMapper;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.StrategicGoalMapper;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.StrategyMapLinkMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class StrategyMapLinkRepositoryImpl implements StrategyMapLinkRepository {

  private final StrategyMapLinkEntityRepository jpaRepository;
  private final StrategyMapLinkMapper linkMapper;
  private final StrategicGoalMapper goalMapper;
  private final InstitutionalIdentityMapper identityMapper;

  @Override
  public StrategyMapLink save(StrategyMapLink link) {
    var entity = linkMapper.toEntity(link);
    var saved = jpaRepository.save(entity);

    // Reutiliza os goals já disponíveis no domain object
    return linkMapper.toDomain(saved, link.getSourceGoalId(), link.getTargetGoalId());
  }

  @Override
  public Optional<StrategyMapLink> findById(StrategyMapLinkId id) {
    return jpaRepository.findById(id.getValor().getValor())
        .map(entity -> {
          // Orquestra: source
          StrategicGoalEntity sourceEntity = entity.getSourceGoalId();
          InstitutionalIdentity sourceIdentity = identityMapper.toDomain(sourceEntity.getIdentityId());
          StrategicGoal sourceGoal = goalMapper.toDomain(sourceEntity, sourceIdentity);

          // Orquestra: target
          StrategicGoalEntity targetEntity = entity.getTargetGoalId();
          InstitutionalIdentity targetIdentity = identityMapper.toDomain(targetEntity.getIdentityId());
          StrategicGoal targetGoal = goalMapper.toDomain(targetEntity, targetIdentity);

          return linkMapper.toDomain(entity, sourceGoal, targetGoal);
        });
  }

}
