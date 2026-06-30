package cv.igrp.RH_Service.sigdi.infrastructure.mappers.tatical;

import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.TacticalActivitiesEntity;
import cv.igrp.RH_Service.sigdi.application.constants.AcceptanceStatus;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
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
        entity.getOrganicUnitId(),
        entity.getTitle(),
        entity.getDescriptionWhat(),
        entity.getJustificationWhy(),
        entity.getLocationWhere(),
        entity.getResponsibleWho(),
        entity.getMethodologyHow(),
        DateRange.of(entity.getStartDate(), entity.getEndDate()),
        (entity.getBudgetEstimated() != null && entity.getEconomicClassifier() != null) ?
            Budget.of(entity.getBudgetEstimated(), entity.getEconomicClassifier()) : null,
        TacticalActivityStatus.fromCodeOrThrow(entity.getStatus()),
        entity.getVersion(),
        new ArrayList<>(),
        entity.getPaaLevel() != null ? PaaLevel.fromCodeOrThrow(entity.getPaaLevel()) : PaaLevel.UNIT_LEVEL,
        entity.getAcceptanceStatus() != null ? AcceptanceStatus.fromCodeOrThrow(entity.getAcceptanceStatus()) : null
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
        entity.getOrganicUnitId(),
        entity.getTitle(),
        entity.getDescriptionWhat(),
        entity.getJustificationWhy(),
        entity.getLocationWhere(),
        entity.getResponsibleWho(),
        entity.getMethodologyHow(),
        DateRange.of(entity.getStartDate(), entity.getEndDate()),
        (entity.getBudgetEstimated() != null && entity.getEconomicClassifier() != null) ? 
            Budget.of(entity.getBudgetEstimated(), entity.getEconomicClassifier()) : null,
        TacticalActivityStatus.fromCodeOrThrow(entity.getStatus()),
        entity.getVersion(),
        keyResults,
        entity.getPaaLevel() != null ? PaaLevel.fromCodeOrThrow(entity.getPaaLevel()) : PaaLevel.UNIT_LEVEL,
        entity.getAcceptanceStatus() != null ? AcceptanceStatus.fromCodeOrThrow(entity.getAcceptanceStatus()) : null
    );
  }

  public TacticalActivitiesEntity toEntity(TacticalActivity domain) {
    if (domain == null) return null;

    TacticalActivitiesEntity entity = new TacticalActivitiesEntity();
    entity.setId(domain.getId().getValor().getValor());
    entity.setInstitutionId(domain.getInstitutionId());
    entity.setStrategicGoalId(domain.getStrategicGoalId().getValor().getValor());
    entity.setOrganicUnitId(domain.getOrganicUnitId());
    entity.setTitle(domain.getTitle());
    entity.setDescriptionWhat(domain.getDescriptionWhat());
    entity.setJustificationWhy(domain.getJustificationWhy());
    entity.setLocationWhere(domain.getLocationWhere());
    entity.setResponsibleWho(domain.getResponsibleWho());
    entity.setMethodologyHow(domain.getMethodologyHow());
    entity.setStartDate(domain.getDateRange().getStartDate());
    entity.setEndDate(domain.getDateRange().getEndDate());
    if (domain.getBudget() != null) {
      entity.setBudgetEstimated(domain.getBudget().getEstimatedAmount());
      entity.setEconomicClassifier(domain.getBudget().getClassifier().getCode());
    }
    entity.setStatus(domain.getStatus().getCode());
    entity.setVersion(domain.getVersion());
    entity.setPaaLevel(domain.getPaaLevel() != null ? domain.getPaaLevel().getCode() : PaaLevel.UNIT_LEVEL.getCode());
    if (domain.getAcceptanceStatus() != null) {
      entity.setAcceptanceStatus(domain.getAcceptanceStatus().getCode());
    }

    return entity;
  }
}
