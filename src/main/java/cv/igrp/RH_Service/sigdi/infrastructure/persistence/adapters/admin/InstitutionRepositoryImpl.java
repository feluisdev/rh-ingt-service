package cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters.admin;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.InstitutionEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.InstitutionEntityRepository;
import cv.igrp.RH_Service.sigdi.domain.admin.models.Institution;
import cv.igrp.RH_Service.sigdi.domain.admin.repository.InstitutionRepository;
import cv.igrp.RH_Service.sigdi.domain.admin.valueobject.InstitutionId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.admin.InstitutionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class InstitutionRepositoryImpl implements InstitutionRepository {

  private final InstitutionEntityRepository jpaRepository;
  private final InstitutionMapper mapper;

  @Transactional
  @Override
  public Institution save(Institution institution) {
    InstitutionEntity entity = mapper.toEntity(institution);
    InstitutionEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Transactional(readOnly = true)
  @Override
  public Optional<Institution> findById(InstitutionId id) {
    return jpaRepository.findById(id.getValor().getValor())
        .map(mapper::toDomain);
  }

  @Transactional(readOnly = true)
  @Override
  public Optional<Institution> findByCode(String code) {
    return jpaRepository.findByCode(code)
        .map(mapper::toDomain);
  }

  @Transactional(readOnly = true)
  @Override
  public List<Institution> findAll() {
    return jpaRepository.findAll().stream()
        .map(mapper::toDomain)
        .collect(Collectors.toList());
  }

  @Transactional
  @Override
  public Institution update(Institution institution) {
    InstitutionEntity entity = mapper.toEntity(institution);
    InstitutionEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }
}
