package cv.igrp.RH_Service.sigdi.domain.strategy.models;

import cv.igrp.RH_Service.shared.application.constants.Estado;
import cv.igrp.RH_Service.sigdi.application.constants.StrategicGoalsPerspective;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.InstitutionalIdentityId;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class StrategicGoal {

  private final StrategicGoalId id;
  private final UUID institutionId;
  private final InstitutionalIdentityId identityId;
  private final String title;
  private final StrategicGoalsPerspective perspective;
  private final BigDecimal weight;
  private final Estado status;
  private final String description;
  private final Double positionX;
  private final Double positionY;
  private final java.util.List<StrategicIndicator> indicators;

  private StrategicGoal(StrategicGoalId id, UUID institutionId, InstitutionalIdentityId identityId,
                        String title, StrategicGoalsPerspective perspective, BigDecimal weight,
                        Estado status, String description, Double positionX, Double positionY,
                        java.util.List<StrategicIndicator> indicators) {
    if (title == null || title.trim().isEmpty())
      throw new IllegalArgumentException("title é obrigatório");
    if (identityId == null)
      throw new IllegalArgumentException("identityId é obrigatório");

    this.id = id;
    this.institutionId = institutionId;
    this.identityId = identityId;
    this.title = title;
    this.perspective = perspective;
    this.weight = (weight != null) ? weight : BigDecimal.valueOf(1.0);
    this.status = (status != null) ? status : Estado.A;
    this.description = description;
    this.positionX = positionX;
    this.positionY = positionY;
    this.indicators = (indicators != null) ? indicators : new java.util.ArrayList<>();
  }

  public static StrategicGoal create(UUID institutionId, InstitutionalIdentityId identityId,
                                     String title, StrategicGoalsPerspective perspective,
                                     BigDecimal weight, String description,
                                     java.util.List<StrategicIndicator> indicators) {
    return new StrategicGoal(StrategicGoalId.gerarNovo(), institutionId, identityId, title,
        perspective, weight, Estado.A, description, null, null, indicators);
  }

  public static StrategicGoal reconstruct(StrategicGoalId id, UUID institutionId,
                                          InstitutionalIdentityId identityId, String title,
                                          StrategicGoalsPerspective perspective, BigDecimal weight,
                                          Estado status, String description,
                                          Double positionX, Double positionY,
                                          java.util.List<StrategicIndicator> indicators) {
    return new StrategicGoal(id, institutionId, identityId, title, perspective, weight, status,
        description, positionX, positionY, indicators);
  }

  public StrategicGoal update(String newTitle, String newDescription, BigDecimal newWeight, java.util.List<StrategicIndicator> newIndicators) {
    String title = (newTitle != null && !newTitle.isBlank()) ? newTitle : this.title;
    String description = newDescription != null ? newDescription : this.description;
    BigDecimal weight = newWeight != null ? newWeight : this.weight;
    return new StrategicGoal(this.id, this.institutionId, this.identityId, title, this.perspective,
        weight, this.status, description, this.positionX, this.positionY, newIndicators != null ? newIndicators : this.indicators);
  }

  public StrategicGoal cancel() {
    return new StrategicGoal(this.id, this.institutionId, this.identityId, this.title,
        this.perspective, this.weight, Estado.I, this.description, this.positionX, this.positionY, this.indicators);
  }

  public StrategicGoal updatePosition(Double x, Double y) {
    return new StrategicGoal(this.id, this.institutionId, this.identityId, this.title,
        this.perspective, this.weight, this.status, this.description, x, y, this.indicators);
  }

  public boolean isActive() {
    return Estado.A.equals(this.status);
  }

  public boolean isFinancial() {
    return StrategicGoalsPerspective.FINANCIAL.equals(this.perspective);
  }
}
