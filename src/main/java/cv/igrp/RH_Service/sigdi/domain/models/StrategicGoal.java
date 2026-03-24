package cv.igrp.RH_Service.sigdi.domain.models;

import cv.igrp.RH_Service.sigdi.domain.valueobject.StrategicGoalId;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class StrategicGoal {

  private final StrategicGoalId id;
  private final InstitutionalIdentity identityId;
  private final String title;
  private final String perspective;
  private final BigDecimal weight;
  private final String status;
  private final String description;

  private StrategicGoal(StrategicGoalId id, InstitutionalIdentity identityId, String title,
                        String perspective, BigDecimal weight, String status, String description) {

    if (title == null || title.trim().isEmpty()) {
      throw new IllegalArgumentException("title é obrigatório");
    }
    this.id = id;
    this.identityId = identityId;
    this.title = title;
    this.perspective = perspective;
    this.weight = (weight != null) ? weight : BigDecimal.valueOf(1.0);
    this.status = (status != null) ? status : "ACTIVE";
    this.description = description;
  }

  public static StrategicGoal create(InstitutionalIdentity identityId, String title, String perspective,
                                     BigDecimal weight, String description) {
    return new StrategicGoal(
        StrategicGoalId.gerarNovo(),
        identityId,
        title,
        perspective,
        weight,
        "ACTIVE",
        description
    );
  }

  public static StrategicGoal reconstruct(StrategicGoalId id, InstitutionalIdentity identityId, String title,
                                          String perspective, BigDecimal weight, String status, String description) {
    return new StrategicGoal(id, identityId, title, perspective, weight, status, description);
  }

  public boolean isActive() {
    return "ACTIVE".equalsIgnoreCase(status);
  }

  public boolean isFinancialPerspective() {
    return "FINANCIAL".equalsIgnoreCase(perspective);
  }
}
