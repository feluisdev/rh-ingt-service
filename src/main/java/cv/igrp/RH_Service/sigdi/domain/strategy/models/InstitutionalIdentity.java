package cv.igrp.RH_Service.sigdi.domain.strategy.models;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.StrategicGoalsPerspective;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.InstitutionalIdentityId;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.InstitutionalValues;
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
  private final InstitutionalValues values;
  private final String versionComment;
  private final boolean isActive;
  private final boolean isArchived; // US001 — versões históricas são read-only
  private final List<StrategicGoal> goals;

  private InstitutionalIdentity(InstitutionalIdentityId id, Integer cycleYear, String mission,
                                String vision, InstitutionalValues values, String versionComment,
                                boolean isActive, boolean isArchived, List<StrategicGoal> goals) {
    if (cycleYear == null) throw new IllegalArgumentException("cycleYear é obrigatório");
    if (mission == null || mission.trim().isEmpty()) throw new IllegalArgumentException("mission é obrigatório");
    if (vision == null || vision.trim().isEmpty()) throw new IllegalArgumentException("vision é obrigatório");
    if (values == null) throw new IllegalArgumentException("values é obrigatório");

    this.id = id;
    this.cycleYear = cycleYear;
    this.mission = mission;
    this.vision = vision;
    this.values = values;
    this.versionComment = versionComment;
    this.isActive = isActive;
    this.isArchived = isArchived;
    this.goals = (goals != null) ? new ArrayList<>(goals) : new ArrayList<>();
  }

  public static InstitutionalIdentity create(Integer cycleYear, String mission, String vision,
                                             InstitutionalValues values, String versionComment) {
    return new InstitutionalIdentity(InstitutionalIdentityId.gerarNovo(), cycleYear, mission,
        vision, values, versionComment, false, false, new ArrayList<>());
  }

  public static InstitutionalIdentity reconstruct(InstitutionalIdentityId id, Integer cycleYear,
                                                  String mission, String vision, InstitutionalValues values,
                                                  String versionComment, boolean isActive,
                                                  boolean isArchived, List<StrategicGoal> goals) {
    return new InstitutionalIdentity(id, cycleYear, mission, vision, values, versionComment,
        isActive, isArchived, goals);
  }

  public List<StrategicGoal> getGoals() {
    return Collections.unmodifiableList(goals);
  }

  // ── Regras de negócio ─────────────────────────────────────────────

  /**
   * US001 — Ao ativar uma versão, a anterior é arquivada (read-only)
   */
  public InstitutionalIdentity activate() {
    if (isArchived)
      throw IgrpResponseStatusException.badRequest(
          "Versão histórica não pode ser reativada. Crie uma nova versão.");
    return new InstitutionalIdentity(this.id, this.cycleYear, this.mission, this.vision,
        this.values, this.versionComment, true, false, this.goals);
  }

  /**
   * US001 — Arquiva a versão atual (torna-a histórica/read-only)
   */
  public InstitutionalIdentity archive() {
    return new InstitutionalIdentity(this.id, this.cycleYear, this.mission, this.vision,
        this.values, this.versionComment, false, true, this.goals);
  }

  /**
   * US001 — Bloqueia edição de versões históricas
   */
  public void assertEditable() {
    if (isArchived)
      throw IgrpResponseStatusException.badRequest(
          "Esta versão é histórica e não pode ser editada.");
  }

  public StrategicGoal addGoal(String title, StrategicGoalsPerspective perspective,
                               java.math.BigDecimal weight, String description) {
    assertEditable();
    StrategicGoal goal = StrategicGoal.create(this.id, title, perspective, weight, description);
    goals.add(goal);
    return goal;
  }
}
