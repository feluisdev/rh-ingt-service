package cv.igrp.RH_Service.sigdi.domain.strategy.models;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.StrategyMapRelationshipType;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategyMapLinkId;
import lombok.Getter;

import java.util.UUID;

@Getter
public class StrategyMapLink {

  private final StrategyMapLinkId id;
  private final UUID institutionId;
  private final StrategicGoalId sourceGoalId;
  private final StrategicGoalId targetGoalId;
  private final StrategyMapRelationshipType relationshipType;

  private StrategyMapLink(StrategyMapLinkId id, UUID institutionId, StrategicGoalId sourceGoalId,
                          StrategicGoalId targetGoalId,
                          StrategyMapRelationshipType relationshipType) {
    if (sourceGoalId.equals(targetGoalId))
      throw IgrpResponseStatusException.badRequest("O link não pode ter source igual ao target");
    this.id = id;
    this.institutionId = institutionId;
    this.sourceGoalId = sourceGoalId;
    this.targetGoalId = targetGoalId;
    this.relationshipType = relationshipType;
  }

  public static StrategyMapLink create(UUID institutionId, StrategicGoalId sourceGoalId,
                                       StrategicGoalId targetGoalId) {
    return new StrategyMapLink(StrategyMapLinkId.gerarNovo(), institutionId, sourceGoalId,
        targetGoalId, StrategyMapRelationshipType.CAUSE_EFFECT);
  }

  public static StrategyMapLink create(UUID institutionId, StrategicGoalId sourceGoalId,
                                       StrategicGoalId targetGoalId,
                                       StrategyMapRelationshipType relationshipType) {
    return new StrategyMapLink(StrategyMapLinkId.gerarNovo(), institutionId, sourceGoalId,
        targetGoalId, relationshipType);
  }

  public static StrategyMapLink reconstruct(StrategyMapLinkId id, UUID institutionId,
                                            StrategicGoalId sourceGoalId,
                                            StrategicGoalId targetGoalId,
                                            StrategyMapRelationshipType relationshipType) {
    return new StrategyMapLink(id, institutionId, sourceGoalId, targetGoalId, relationshipType);
  }

  public boolean isCauseEffect() {
    return StrategyMapRelationshipType.CAUSE_EFFECT.equals(relationshipType);
  }
}
