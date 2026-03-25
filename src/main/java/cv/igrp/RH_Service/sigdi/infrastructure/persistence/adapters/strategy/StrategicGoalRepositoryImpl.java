package cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters.strategy;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.StrategicGoalEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.StrategicGoalEntityRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.StrategicGoal;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.StrategicGoalRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.strategy.StrategicGoalMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class StrategicGoalRepositoryImpl implements StrategicGoalRepository {

  private final StrategicGoalEntityRepository jpaRepository;
  private final StrategicGoalMapper mapper;

  @Override
  public StrategicGoal save(StrategicGoal goal) {
    StrategicGoalEntity entity = mapper.toEntity(goal);
    StrategicGoalEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Override
  public Optional<StrategicGoal> findById(StrategicGoalId id) {
    return jpaRepository.findById(id.getValor().getValor())
        .map(mapper::toDomain);
  }
}
