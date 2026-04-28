package cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters.intelligence;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.SimulationResultsEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.SimulationResultsEntityRepository;
import cv.igrp.RH_Service.sigdi.domain.intelligence.models.SimulationResult;
import cv.igrp.RH_Service.sigdi.domain.intelligence.repository.SimulationResultRepository;
import cv.igrp.RH_Service.sigdi.domain.intelligence.valueobject.SimulationResultId;
import cv.igrp.RH_Service.sigdi.domain.intelligence.valueobject.SimulationScenarioId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.intelligence.SimulationResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SimulationResultRepositoryImpl implements SimulationResultRepository {

  private final SimulationResultsEntityRepository jpaRepository;
  private final SimulationResultMapper mapper;

  @Transactional
  @Override
  public SimulationResult save(SimulationResult result) {
    SimulationResultsEntity entity = mapper.toEntity(result);
    SimulationResultsEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Transactional(readOnly = true)
  @Override
  public Optional<SimulationResult> findById(SimulationResultId id) {
    return jpaRepository.findById(id.getValor().getValor())
        .map(mapper::toDomain);
  }

  @Transactional(readOnly = true)
  @Override
  public List<SimulationResult> findByScenarioId(SimulationScenarioId scenarioId) {
    return jpaRepository.findByScenarioId_Id(scenarioId.getValor().getValor()).stream()
        .map(mapper::toDomain)
        .toList();
  }
}

