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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FinancialExecutionMirrorRepositoryImpl implements FinancialExecutionMirrorRepository {

  private final FinancialExecutionMirrorEntityRepository jpaRepository;
  private final FinancialExecutionMirrorMapper mapper;

  @Transactional
  @Override
  public FinancialExecutionMirror save(FinancialExecutionMirror mirror) {
    FinancialExecutionMirrorEntity entity = mapper.toEntity(mirror);
    FinancialExecutionMirrorEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Transactional(readOnly = true)
  @Override
  public Optional<FinancialExecutionMirror> findById(FinancialExecutionMirrorId id) {
    return jpaRepository.findById(id.getValor().getValor())
        .map(mapper::toDomain);
  }

  @Transactional(readOnly = true)
  @Override
  public Optional<FinancialExecutionMirror> findByKey(EconomicClassifier classifier, String organicUnit, Integer fiscalYear) {
    if (classifier == null || organicUnit == null || organicUnit.isBlank() || fiscalYear == null) {
      return Optional.empty();
    }
    return jpaRepository.findByClassifierAndOrganicUnitAndFiscalYear(classifier.getCode(), organicUnit, fiscalYear)
        .map(mapper::toDomain);
  }

  @Transactional(readOnly = true)
  @Override
  public List<FinancialExecutionMirror> findAllByFiscalYear(Integer fiscalYear) {
    return jpaRepository.findAllByFiscalYear(fiscalYear).stream()
        .map(mapper::toDomain)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  @Override
  public List<FinancialExecutionMirror> findAllByFiscalYearAndOrganicUnit(Integer fiscalYear, String organicUnit) {
    return jpaRepository.findAllByFiscalYearAndOrganicUnit(fiscalYear, organicUnit).stream()
        .map(mapper::toDomain)
        .collect(Collectors.toList());
  }
}

