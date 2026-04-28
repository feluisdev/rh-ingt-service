/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME */

package cv.igrp.RH_Service.shared.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@IgrpEntity
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_activity_approval_history")
public class TaticalActivityHistoryEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

  
    @NotNull(message = "institutionId is mandatory")
    @Column(name="institution_id", nullable = false)
    private UUID institutionId;

  
    @Column(name="action")
    private String action;

  
    @Column(name="actor_id")
    private UUID actorId;

  
    @Column(name="delegated_by")
    private UUID delegatedBy;

  
    @Lob
    @Column(name="comment", columnDefinition="TEXT")
    private String comment;

  
    @Column(name="from_status")
    private String fromStatus;

  
    @Column(name="to_status")
    private String toStatus;

     @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "activity_id")
   private TacticalActivitiesEntity activityId;


}