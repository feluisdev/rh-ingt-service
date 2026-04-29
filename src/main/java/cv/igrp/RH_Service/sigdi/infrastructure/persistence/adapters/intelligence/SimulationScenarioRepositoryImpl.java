package cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters.intelligence;

import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.SimulationScenariosEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.SimulationScenariosEntityRepository;
import cv.igrp.RH_Service.sigdi.domain.intelligence.models.SimulationScenario;
import cv.igrp.RH_Service.sigdi.domain.intelligence.repository.SimulationScenarioRepository;
import cv.igrp.RH_Service.sigdi.domain.intelligence.valueobject.SimulationScenarioId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.intelligence.SimulationScenarioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SimulationScenarioRepositoryImpl implements SimulationScenarioRepository {

  private final SimulationScenariosEntityRepository jpaRepository;
  private final SimulationScenarioMapper mapper;

  @Transactional
  @Override
  public SimulationScenario save(SimulationScenario scenario) {
    SimulationScenariosEntity entity = mapper.toEntity(scenario);
    SimulationScenariosEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Transactional(readOnly = true)
  @Override
  public Optional<SimulationScenario> findById(SimulationScenarioId id) {
    return jpaRepository.findById(id.getValor().getValor())
        .map(mapper::toDomain);
  }

  @Transactional(readOnly = true)
  @Override
  public Optional<SimulationScenario> findByIdFull(SimulationScenarioId id) {
    return jpaRepository.findById(id.getValor().getValor())
        .map(mapper::toDomainFull);
  }

  @Transactional(readOnly = true)
  @Override
  public List<SimulationScenario> findAll(int page, int size) {
    return jpaRepository.findAll(PageRequest.of(page, size))
        .stream()
        .map(mapper::toDomain)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  @Override
  public long count() {
    return jpaRepository.count();
  }
}

