package cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters.strategy;

import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.BscPerspectiveConfigEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.BscPerspectiveConfigEntityRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.BscPerspectiveConfig;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.BscPerspectiveConfigRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.strategy.BscPerspectiveConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Adapter for the {@link BscPerspectiveConfigRepository} port. Reads/writes ONLY the
 * perspective-config table via {@link BscPerspectiveConfigEntityRepository} -- no other
 * aggregate or table is ever touched here (PERSP-03 structural isolation).
 */
@Component
@RequiredArgsConstructor
public class BscPerspectiveConfigRepositoryImpl implements BscPerspectiveConfigRepository {

  private final BscPerspectiveConfigEntityRepository jpaRepository;
  private final BscPerspectiveConfigMapper mapper;

  @Transactional(readOnly = true)
  @Override
  public List<BscPerspectiveConfig> findAll() {
    return jpaRepository.findAllByOrderByDisplayOrderAsc().stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Transactional(readOnly = true)
  @Override
  public Optional<BscPerspectiveConfig> findByCode(String code) {
    return jpaRepository.findByCode(code)
        .map(mapper::toDomain);
  }

  @Transactional
  @Override
  public List<BscPerspectiveConfig> saveAll(List<BscPerspectiveConfig> configs) {
    List<BscPerspectiveConfigEntity> entities = configs.stream()
        .map(mapper::toEntity)
        .toList();
    return jpaRepository.saveAll(entities).stream()
        .map(mapper::toDomain)
        .toList();
  }
}
