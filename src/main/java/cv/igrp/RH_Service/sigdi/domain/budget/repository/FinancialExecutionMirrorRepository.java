package cv.igrp.RH_Service.sigdi.domain.budget.repository;

import cv.igrp.RH_Service.sigdi.domain.budget.models.FinancialExecutionMirror;
import cv.igrp.RH_Service.sigdi.domain.budget.valueobject.FinancialExecutionMirrorId;
import cv.igrp.RH_Service.sigdi.domain.shared.valueobject.EconomicClassifier;

import java.util.List;
import java.util.Optional;

public interface FinancialExecutionMirrorRepository {

  FinancialExecutionMirror save(FinancialExecutionMirror mirror);

  Optional<FinancialExecutionMirror> findById(FinancialExecutionMirrorId id);

  Optional<FinancialExecutionMirror> findByKey(EconomicClassifier classifier, String organicUnit, Integer fiscalYear);

  List<FinancialExecutionMirror> findAllByFiscalYear(Integer fiscalYear);

  List<FinancialExecutionMirror> findAllByFiscalYearAndOrganicUnit(Integer fiscalYear, String organicUnit);
}

