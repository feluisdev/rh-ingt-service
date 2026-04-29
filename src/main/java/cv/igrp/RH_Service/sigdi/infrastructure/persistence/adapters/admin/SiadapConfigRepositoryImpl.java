package cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters.admin;

import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.SiadapConfigEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.SiadapConfigEntityRepository;
import cv.igrp.RH_Service.sigdi.domain.admin.models.SiadapConfig;
import cv.igrp.RH_Service.sigdi.domain.admin.repository.SiadapConfigRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.admin.SiadapConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SiadapConfigRepositoryImpl implements SiadapConfigRepository {

  private final SiadapConfigEntityRepository jpaRepository;
  private final SiadapConfigMapper mapper;

  @Transactional
  @Override
  public SiadapConfig save(SiadapConfig config) {
    SiadapConfigEntity entity = mapper.toEntity(config);
    SiadapConfigEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Transactional(readOnly = true)
  @Override
  public Optional<SiadapConfig> findByFiscalYear(Integer fiscalYear) {
    return jpaRepository.findByFiscalYear(fiscalYear)
        .map(mapper::toDomain);
  }
}
