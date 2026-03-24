package cv.igrp.RH_Service.sigdi.domain.models;

import cv.igrp.RH_Service.shared.application.constants.Estado;
import cv.igrp.RH_Service.sigdi.application.constants.StrategicGoalsPerspective;
import cv.igrp.RH_Service.sigdi.domain.valueobject.StrategicGoalId;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class StrategicGoal {

  private final StrategicGoalId id;
  private final InstitutionalIdentity identity;
  private final String title;
  private final StrategicGoalsPerspective perspective;
  private final BigDecimal weight;
  private final Estado status;
  private final String description;

  private StrategicGoal(StrategicGoalId id, InstitutionalIdentity identity, String title,
                        StrategicGoalsPerspective perspective, BigDecimal weight, Estado status, String description) {

    if (title == null || title.trim().isEmpty()) {
      throw new IllegalArgumentException("title é obrigatório");
    }
    this.id = id;
    this.identity = identity;
    this.title = title;
    this.perspective = perspective;
    this.weight = (weight != null) ? weight : BigDecimal.valueOf(1.0);
    this.status = (status != null) ? status : Estado.A;
    this.description = description;
  }

  public static StrategicGoal create(InstitutionalIdentity identity, String title, StrategicGoalsPerspective perspective,
                                     BigDecimal weight, String description) {
    return new StrategicGoal(
        StrategicGoalId.gerarNovo(),
        identity,
        title,
        perspective,
        weight,
        Estado.A,
        description
    );
  }

  public static StrategicGoal reconstruct(StrategicGoalId id, InstitutionalIdentity identity, String title,
                                          StrategicGoalsPerspective perspective, BigDecimal weight, Estado status, String description) {
    return new StrategicGoal(id, identity, title, perspective, weight, status, description);
  }

  public boolean isFinancial() {
    return StrategicGoalsPerspective.FINANCIAL.equals(this.perspective);
  }


}
