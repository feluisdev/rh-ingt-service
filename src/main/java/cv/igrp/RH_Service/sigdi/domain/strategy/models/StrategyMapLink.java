package cv.igrp.RH_Service.sigdi.domain.strategy.models;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.StrategyMapRelationshipType;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategyMapLinkId;
import lombok.Getter;

@Getter
public class StrategyMapLink {

  private final StrategyMapLinkId id;
  private final StrategicGoalId sourceGoalId; // referência por ID, não objeto completo
  private final StrategicGoalId targetGoalId; // referência por ID, não objeto completo
  private final StrategyMapRelationshipType relationshipType;

  private StrategyMapLink(StrategyMapLinkId id, StrategicGoalId sourceGoalId,
                          StrategicGoalId targetGoalId, StrategyMapRelationshipType relationshipType) {
    if (sourceGoalId.equals(targetGoalId)) {
      throw IgrpResponseStatusException.badRequest("O link não pode ter source igual ao target");
    }
    this.id = id;
    this.sourceGoalId = sourceGoalId;
    this.targetGoalId = targetGoalId;
    this.relationshipType = relationshipType;
  }

  public static StrategyMapLink create(StrategicGoalId sourceGoalId, StrategicGoalId targetGoalId) {
    return new StrategyMapLink(
        StrategyMapLinkId.gerarNovo(),
        sourceGoalId,
        targetGoalId,
        StrategyMapRelationshipType.CAUSE_EFFECT
    );
  }

  public static StrategyMapLink reconstruct(StrategyMapLinkId id, StrategicGoalId sourceGoalId,
                                            StrategicGoalId targetGoalId,
                                            StrategyMapRelationshipType relationshipType) {
    return new StrategyMapLink(id, sourceGoalId, targetGoalId, relationshipType);
  }

  public boolean isCauseEffect() {
    return StrategyMapRelationshipType.CAUSE_EFFECT.equals(relationshipType);
  }
}
