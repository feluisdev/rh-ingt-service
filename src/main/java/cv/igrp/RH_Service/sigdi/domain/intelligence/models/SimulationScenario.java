package cv.igrp.RH_Service.sigdi.domain.intelligence.models;

import cv.igrp.RH_Service.sigdi.application.constants.SimulationScenarioStatus;
import cv.igrp.RH_Service.sigdi.domain.intelligence.valueobject.SimulationScenarioId;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class SimulationScenario {

  private final SimulationScenarioId id;
  private final String name;
  private final String parametersJson;
  private final SimulationScenarioStatus status;
  private final List<SimulationResult> results;

  private SimulationScenario(SimulationScenarioId id, String name, String parametersJson,
                             SimulationScenarioStatus status, List<SimulationResult> results) {
    if (id == null) throw new IllegalArgumentException("id é obrigatório");
    if (name == null || name.isBlank()) throw new IllegalArgumentException("name é obrigatório");
    this.id = id;
    this.name = name;
    this.parametersJson = parametersJson;
    this.status = (status != null) ? status : SimulationScenarioStatus.PROCESSING;
    this.results = (results != null) ? new ArrayList<>(results) : new ArrayList<>();
  }

  public static SimulationScenario create(String name, String parametersJson) {
    return new SimulationScenario(SimulationScenarioId.gerarNovo(), name, parametersJson,
        SimulationScenarioStatus.PROCESSING, new ArrayList<>());
  }

  public static SimulationScenario reconstruct(SimulationScenarioId id, String name,
                                               String parametersJson, SimulationScenarioStatus status,
                                               List<SimulationResult> results) {
    return new SimulationScenario(id, name, parametersJson, status, results);
  }

  public List<SimulationResult> getResults() {
    return Collections.unmodifiableList(results);
  }

  public boolean isProcessing() {
    return SimulationScenarioStatus.PROCESSING.equals(status);
  }

  public boolean isCompleted() {
    return SimulationScenarioStatus.COMPLETED.equals(status);
  }

  public SimulationScenario addResult(SimulationResult result) {
    if (result == null) throw new IllegalArgumentException("result é obrigatório");
    if (!this.id.equals(result.getScenarioId()))
      throw new IllegalArgumentException("result não pertence ao scenario: " + id.getStringValor());
    List<SimulationResult> newResults = new ArrayList<>(this.results);
    newResults.add(result);
    return new SimulationScenario(this.id, this.name, this.parametersJson, this.status, newResults);
  }

  public SimulationScenario complete() {
    return new SimulationScenario(this.id, this.name, this.parametersJson,
        SimulationScenarioStatus.COMPLETED, this.results);
  }
}

