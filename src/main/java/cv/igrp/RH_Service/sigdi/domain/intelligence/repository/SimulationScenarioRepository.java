package cv.igrp.RH_Service.sigdi.domain.intelligence.repository;

import cv.igrp.RH_Service.sigdi.domain.intelligence.models.SimulationScenario;
import cv.igrp.RH_Service.sigdi.domain.intelligence.valueobject.SimulationScenarioId;

import java.util.Optional;

public interface SimulationScenarioRepository {

  SimulationScenario save(SimulationScenario scenario);

  Optional<SimulationScenario> findById(SimulationScenarioId id);

  Optional<SimulationScenario> findByIdFull(SimulationScenarioId id);
}

