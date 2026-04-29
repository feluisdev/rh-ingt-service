/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME */

package cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.ArrayList;


@Getter
@Setter
@IgrpEntity
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_okrs")
public class OkrEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

  
    @NotNull(message = "institutionId is mandatory")
    @Column(name="institution_id", nullable = false)
    private UUID institutionId;

  
    @Column(name="strategic_goal_id")
    private UUID strategicGoalId;

  
    @Column(name="cycle")
    private String cycle;

  
    @Column(name="title")
    private String title;

  
    @Column(name="status")
    private String status;

  


  @OneToMany(mappedBy = "okrId", fetch = FetchType.LAZY)
private List<KeyResultsEntity> keyResults = new ArrayList<>();
}