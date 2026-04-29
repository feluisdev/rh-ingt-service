package cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters.budget;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.CostDriverEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.CostDriverEntityRepository;
import cv.igrp.RH_Service.sigdi.application.constants.CostDriverType;
import cv.igrp.RH_Service.sigdi.domain.budget.models.CostDriver;
import cv.igrp.RH_Service.sigdi.domain.budget.repository.CostDriverRepository;
import cv.igrp.RH_Service.sigdi.domain.budget.valueobject.CostDriverId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.budget.CostDriverMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CostDriverRepositoryImpl implements CostDriverRepository {

  private final CostDriverEntityRepository jpaRepository;
  private final CostDriverMapper mapper;

  @Transactional
  @Override
  public CostDriver save(CostDriver costDriver) {
    CostDriverEntity entity = mapper.toEntity(costDriver);
    CostDriverEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Transactional
  @Override
  public CostDriver update(CostDriver costDriver) {
    CostDriverEntity entity = mapper.toEntity(costDriver);
    CostDriverEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Transactional(readOnly = true)
  @Override
  public List<CostDriver> findAll() {
    return jpaRepository.findAll().stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Transactional(readOnly = true)
  @Override
  public Optional<CostDriver> findById(CostDriverId id) {
    return jpaRepository.findById(id.getValor().getValor())
        .map(mapper::toDomain);
  }

  @Transactional(readOnly = true)
  @Override
  public List<CostDriver> findByType(CostDriverType type) {
    return jpaRepository.findByDriverType(type.getCode()).stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Transactional(readOnly = true)
  @Override
  public Optional<CostDriver> findActiveByType(CostDriverType type, LocalDate referenceDate) {
    LocalDate date = (referenceDate != null) ? referenceDate : LocalDate.now();

    return jpaRepository.findByDriverType(type.getCode()).stream()
        .map(mapper::toDomain)
        .filter(driver -> !date.isBefore(driver.getValidFrom()))
        .findFirst();
  }
}

