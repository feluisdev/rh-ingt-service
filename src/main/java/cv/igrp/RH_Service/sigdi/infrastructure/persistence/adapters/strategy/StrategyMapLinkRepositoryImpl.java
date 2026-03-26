package cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters.strategy;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.StrategyMapLinkEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.StrategyMapLinkEntityRepository;
import cv.igrp.RH_Service.sigdi.application.constants.StrategyMapRelationshipType;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.StrategyMapLink;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.StrategyMapLinkRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategyMapLinkId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.strategy.StrategyMapLinkMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class StrategyMapLinkRepositoryImpl implements StrategyMapLinkRepository {

  private final StrategyMapLinkEntityRepository jpaRepository;
  private final StrategyMapLinkMapper mapper;

  @Override
  public StrategyMapLink save(StrategyMapLink link) {
    StrategyMapLinkEntity entity = mapper.toEntity(link);
    StrategyMapLinkEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Override
  public Optional<StrategyMapLink> findById(StrategyMapLinkId id) {
    return jpaRepository.findById(id.getValor().getValor())
        .map(mapper::toDomain);
  }

  @Override
  public Optional<StrategyMapLink> findBySourceTargetAndType(StrategicGoalId sourceGoalId, StrategicGoalId targetGoalId,
      StrategyMapRelationshipType relationshipType) {
    return jpaRepository.findBySourceGoalId_IdAndTargetGoalId_IdAndRelationshipType(
        sourceGoalId.getValor().getValor(),
        targetGoalId.getValor().getValor(),
        relationshipType.getCode())
        .map(mapper::toDomain);
  }
}
