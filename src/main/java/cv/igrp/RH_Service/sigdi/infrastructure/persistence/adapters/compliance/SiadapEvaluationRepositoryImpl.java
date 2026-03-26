package cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters.compliance;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.SiadapEvaluationEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.SiadapEvaluationEntityRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapEvaluation;
import cv.igrp.RH_Service.sigdi.domain.compliance.repository.SiadapEvaluationRepository;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.SiadapEvaluationId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.compliance.SiadapEvaluationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SiadapEvaluationRepositoryImpl implements SiadapEvaluationRepository {

  private final SiadapEvaluationEntityRepository jpaRepository;
  private final SiadapEvaluationMapper mapper;

  @Override
  public SiadapEvaluation save(SiadapEvaluation evaluation) {
    SiadapEvaluationEntity entity = mapper.toEntity(evaluation);
    SiadapEvaluationEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Override
  public Optional<SiadapEvaluation> findById(SiadapEvaluationId id) {
    return jpaRepository.findById(id.getValor().getValor())
        .map(mapper::toDomain);
  }

  @Override
  public Optional<SiadapEvaluation> findByEmployeeAndYear(String employeeId, Integer year) {
    if (employeeId == null || employeeId.isBlank() || year == null) return Optional.empty();
    return jpaRepository.findByEmployeeIdAndYear(employeeId, year.toString())
        .map(mapper::toDomain);
  }

  @Override
  public List<SiadapEvaluation> findByYear(Integer year) {
    if (year == null) return List.of();
    return jpaRepository.findByYear(year.toString()).stream()
        .map(mapper::toDomain)
        .toList();
  }
}

