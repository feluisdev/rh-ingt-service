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
  private final Integer year;
  private final java.util.List<StrategicIndicator> indicators;

  private StrategicGoal(StrategicGoalId id, UUID institutionId, InstitutionalIdentityId identityId,
                        String title, StrategicGoalsPerspective perspective, BigDecimal weight,
                        Estado status, String description, Double positionX, Double positionY,
                        Integer year, java.util.List<StrategicIndicator> indicators) {
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
    this.year = year;
    this.indicators = (indicators != null) ? indicators : new java.util.ArrayList<>();
  }

  public static StrategicGoal create(UUID institutionId, InstitutionalIdentityId identityId,
                                     String title, StrategicGoalsPerspective perspective,
                                     BigDecimal weight, String description, Integer year,
                                     java.util.List<StrategicIndicator> indicators) {
    return new StrategicGoal(StrategicGoalId.gerarNovo(), institutionId, identityId, title,
        perspective, weight, Estado.A, description, null, null, year, indicators);
  }

  public static StrategicGoal reconstruct(StrategicGoalId id, UUID institutionId,
                                          InstitutionalIdentityId identityId, String title,
                                          StrategicGoalsPerspective perspective, BigDecimal weight,
                                          Estado status, String description,
                                          Double positionX, Double positionY, Integer year,
                                          java.util.List<StrategicIndicator> indicators) {
    return new StrategicGoal(id, institutionId, identityId, title, perspective, weight, status,
        description, positionX, positionY, year, indicators);
  }

  /**
   * FIX-09 / A-124-02: {@code newPerspective} is a REAL parameter, and its position -- between the
   * year and the indicators, not appended at the end -- is deliberate.
   *
   * <p>Until Phase 130 this method copied {@code this.perspective} and never received one, so the
   * client sent the field, the server dropped it, and the product answered "gravado com sucesso".
   * Adding the parameter in the MIDDLE changes the arity and the signature at once, which turns
   * every old five-argument call into a COMPILATION ERROR. That is the point: a sixth parameter
   * appended at the end with a default value would have left the one call site that mattered
   * ({@code UpdateStrategicGoalsCommandHandler}) silently compiling and silently dropping the
   * field, which is the very defect being removed.
   *
   * <p>Semantics match every other argument here: NULL PRESERVES the current value. An edit that
   * omits the perspective keeps the one the goal already has.
   */
  public StrategicGoal update(String newTitle, String newDescription, BigDecimal newWeight,
                               Integer newYear, StrategicGoalsPerspective newPerspective,
                               java.util.List<StrategicIndicator> newIndicators) {
    String title = (newTitle != null && !newTitle.isBlank()) ? newTitle : this.title;
    String description = newDescription != null ? newDescription : this.description;
    BigDecimal weight = newWeight != null ? newWeight : this.weight;
    Integer year = newYear != null ? newYear : this.year;
    StrategicGoalsPerspective perspective = newPerspective != null ? newPerspective : this.perspective;
    return new StrategicGoal(this.id, this.institutionId, this.identityId, title, perspective,
        weight, this.status, description, this.positionX, this.positionY, year, newIndicators != null ? newIndicators : this.indicators);
  }

  public StrategicGoal cancel() {
    return new StrategicGoal(this.id, this.institutionId, this.identityId, this.title,
        this.perspective, this.weight, Estado.I, this.description, this.positionX, this.positionY, this.year, this.indicators);
  }

  public StrategicGoal updatePosition(Double x, Double y) {
    return new StrategicGoal(this.id, this.institutionId, this.identityId, this.title,
        this.perspective, this.weight, this.status, this.description, x, y, this.year, this.indicators);
  }

  public boolean isActive() {
    return Estado.A.equals(this.status);
  }

  public boolean isFinancial() {
    return StrategicGoalsPerspective.FINANCIAL.equals(this.perspective);
  }
}
