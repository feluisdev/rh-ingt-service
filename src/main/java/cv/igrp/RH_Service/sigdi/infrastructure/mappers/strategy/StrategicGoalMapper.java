package cv.igrp.RH_Service.sigdi.infrastructure.mappers.strategy;

import cv.igrp.RH_Service.shared.application.constants.Estado;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.InstitutionalIdentityEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.StrategicGoalEntity;
import cv.igrp.RH_Service.sigdi.application.constants.StrategicGoalsPerspective;
import cv.igrp.RH_Service.sigdi.application.dto.StategicGoalResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.StategicGoalSumaryDTO;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.BscPerspectiveConfig;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.StrategicGoal;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.BscPerspectiveConfigRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.InstitutionalIdentityId;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.StrategicIndicatorEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StrategicGoalMapper {

  private final BscPerspectiveConfigRepository bscPerspectiveConfigRepository;

  public StrategicGoal toDomain(StrategicGoalEntity entity) {
    if (entity == null) return null;

    java.util.List<cv.igrp.RH_Service.sigdi.domain.strategy.models.StrategicIndicator> domainIndicators = new java.util.ArrayList<>();
    if (entity.getIndicators() != null && !entity.getIndicators().isEmpty()) {
      domainIndicators = entity.getIndicators().stream().map(indEntity ->
        cv.igrp.RH_Service.sigdi.domain.strategy.models.StrategicIndicator.reconstruct(
          indEntity.getId(),
          indEntity.getTitle(),
          indEntity.getFormula(),
          indEntity.getTarget(),
          indEntity.getEvaluationCriteria(),
          indEntity.getInfoSource(),
          indEntity.getWeight(),
          indEntity.getCriteriaSuperado(),
          indEntity.getCriteriaSeguranca(),
          indEntity.getCriteriaAlcancado(),
          indEntity.getCriteriaInsuficiente()
        )
      ).collect(java.util.stream.Collectors.toList());
    }

    return StrategicGoal.reconstruct(
        StrategicGoalId.from(entity.getId()),
        entity.getInstitutionId(),
        InstitutionalIdentityId.from(entity.getIdentityId().getId()),
        entity.getTitle(),
        StrategicGoalsPerspective.fromCodeOrThrow(entity.getPerspective()),
        entity.getWeight(),
        Estado.fromCodeOrThrow(entity.getStatus()),
        entity.getDescription(),
        entity.getPositionX(),
        entity.getPositionY(),
        entity.getYear(),
        domainIndicators
    );
  }

  public StategicGoalResponseDTO toResponse(StrategicGoal domain) {
    if (domain == null) return null;
    String perspectiveCode = domain.getPerspective().getCode();
    String perspectiveLabel = bscPerspectiveConfigRepository.findByCode(perspectiveCode)
        .map(BscPerspectiveConfig::getLabel)
        .orElse(perspectiveCode);
    return toResponse(domain, java.util.Map.of(perspectiveCode, perspectiveLabel));
  }

  /**
   * Same field mapping as {@link #toResponse(StrategicGoal)}, but sources the perspective label from
   * a pre-built {@code code -> label} map instead of issuing its own {@code findByCode} lookup.
   * Callers mapping a list of goals (e.g. {@code GetCurrentStrategyMapQueryHandler}) should build the
   * map once via a single {@code findAll()} and pass it in here, avoiding one {@code findByCode} query
   * per goal.
   */
  public StategicGoalResponseDTO toResponse(StrategicGoal domain, java.util.Map<String, String> labelsByCode) {
    if (domain == null) return null;
    StategicGoalResponseDTO dto = new StategicGoalResponseDTO();
    dto.setId(domain.getId().getValor().getValor());
    dto.setIdentityId(domain.getIdentityId().getValor().getValor());
    dto.setPerspective(domain.getPerspective().getCode());
    dto.setPerspectiveDesc(
        labelsByCode.getOrDefault(domain.getPerspective().getCode(), domain.getPerspective().getCode()));
    dto.setWeight(domain.getWeight());
    dto.setTitle(domain.getTitle());
    dto.setDescription(domain.getDescription());
    dto.setStatus(domain.getStatus().getCode());
    dto.setStatusDesc(domain.getStatus().getDescription());
    dto.setYear(domain.getYear());
    dto.setProgress(0.0);
    dto.setLinkedActivities(0);

    if (domain.getIndicators() != null && !domain.getIndicators().isEmpty()) {
      java.util.List<cv.igrp.RH_Service.sigdi.application.dto.StrategicIndicatorDTO> indicatorDTOs = domain.getIndicators().stream().map(ind -> {
        cv.igrp.RH_Service.sigdi.application.dto.StrategicIndicatorDTO indDTO = new cv.igrp.RH_Service.sigdi.application.dto.StrategicIndicatorDTO();
        indDTO.setId(ind.getId());
        indDTO.setTitle(ind.getTitle());
        indDTO.setFormula(ind.getFormula());
        indDTO.setTarget(ind.getTarget());
        indDTO.setEvaluationCriteria(ind.getEvaluationCriteria());
        indDTO.setInfoSource(ind.getInfoSource());
        indDTO.setWeight(ind.getWeight());
        indDTO.setCriteriaSuperado(ind.getCriteriaSuperado());
        indDTO.setCriteriaSeguranca(ind.getCriteriaSeguranca());
        indDTO.setCriteriaAlcancado(ind.getCriteriaAlcancado());
        indDTO.setCriteriaInsuficiente(ind.getCriteriaInsuficiente());
        return indDTO;
      }).collect(java.util.stream.Collectors.toList());
      dto.setIndicators(indicatorDTOs);
    }

    return dto;
  }

  public StategicGoalSumaryDTO toSummary(StrategicGoal domain) {
    if (domain == null) return null;
    StategicGoalSumaryDTO dto = new StategicGoalSumaryDTO();
    dto.setId(domain.getId().getValor().getValor());
    dto.setTitle(domain.getTitle());
    dto.setPerspective(domain.getPerspective().getCode());
    dto.setWeight(domain.getWeight());
    dto.setStatus(domain.getStatus().getCode());
    dto.setStatusDes(domain.getStatus().getDescription());
    dto.setYear(domain.getYear());
    dto.setProgress(0.0);
    dto.setLinkedActivities(0);
    return dto;
  }

  public StrategicGoalEntity toEntity(StrategicGoal domain) {
    if (domain == null) return null;

    StrategicGoalEntity entity = new StrategicGoalEntity();
    entity.setId(domain.getId().getValor().getValor());
    entity.setInstitutionId(domain.getInstitutionId());
    entity.setTitle(domain.getTitle());
    entity.setPerspective(domain.getPerspective().getCode());
    entity.setWeight(domain.getWeight());
    entity.setStatus(domain.getStatus().getCode());
    entity.setDescription(domain.getDescription());
    entity.setPositionX(domain.getPositionX());
    entity.setPositionY(domain.getPositionY());
    entity.setYear(domain.getYear());

    InstitutionalIdentityEntity identityRef = new InstitutionalIdentityEntity();
    identityRef.setId(domain.getIdentityId().getValor().getValor());
    entity.setIdentityId(identityRef);

    if (domain.getIndicators() != null && !domain.getIndicators().isEmpty()) {
      java.util.List<StrategicIndicatorEntity> indicatorEntities = domain.getIndicators().stream().map(ind -> {
        StrategicIndicatorEntity indEntity = new StrategicIndicatorEntity();
        indEntity.setId(ind.getId());
        indEntity.setTitle(ind.getTitle());
        indEntity.setFormula(ind.getFormula());
        indEntity.setTarget(ind.getTarget());
        indEntity.setEvaluationCriteria(ind.getEvaluationCriteria());
        indEntity.setInfoSource(ind.getInfoSource());
        indEntity.setWeight(ind.getWeight());
        indEntity.setCriteriaSuperado(ind.getCriteriaSuperado());
        indEntity.setCriteriaSeguranca(ind.getCriteriaSeguranca());
        indEntity.setCriteriaAlcancado(ind.getCriteriaAlcancado());
        indEntity.setCriteriaInsuficiente(ind.getCriteriaInsuficiente());
        indEntity.setGoal(entity);
        return indEntity;
      }).collect(java.util.stream.Collectors.toList());
      entity.setIndicators(indicatorEntities);
    }

    return entity;
  }
}
