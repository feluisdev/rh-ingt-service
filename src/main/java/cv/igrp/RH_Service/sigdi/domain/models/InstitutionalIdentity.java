package cv.igrp.RH_Service.sigdi.domain.models;

import cv.igrp.RH_Service.sigdi.domain.valueobject.InstitutionalIdentityId;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class InstitutionalIdentity {

  private final InstitutionalIdentityId id;
  private final Integer cycleYear;
  private final String mission;
  private final String vision;
  private final String valuesJson;
  private final String versionComment;
  private final boolean isActive;

  private final List<StrategicGoal> goals;

  private InstitutionalIdentity(InstitutionalIdentityId id, Integer cycleYear, String mission, String vision,
                                String valuesJson, String versionComment, boolean isActive,
                                List<StrategicGoal> goals) {

    if (cycleYear == null) {
      throw new IllegalArgumentException("cycleYear é obrigatório");
    }
    if (mission == null || mission.trim().isEmpty()) {
      throw new IllegalArgumentException("mission é obrigatório");
    }
    if (vision == null || vision.trim().isEmpty()) {
      throw new IllegalArgumentException("vision é obrigatório");
    }

    this.id = id;
    this.cycleYear = cycleYear;
    this.mission = mission;
    this.vision = vision;
    this.valuesJson = valuesJson;
    this.versionComment = versionComment;
    this.isActive = isActive;
    this.goals = (goals != null) ? new ArrayList<>(goals) : new ArrayList<>();
  }

  public static InstitutionalIdentity create(Integer cycleYear, String mission, String vision,
                                             String valuesJson, String versionComment) {
    return new InstitutionalIdentity(
        InstitutionalIdentityId.gerarNovo(),
        cycleYear,
        mission,
        vision,
        valuesJson,
        versionComment,
        false,
        new ArrayList<>()
    );
  }

  public static InstitutionalIdentity reconstruct(InstitutionalIdentityId id, Integer cycleYear, String mission, String vision,
                                                  String valuesJson, String versionComment, boolean isActive,
                                                  List<StrategicGoal> goals) {
    return new InstitutionalIdentity(
        id,
        cycleYear,
        mission,
        vision,
        valuesJson,
        versionComment,
        isActive,
        goals
    );
  }

  // Retorna lista imutável de objetivos
  public List<StrategicGoal> getGoals() {
    return Collections.unmodifiableList(goals);
  }

  // Regras de negócio
  public InstitutionalIdentity activate() {
    return new InstitutionalIdentity(
        this.id,
        this.cycleYear,
        this.mission,
        this.vision,
        this.valuesJson,
        this.versionComment,
        true,
        this.goals
    );
  }
}
