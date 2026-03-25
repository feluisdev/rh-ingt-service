package cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters.tatical;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.TacticalActivitiesEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.TacticalActivitiesEntityRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.TacticalActivity;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.TacticalActivityRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.TacticalActivityId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.tatical.TacticalActivityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TacticalActivityRepositoryImpl implements TacticalActivityRepository {

  private final TacticalActivitiesEntityRepository jpaRepository;
  private final TacticalActivityMapper mapper;

  @Override
  public TacticalActivity save(TacticalActivity activity) {
    TacticalActivitiesEntity entity = mapper.toEntity(activity);
    TacticalActivitiesEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Override
  public Optional<TacticalActivity> findById(TacticalActivityId id) {
    return jpaRepository.findById(id.getValor().getValor())
        .map(mapper::toDomain);
  }

  @Override
  public Optional<TacticalActivity> findByIdFull(TacticalActivityId id) {
    return jpaRepository.findById(id.getValor().getValor())
        .map(mapper::toDomainFull);
  }
}
