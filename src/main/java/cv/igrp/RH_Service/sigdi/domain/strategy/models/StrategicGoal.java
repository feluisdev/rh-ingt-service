package cv.igrp.RH_Service.sigdi.domain.strategy.models;

import cv.igrp.RH_Service.shared.application.constants.Estado;
import cv.igrp.RH_Service.sigdi.application.constants.StrategicGoalsPerspective;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.InstitutionalIdentityId;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class StrategicGoal {

  private final StrategicGoalId id;
  private final InstitutionalIdentityId identityId; // referência por ID, não objeto completo
  private final String title;
  private final StrategicGoalsPerspective perspective;
  private final BigDecimal weight;
  private final Estado status;
  private final String description;

  private StrategicGoal(StrategicGoalId id, InstitutionalIdentityId identityId, String title,
                        StrategicGoalsPerspective perspective, BigDecimal weight, Estado status,
                        String description) {
    if (title == null || title.trim().isEmpty()) {
      throw new IllegalArgumentException("title é obrigatório");
    }
    if (identityId == null) {
      throw new IllegalArgumentException("identityId é obrigatório");
    }
    this.id = id;
    this.identityId = identityId;
    this.title = title;
    this.perspective = perspective;
    this.weight = (weight != null) ? weight : BigDecimal.valueOf(1.0);
    this.status = (status != null) ? status : Estado.A;
    this.description = description;
  }

  public static StrategicGoal create(InstitutionalIdentityId identityId, String title,
                                     StrategicGoalsPerspective perspective, BigDecimal weight,
                                     String description) {
    return new StrategicGoal(
        StrategicGoalId.gerarNovo(),
        identityId,
        title,
        perspective,
        weight,
        Estado.A,
        description
    );
  }

  public static StrategicGoal reconstruct(StrategicGoalId id, InstitutionalIdentityId identityId,
                                          String title, StrategicGoalsPerspective perspective,
                                          BigDecimal weight, Estado status, String description) {
    return new StrategicGoal(id, identityId, title, perspective, weight, status, description);
  }

  /** Atualiza apenas title, description e weight — perspective é imutável. */
  public StrategicGoal update(String newTitle, String newDescription, BigDecimal newWeight) {
    String title = (newTitle != null && !newTitle.isBlank()) ? newTitle : this.title;
    String description = newDescription != null ? newDescription : this.description;
    BigDecimal weight = newWeight != null ? newWeight : this.weight;
    return StrategicGoal.reconstruct(this.id, this.identityId, title, this.perspective, weight, this.status, description);
  }

  public boolean isActive() {
    return Estado.A.equals(this.status);
  }

  public boolean isFinancial() {
    return StrategicGoalsPerspective.FINANCIAL.equals(this.perspective);
  }

}
