package cv.igrp.RH_Service.sigdi.infrastructure.mappers.tatical;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.KeyResultsEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.OkrEntity;
import cv.igrp.RH_Service.sigdi.application.constants.KeyResultMetricUnit;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.InstitutionalIdentityId;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.Okr;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.OkrKeyResult;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.KeyResultId;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.OkrId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OkrMapper {

  public Okr toDomain(OkrEntity entity) {
    if (entity == null) return null;

    return Okr.reconstruct(
        OkrId.from(entity.getId()),
        entity.getInstitutionId() != null
            ? InstitutionalIdentityId.from(entity.getInstitutionId())
            : null,
        entity.getStrategicGoalId() != null
            ? StrategicGoalId.from(entity.getStrategicGoalId())
            : null,
        entity.getTitle(),
        entity.getCycle(),
        entity.getStatus(),
        new ArrayList<>()
    );
  }

  public Okr toDomainFull(OkrEntity entity) {
    if (entity == null) return null;

    List<OkrKeyResult> keyResults = entity.getKeyResults().stream()
        .map(this::krToDomain)
        .collect(Collectors.toList());

    return Okr.reconstruct(
        OkrId.from(entity.getId()),
        entity.getInstitutionId() != null
            ? InstitutionalIdentityId.from(entity.getInstitutionId())
            : null,
        entity.getStrategicGoalId() != null
            ? StrategicGoalId.from(entity.getStrategicGoalId())
            : null,
        entity.getTitle(),
        entity.getCycle(),
        entity.getStatus(),
        keyResults
    );
  }

  public OkrEntity toEntity(Okr domain) {
    if (domain == null) return null;

    OkrEntity entity = new OkrEntity();
    entity.setId(domain.getId().getValor().getValor());
    entity.setInstitutionId(
        domain.getInstitutionId() != null
            ? domain.getInstitutionId().getValor().getValor()
            : null);
    entity.setStrategicGoalId(
        domain.getStrategicGoalId() != null
            ? domain.getStrategicGoalId().getValor().getValor()
            : null);
    entity.setTitle(domain.getTitle());
    entity.setCycle(domain.getCycle());
    entity.setStatus(domain.getStatus());
    return entity;
  }

  public KeyResultsEntity krToEntity(OkrKeyResult domain, OkrEntity okrRef) {
    if (domain == null) return null;

    KeyResultsEntity entity = new KeyResultsEntity();
    entity.setId(domain.getId().getValor().getValor());
    entity.setTitle(domain.getTitle());
    entity.setTargetValue(domain.getTargetValue());
    entity.setCurrentValue(domain.getCurrentValue() != null
        ? domain.getCurrentValue()
        : BigDecimal.ZERO);
    entity.setMetricUnit(domain.getUnit().getCode());
    entity.setWeight(domain.getWeight());
    entity.setOkrId(okrRef);
    return entity;
  }

  public OkrKeyResult krToDomain(KeyResultsEntity entity) {
    if (entity == null) return null;

    return OkrKeyResult.reconstruct(
        KeyResultId.from(entity.getId()),
        entity.getOkrId() != null
            ? OkrId.from(entity.getOkrId().getId())
            : null,
        entity.getTitle(),
        entity.getTargetValue(),
        entity.getCurrentValue(),
        KeyResultMetricUnit.fromCodeOrThrow(entity.getMetricUnit()),
        entity.getWeight()
    );
  }
}