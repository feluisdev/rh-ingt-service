package cv.igrp.RH_Service.sigdi.infrastructure.mappers.intelligence;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.SimulationScenariosEntity;
import cv.igrp.RH_Service.sigdi.application.constants.SimulationScenarioStatus;
import cv.igrp.RH_Service.sigdi.domain.intelligence.models.SimulationResult;
import cv.igrp.RH_Service.sigdi.domain.intelligence.models.SimulationScenario;
import cv.igrp.RH_Service.sigdi.domain.intelligence.valueobject.SimulationScenarioId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SimulationScenarioMapper {

  private final SimulationResultMapper resultMapper;

  public SimulationScenario toDomain(SimulationScenariosEntity entity) {
    if (entity == null) return null;

    return SimulationScenario.reconstruct(
        SimulationScenarioId.from(entity.getId()),
        entity.getName(),
        entity.getParameters(),
        entity.getStatus() != null ? SimulationScenarioStatus.fromCodeOrThrow(entity.getStatus()) : null,
        List.of()
    );
  }

  public SimulationScenario toDomainFull(SimulationScenariosEntity entity) {
    if (entity == null) return null;

    List<SimulationResult> results = entity.getSimulationResults().stream()
        .map(resultMapper::toDomain)
        .collect(Collectors.toList());

    return SimulationScenario.reconstruct(
        SimulationScenarioId.from(entity.getId()),
        entity.getName(),
        entity.getParameters(),
        entity.getStatus() != null ? SimulationScenarioStatus.fromCodeOrThrow(entity.getStatus()) : null,
        results
    );
  }

  public SimulationScenariosEntity toEntity(SimulationScenario domain) {
    if (domain == null) return null;

    SimulationScenariosEntity entity = new SimulationScenariosEntity();
    entity.setId(domain.getId().getValor().getValor());
    entity.setName(domain.getName());
    entity.setParameters(domain.getParametersJson());
    entity.setStatus(domain.getStatus() != null ? domain.getStatus().getCode() : null);
    return entity;
  }
}

