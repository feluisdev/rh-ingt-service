package cv.igrp.RH_Service.sigdi.domain.intelligence.valueobject;

import com.fasterxml.jackson.databind.ObjectMapper;
import cv.igrp.RH_Service.sigdi.application.constants.SimulationScenarioScope;
import cv.igrp.RH_Service.sigdi.application.constants.SimulationScenarioType;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class SimulationScenarioParameters {

  private static final ObjectMapper mapper = new ObjectMapper();

  private final SimulationScenarioType type;
  private final BigDecimal percentage;
  private final SimulationScenarioScope scope;
  private final String departmentId;
  private final String priorityThreshold;

  private SimulationScenarioParameters(SimulationScenarioType type, BigDecimal percentage,
                                       SimulationScenarioScope scope, String departmentId,
                                       String priorityThreshold) {
    if (type == null) throw new IllegalArgumentException("type é obrigatório");
    if (percentage == null) throw new IllegalArgumentException("percentage é obrigatório");
    if (percentage.compareTo(BigDecimal.ZERO) <= 0 || percentage.compareTo(new BigDecimal("100")) > 0) {
      throw new IllegalArgumentException("percentage deve ser > 0 e <= 100");
    }
    if (scope == null) throw new IllegalArgumentException("scope é obrigatório");
    if (SimulationScenarioScope.DEPARTMENT_ID.equals(scope) && (departmentId == null || departmentId.isBlank())) {
      throw new IllegalArgumentException("departmentId é obrigatório quando scope=DEPARTMENT_ID");
    }
    this.type = type;
    this.percentage = percentage;
    this.scope = scope;
    this.departmentId = departmentId;
    this.priorityThreshold = priorityThreshold;
  }

  public static SimulationScenarioParameters of(SimulationScenarioType type, BigDecimal percentage,
                                                SimulationScenarioScope scope, String departmentId,
                                                String priorityThreshold) {
    return new SimulationScenarioParameters(type, percentage, scope, departmentId, priorityThreshold);
  }

  public static SimulationScenarioParameters fromJson(String json) {
    if (json == null || json.isBlank()) {
      throw new IllegalArgumentException("parametersJson é obrigatório");
    }
    try {
      Map<String, Object> map = mapper.readValue(json, Map.class);

      String typeRaw = readString(map, "type");
      String scopeRaw = readString(map, "scope");
      String priority = readString(map, "priority_threshold");
      String departmentId = readString(map, "departmentId");
      if (departmentId == null) departmentId = readString(map, "department_id");

      BigDecimal percentage = readBigDecimal(map, "percentage");
      if (percentage == null) percentage = readBigDecimal(map, "cut_percentage");

      SimulationScenarioType type = (typeRaw != null)
          ? SimulationScenarioType.fromCodeOrThrow(typeRaw)
          : SimulationScenarioType.BUDGET_CUT;

      SimulationScenarioScope scope = (scopeRaw != null)
          ? SimulationScenarioScope.fromCodeOrThrow(scopeRaw)
          : SimulationScenarioScope.GLOBAL;

      return new SimulationScenarioParameters(type, percentage, scope, departmentId, priority);
    } catch (Exception e) {
      throw new IllegalArgumentException("JSON inválido para SimulationScenarioParameters: " + json, e);
    }
  }

  public String toJson() {
    try {
      Map<String, Object> map = new LinkedHashMap<>();
      map.put("type", type.getCode());
      map.put("percentage", percentage);
      map.put("scope", scope.getCode());
      if (departmentId != null && !departmentId.isBlank()) map.put("departmentId", departmentId);
      if (priorityThreshold != null && !priorityThreshold.isBlank()) map.put("priority_threshold", priorityThreshold);
      return mapper.writeValueAsString(map);
    } catch (Exception e) {
      throw new IllegalStateException("Erro ao serializar SimulationScenarioParameters", e);
    }
  }

  private static String readString(Map<String, Object> map, String key) {
    Object value = map.get(key);
    if (value == null) return null;
    String s = value.toString();
    return s.isBlank() ? null : s;
  }

  private static BigDecimal readBigDecimal(Map<String, Object> map, String key) {
    Object value = map.get(key);
    if (value == null) return null;
    String s = value.toString();
    if (s.isBlank()) return null;
    return new BigDecimal(s);
  }
}

