package cv.igrp.RH_Service.sigdi.infrastructure.mappers.strategy;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.InstitutionalIdentityEntity;
import cv.igrp.RH_Service.sigdi.application.dto.CreateIdentityRequestDTO;
import cv.igrp.RH_Service.sigdi.application.dto.IdentityResponseDTO;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.InstitutionalIdentity;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.StrategicGoal;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.InstitutionalIdentityId;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.InstitutionalValues;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class InstitutionalIdentityMapper {

  private final StrategicGoalMapper goalMapper;

  /**
   * Entity → Domain (sem goals)
   */
  public InstitutionalIdentity toDomain(InstitutionalIdentityEntity entity) {
    if (entity == null) return null;

    return InstitutionalIdentity.reconstruct(
        InstitutionalIdentityId.from(entity.getId()),
        entity.getCycleYear(),
        entity.getMission(),
        entity.getVision(),
        InstitutionalValues.of(entity.getValuesJson()),
        entity.getVersionComment(),
        entity.isActive(),
        new ArrayList<>()
    );
  }

  /**
   * Entity → Domain com goals (aggregate completo)
   * Usa os goals já carregados via @OneToMany da entity
   */
  public InstitutionalIdentity toDomainFull(InstitutionalIdentityEntity entity) {
    if (entity == null) return null;

    List<StrategicGoal> goals = entity.getGoals().stream()
        .map(goalMapper::toDomain)
        .collect(Collectors.toList());

    return InstitutionalIdentity.reconstruct(
        InstitutionalIdentityId.from(entity.getId()),
        entity.getCycleYear(),
        entity.getMission(),
        entity.getVision(),
        InstitutionalValues.of(entity.getValuesJson()),
        entity.getVersionComment(),
        entity.isActive(),
        goals
    );
  }

  /**
   * Domain → Entity
   */
  public InstitutionalIdentityEntity toEntity(InstitutionalIdentity domain) {
    if (domain == null) return null;

    InstitutionalIdentityEntity entity = new InstitutionalIdentityEntity();
    entity.setId(domain.getId().getValor().getValor());
    entity.setCycleYear(domain.getCycleYear());
    entity.setMission(domain.getMission());
    entity.setVision(domain.getVision());
    entity.setValuesJson(domain.getValues().toJson());
    entity.setVersionComment(domain.getVersionComment());
    entity.setActive(domain.isActive());
    return entity;
  }

  public IdentityResponseDTO toResponse(InstitutionalIdentity domain) {
    if (domain == null) return null;

    IdentityResponseDTO response = new IdentityResponseDTO();
    response.setId(domain.getId().getValor().getValor());
    response.setCycleYear(domain.getCycleYear());
    response.setMission(domain.getMission());
    response.setVision(domain.getVision());
    response.setValues(domain.getValues().getValores());
    response.setVersionComment(domain.getVersionComment());
    response.setActive(domain.isActive());
    return response;
  }

  public InstitutionalValues toValues(CreateIdentityRequestDTO dto) {
    return InstitutionalValues.of(dto.getValues());
  }

}
