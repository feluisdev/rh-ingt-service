package cv.igrp.RH_Service.sigdi.infrastructure.mappers.tatical;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.TacticalActivitiesEntity;
import cv.igrp.RH_Service.sigdi.application.constants.TacticalActivityStatus;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.KeyResult;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.TacticalActivity;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.Budget;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.DateRange;
import cv.igrp.RH_Service.sigdi.domain.shared.valueobject.EconomicClassifier;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.TacticalActivityId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TacticalActivityMapper {

  private final KeyResultMapper keyResultMapper;

  public TacticalActivity toDomain(TacticalActivitiesEntity entity) {
    if (entity == null) return null;

    return TacticalActivity.reconstruct(
        TacticalActivityId.from(entity.getId()),
        entity.getInstitutionId(),
        StrategicGoalId.from(entity.getStrategicGoalId()),
        entity.getOrganicUnitId() != null ? entity.getOrganicUnitId().toString() : null,
        entity.getTitle(),
        entity.getDescriptionWhat(),
        entity.getJustificationWhy(),
        entity.getLocationWhere(),
        entity.getResponsibleWho() != null ? entity.getResponsibleWho().toString() : null,
        entity.getMethodologyHow(),
        DateRange.of(entity.getStartDate(), entity.getEndDate()),
        Budget.of(entity.getBudgetEstimated(), EconomicClassifier.of(entity.getEconomicClassifier())),
        TacticalActivityStatus.fromCodeOrThrow(entity.getStatus()),
        entity.getVersion(),
        new ArrayList<>()
    );
  }

  public TacticalActivity toDomainFull(TacticalActivitiesEntity entity) {
    if (entity == null) return null;

    List<KeyResult> keyResults = entity.getKeyResults().stream()
        .map(keyResultMapper::toDomain)
        .collect(Collectors.toList());

    return TacticalActivity.reconstruct(
        TacticalActivityId.from(entity.getId()),
        entity.getInstitutionId(),
        StrategicGoalId.from(entity.getStrategicGoalId()),
        entity.getOrganicUnitId() != null ? entity.getOrganicUnitId().toString() : null,
        entity.getTitle(),
        entity.getDescriptionWhat(),
        entity.getJustificationWhy(),
        entity.getLocationWhere(),
        entity.getResponsibleWho() != null ? entity.getResponsibleWho().toString() : null,
        entity.getMethodologyHow(),
        DateRange.of(entity.getStartDate(), entity.getEndDate()),
        Budget.of(entity.getBudgetEstimated(), EconomicClassifier.of(entity.getEconomicClassifier())),
        TacticalActivityStatus.fromCodeOrThrow(entity.getStatus()),
        entity.getVersion(),
        keyResults
    );
  }

  public TacticalActivitiesEntity toEntity(TacticalActivity domain) {
    if (domain == null) return null;

    TacticalActivitiesEntity entity = new TacticalActivitiesEntity();
    entity.setId(domain.getId().getValor().getValor());
    entity.setInstitutionId(domain.getInstitutionId());
    entity.setStrategicGoalId(domain.getStrategicGoalId().getValor().getValor());
    entity.setOrganicUnitId(domain.getOrganicUnitId() != null ? java.util.UUID.fromString(domain.getOrganicUnitId()) : null);
    entity.setTitle(domain.getTitle());
    entity.setDescriptionWhat(domain.getDescriptionWhat());
    entity.setJustificationWhy(domain.getJustificationWhy());
    entity.setLocationWhere(domain.getLocationWhere());
    entity.setResponsibleWho(domain.getResponsibleWho() != null ? java.util.UUID.fromString(domain.getResponsibleWho()) : null);
    entity.setMethodologyHow(domain.getMethodologyHow());
    entity.setStartDate(domain.getDateRange().getStartDate());
    entity.setEndDate(domain.getDateRange().getEndDate());
    entity.setBudgetEstimated(domain.getBudget().getEstimatedAmount());
    entity.setEconomicClassifier(domain.getBudget().getClassifier().getCode());
    entity.setStatus(domain.getStatus().getCode());
    entity.setVersion(domain.getVersion());

    return entity;
  }
}
