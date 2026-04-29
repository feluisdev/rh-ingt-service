/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME */

package cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;


@Getter
@Setter
@IgrpEntity
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_strategic_goals")
public class StrategicGoalEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

  
    @Column(name="institution_id")
    private UUID institutionId;

  
    @NotBlank(message = "title is mandatory")
    @Column(name="title", nullable = false)
    private String title;

  
    @Column(name="perspective")
    private String perspective;

  
    @Column(name="weight")
    private BigDecimal weight;

  
    @Column(name="status")
    private String status;

  
    @Lob
    @Column(name="description", columnDefinition="TEXT")
    private String description;


    @Column(name="position_x")
    private Double positionX;


    @Column(name="position_y")
    private Double positionY;

  


    @OneToMany(mappedBy = "parentGoalId", fetch = FetchType.LAZY)
    private List<StrategicGoalEntity> stategyGoals = new ArrayList<>();

    @OneToMany(mappedBy = "goal", fetch = FetchType.LAZY, cascade = { CascadeType.ALL }, orphanRemoval = true)
    private List<StrategicIndicatorEntity> indicators = new ArrayList<>();
   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "identity_id")
   private InstitutionalIdentityEntity identityId;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "parent_goal_id")
   private StrategicGoalEntity parentGoalId;


}