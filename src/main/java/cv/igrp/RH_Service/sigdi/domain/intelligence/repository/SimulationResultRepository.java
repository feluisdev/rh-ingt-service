package cv.igrp.RH_Service.sigdi.domain.intelligence.repository;

import cv.igrp.RH_Service.sigdi.domain.intelligence.models.SimulationResult;
import cv.igrp.RH_Service.sigdi.domain.intelligence.valueobject.SimulationResultId;
import cv.igrp.RH_Service.sigdi.domain.intelligence.valueobject.SimulationScenarioId;

import java.util.List;
import java.util.Optional;

public interface SimulationResultRepository {

  SimulationResult save(SimulationResult result);

  Optional<SimulationResult> findById(SimulationResultId id);

  List<SimulationResult> findByScenarioId(SimulationScenarioId scenarioId);
}

