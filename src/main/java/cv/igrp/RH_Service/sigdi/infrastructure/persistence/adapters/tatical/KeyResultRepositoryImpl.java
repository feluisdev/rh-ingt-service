package cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters.tatical;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.KeyResultsEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.KeyResultsEntityRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.KeyResult;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.KeyResultRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.KeyResultId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.tatical.KeyResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class KeyResultRepositoryImpl implements KeyResultRepository {

  private final KeyResultsEntityRepository jpaRepository;
  private final KeyResultMapper mapper;

  @Override
  public KeyResult save(KeyResult keyResult) {
    KeyResultsEntity entity = mapper.toEntity(keyResult);
    KeyResultsEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Override
  public Optional<KeyResult> findById(KeyResultId id) {
    return jpaRepository.findById(id.getValor().getValor())
        .map(mapper::toDomain);
  }

  @Override
  public Optional<KeyResult> findByIdFull(KeyResultId id) {
    return jpaRepository.findById(id.getValor().getValor())
        .map(mapper::toDomainFull);
  }
}
