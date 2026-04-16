package cv.igrp.RH_Service.shared.infrastructure.persistence.repository;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.FinancialExecutionMirrorEntity;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface FinancialExecutionMirrorEntityRepository extends
    JpaRepository<FinancialExecutionMirrorEntity, UUID>,
    JpaSpecificationExecutor<FinancialExecutionMirrorEntity>
{
      Optional<FinancialExecutionMirrorEntity> findByClassifierAndOrganicUnitAndFiscalYear(String classifier, String organicUnit, Integer fiscalYear);

      List<FinancialExecutionMirrorEntity> findAllByFiscalYear(Integer fiscalYear);

      List<FinancialExecutionMirrorEntity> findAllByFiscalYearAndOrganicUnit(Integer fiscalYear, String organicUnit);

      default FinancialExecutionMirrorEntity findByIdOrThrow(UUID id) {
          return this.findById(id)
          .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.NOT_FOUND,"FinancialExecutionMirrorEntity not found for id: " + id));
      }

}
