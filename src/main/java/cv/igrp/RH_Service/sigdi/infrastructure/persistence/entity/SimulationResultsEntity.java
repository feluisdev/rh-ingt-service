/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME */

package cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@IgrpEntity
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_simulation_results")
public class SimulationResultsEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

  
    @NotNull(message = "activityId is mandatory")
    @Column(name="activity_id", nullable = false)
    private UUID activityId;

  
    @Column(name="recommendation")
    private String recommendation;

  
    @Column(name="impact_score")
    private BigDecimal impactScore;

  
    @Lob
    @Column(name="details", columnDefinition="TEXT")
    private String details;

     @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "scenario_id")
   private SimulationScenariosEntity scenarioId;


}