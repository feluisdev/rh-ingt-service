package cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters.intelligence;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.SimulationScenariosEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.SimulationScenariosEntityRepository;
import cv.igrp.RH_Service.sigdi.domain.intelligence.models.SimulationScenario;
import cv.igrp.RH_Service.sigdi.domain.intelligence.repository.SimulationScenarioRepository;
import cv.igrp.RH_Service.sigdi.domain.intelligence.valueobject.SimulationScenarioId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.intelligence.SimulationScenarioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SimulationScenarioRepositoryImpl implements SimulationScenarioRepository {

  private final SimulationScenariosEntityRepository jpaRepository;
  private final SimulationScenarioMapper mapper;

  @Override
  public SimulationScenario save(SimulationScenario scenario) {
    SimulationScenariosEntity entity = mapper.toEntity(scenario);
    SimulationScenariosEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Override
  public Optional<SimulationScenario> findById(SimulationScenarioId id) {
    return jpaRepository.findById(id.getValor().getValor())
        .map(mapper::toDomain);
  }

  @Override
  public Optional<SimulationScenario> findByIdFull(SimulationScenarioId id) {
    return jpaRepository.findById(id.getValor().getValor())
        .map(mapper::toDomainFull);
  }
}

