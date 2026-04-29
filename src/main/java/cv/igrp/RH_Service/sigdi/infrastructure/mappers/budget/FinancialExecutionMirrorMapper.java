package cv.igrp.RH_Service.sigdi.infrastructure.mappers.budget;

import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.FinancialExecutionMirrorEntity;
import cv.igrp.RH_Service.sigdi.domain.budget.models.FinancialExecutionMirror;
import cv.igrp.RH_Service.sigdi.domain.budget.valueobject.FinancialExecutionMirrorId;
import cv.igrp.RH_Service.sigdi.domain.shared.valueobject.EconomicClassifier;
import org.springframework.stereotype.Component;

@Component
public class FinancialExecutionMirrorMapper {

  public FinancialExecutionMirror toDomain(FinancialExecutionMirrorEntity entity) {
    if (entity == null) return null;

    return FinancialExecutionMirror.reconstruct(
        FinancialExecutionMirrorId.from(entity.getId()),
        EconomicClassifier.of(entity.getClassifier()),
        entity.getOrganicUnit(),
        entity.getFiscalYear(),
        entity.getAmountCommitted(),
        entity.getAmountLiquidated(),
        entity.getAmountPaid(),
        entity.getLastSync()
    );
  }

  public FinancialExecutionMirrorEntity toEntity(FinancialExecutionMirror domain) {
    if (domain == null) return null;

    FinancialExecutionMirrorEntity entity = new FinancialExecutionMirrorEntity();
    entity.setId(domain.getId().getValor().getValor());
    entity.setClassifier(domain.getClassifier().getCode());
    entity.setOrganicUnit(domain.getOrganicUnit());
    entity.setFiscalYear(domain.getFiscalYear());
    entity.setAmountCommitted(domain.getAmountCommitted());
    entity.setAmountLiquidated(domain.getAmountLiquidated());
    entity.setAmountPaid(domain.getAmountPaid());
    entity.setLastSync(domain.getLastSync());
    return entity;
  }
}

