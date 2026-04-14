package cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters.strategy;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.StrategyMapLinkEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.StrategyMapLinkEntityRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.StrategyMapLink;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.StrategyMapLinkRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.InstitutionalIdentityId;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategyMapLinkId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.strategy.StrategyMapLinkMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class StrategyMapLinkRepositoryImpl implements StrategyMapLinkRepository {

  private final StrategyMapLinkEntityRepository jpaRepository;
  private final StrategyMapLinkMapper mapper;

  @Transactional
  @Override
  public StrategyMapLink save(StrategyMapLink link) {
    StrategyMapLinkEntity entity = mapper.toEntity(link);
    StrategyMapLinkEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Transactional(readOnly = true)
  @Override
  public Optional<StrategyMapLink> findById(StrategyMapLinkId id) {
    return jpaRepository.findById(id.getValor().getValor())
        .map(mapper::toDomain);
  }

  @Transactional(readOnly = true)
  @Override
  public Optional<StrategyMapLink> findBySourceAndTarget(StrategicGoalId sourceGoalId, StrategicGoalId targetGoalId) {
    return jpaRepository.findBySourceGoalId_IdAndTargetGoalId_Id(
        sourceGoalId.getValor().getValor(),
        targetGoalId.getValor().getValor()
    ).map(mapper::toDomain);
  }

  @Transactional(readOnly = true)
  @Override
  public List<StrategyMapLink> findByIdentityId(InstitutionalIdentityId identityId) {
    return jpaRepository.findBySourceGoalId_IdentityId_IdAndTargetGoalId_IdentityId_Id(
            identityId.getValor().getValor(),
            identityId.getValor().getValor()
        ).stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Transactional
  @Override
  public void delete(StrategyMapLinkId id) {
    jpaRepository.deleteById(id.getValor().getValor());
  }
}
