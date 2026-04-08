/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME */

package cv.igrp.RH_Service.shared.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;


@Getter
@Setter
@IgrpEntity
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_goal_relationships",
  uniqueConstraints = {
    @UniqueConstraint(
      name = "uq_strategy_link_source_target",
      columnNames = {
        "source_goal_id","target_goal_id"
      }
    )
  })
public class StrategyMapLinkEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

  
    @Column(name="institution_id")
    private UUID institutionId;

  
    @NotNull(message = "sourceGoalId is mandatory")


  @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_goal_id", referencedColumnName = "id")
    private StrategicGoalEntity sourceGoalId;
    @NotNull(message = "targetGoalId is mandatory")


  @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_goal_id", referencedColumnName = "id")
    private StrategicGoalEntity targetGoalId;
    @Column(name="relationship_type")
    private String relationshipType;

  
}