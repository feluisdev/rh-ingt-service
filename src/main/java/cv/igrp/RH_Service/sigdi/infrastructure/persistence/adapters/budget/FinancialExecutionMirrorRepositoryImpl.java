package cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters.budget;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.FinancialExecutionMirrorEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.FinancialExecutionMirrorEntityRepository;
import cv.igrp.RH_Service.sigdi.domain.budget.models.FinancialExecutionMirror;
import cv.igrp.RH_Service.sigdi.domain.budget.repository.FinancialExecutionMirrorRepository;
import cv.igrp.RH_Service.sigdi.domain.budget.valueobject.FinancialExecutionMirrorId;
import cv.igrp.RH_Service.sigdi.domain.shared.valueobject.EconomicClassifier;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.budget.FinancialExecutionMirrorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FinancialExecutionMirrorRepositoryImpl implements FinancialExecutionMirrorRepository {

  private final FinancialExecutionMirrorEntityRepository jpaRepository;
  private final FinancialExecutionMirrorMapper mapper;

  @Override
  public FinancialExecutionMirror save(FinancialExecutionMirror mirror) {
    FinancialExecutionMirrorEntity entity = mapper.toEntity(mirror);
    FinancialExecutionMirrorEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Override
  public Optional<FinancialExecutionMirror> findById(FinancialExecutionMirrorId id) {
    return jpaRepository.findById(id.getValor().getValor())
        .map(mapper::toDomain);
  }

  @Override
  public Optional<FinancialExecutionMirror> findByKey(EconomicClassifier classifier, String organicUnit, Integer fiscalYear) {
    if (classifier == null || organicUnit == null || organicUnit.isBlank() || fiscalYear == null) {
      return Optional.empty();
    }
    return jpaRepository.findByClassifierAndOrganicUnitAndFiscalYear(classifier.getCode(), organicUnit, fiscalYear)
        .map(mapper::toDomain);
  }
}

