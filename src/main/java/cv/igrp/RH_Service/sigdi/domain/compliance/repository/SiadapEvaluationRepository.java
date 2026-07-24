package cv.igrp.RH_Service.sigdi.domain.compliance.repository;

import cv.igrp.RH_Service.sigdi.domain.compliance.models.SiadapEvaluation;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.SiadapEvaluationId;

import java.util.List;
import java.util.Optional;

public interface SiadapEvaluationRepository {

  SiadapEvaluation save(SiadapEvaluation evaluation);

  Optional<SiadapEvaluation> findById(SiadapEvaluationId id);

  Optional<SiadapEvaluation> findByEmployeeAndYear(String employeeId, Integer year);

  List<SiadapEvaluation> findByYear(Integer year);

  List<SiadapEvaluation> findByYearAndOrganicUnitId(Integer year, String organicUnitId);

  List<SiadapEvaluation> saveAll(List<SiadapEvaluation> evaluations);
}

