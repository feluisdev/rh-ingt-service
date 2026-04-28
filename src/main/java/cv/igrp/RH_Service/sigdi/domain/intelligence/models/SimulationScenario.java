package cv.igrp.RH_Service.sigdi.domain.intelligence.models;

import cv.igrp.RH_Service.sigdi.application.constants.SimulationScenarioStatus;
import cv.igrp.RH_Service.sigdi.domain.intelligence.valueobject.SimulationScenarioId;
import cv.igrp.RH_Service.sigdi.domain.intelligence.valueobject.SimulationScenarioParameters;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class SimulationScenario {

  private final SimulationScenarioId id;
  private final String name;
  private final SimulationScenarioParameters parameters;
  private final SimulationScenarioStatus status;
  private final List<SimulationResult> results;

  private SimulationScenario(SimulationScenarioId id, String name, SimulationScenarioParameters parameters,
      SimulationScenarioStatus status, List<SimulationResult> results) {
    if (id == null)
      throw new IllegalArgumentException("id é obrigatório");
    if (name == null || name.isBlank())
      throw new IllegalArgumentException("name é obrigatório");
    if (parameters == null)
      throw new IllegalArgumentException("parameters é obrigatório");
    this.id = id;
    this.name = name;
    this.parameters = parameters;
    this.status = (status != null) ? status : SimulationScenarioStatus.PROCESSING;
    this.results = (results != null) ? new ArrayList<>(results) : new ArrayList<>();
  }

  public static SimulationScenario create(String name, SimulationScenarioParameters parameters) {
    return new SimulationScenario(SimulationScenarioId.gerarNovo(), name, parameters,
        SimulationScenarioStatus.PROCESSING, new ArrayList<>());
  }

  public static SimulationScenario reconstruct(SimulationScenarioId id, String name,
      SimulationScenarioParameters parameters, SimulationScenarioStatus status,
      List<SimulationResult> results) {
    return new SimulationScenario(id, name, parameters, status, results);
  }

  public List<SimulationResult> getResults() {
    return Collections.unmodifiableList(results);
  }

  public String getParametersJson() {
    return parameters.toJson();
  }

  public List<UUID> getImpactedActivities() {
    return results.stream()
        .map(SimulationResult::getActivityId)
        .distinct()
        .collect(Collectors.toList());
  }

  public boolean isProcessing() {
    return SimulationScenarioStatus.PROCESSING.equals(status);
  }

  public boolean isCompleted() {
    return SimulationScenarioStatus.COMPLETED.equals(status);
  }

  public SimulationScenario addResult(SimulationResult result) {
    if (result == null)
      throw new IllegalArgumentException("result é obrigatório");
    if (isCompleted())
      throw new IllegalStateException("scenario COMPLETED não aceita novos resultados");
    if (!this.id.equals(result.getScenarioId()))
      throw new IllegalArgumentException("result não pertence ao scenario: " + id.getStringValor());
    List<SimulationResult> newResults = new ArrayList<>(this.results);
    newResults.add(result);
    return new SimulationScenario(this.id, this.name, this.parameters, this.status, newResults);
  }

  public SimulationScenario complete() {
    if (isCompleted())
      return this;
    if (!isProcessing())
      throw new IllegalStateException("Apenas scenario PROCESSING pode ser completado");
    return new SimulationScenario(this.id, this.name, this.parameters,
        SimulationScenarioStatus.COMPLETED, this.results);
  }
}
