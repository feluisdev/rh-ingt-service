package cv.igrp.RH_Service.sigdi.domain.strategy.models;

import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.InstitutionalIdentityId;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.InstitutionalValues;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Getter
public class InstitutionalIdentity {

  private final InstitutionalIdentityId id;
  private final UUID institutionId;
  private final Integer cycleYear;
  private final String mission;
  private final String vision;
  private final InstitutionalValues values;
  private final String versionComment;
  private final boolean isActive;
  private final List<StrategicGoal> goals;

  private InstitutionalIdentity(InstitutionalIdentityId id, UUID institutionId, Integer cycleYear,
      String mission, String vision, InstitutionalValues values, String versionComment,
      boolean isActive, List<StrategicGoal> goals) {
    if (cycleYear == null)
      throw new IllegalArgumentException("cycleYear é obrigatório");
    if (mission == null || mission.trim().isEmpty())
      throw new IllegalArgumentException("mission é obrigatório");
    if (vision == null || vision.trim().isEmpty())
      throw new IllegalArgumentException("vision é obrigatório");
    if (values == null)
      throw new IllegalArgumentException("values é obrigatório");

    this.id = id;
    this.institutionId = institutionId;
    this.cycleYear = cycleYear;
    this.mission = mission;
    this.vision = vision;
    this.values = values;
    this.versionComment = versionComment;
    this.isActive = isActive;
    this.goals = (goals != null) ? new ArrayList<>(goals) : new ArrayList<>();
  }

  public static InstitutionalIdentity create(UUID institutionId, Integer cycleYear, String mission,
      String vision, InstitutionalValues values, String versionComment) {
    return new InstitutionalIdentity(InstitutionalIdentityId.gerarNovo(), institutionId, cycleYear,
        mission, vision, values, versionComment, true, new ArrayList<>());
  }

  public static InstitutionalIdentity reconstruct(InstitutionalIdentityId id, UUID institutionId,
      Integer cycleYear, String mission, String vision, InstitutionalValues values,
      String versionComment, boolean isActive, List<StrategicGoal> goals) {
    return new InstitutionalIdentity(id, institutionId, cycleYear, mission, vision, values,
        versionComment, isActive, goals);
  }

  public List<StrategicGoal> getGoals() {
    return Collections.unmodifiableList(goals);
  }

  public InstitutionalIdentity activate() {
    return new InstitutionalIdentity(this.id, this.institutionId, this.cycleYear, this.mission,
        this.vision, this.values, this.versionComment, true, this.goals);
  }

  public InstitutionalIdentity deactivate() {
    return new InstitutionalIdentity(this.id, this.institutionId, this.cycleYear, this.mission,
        this.vision, this.values, this.versionComment, false, this.goals);
  }
}
