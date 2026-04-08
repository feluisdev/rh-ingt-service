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
@Table(name = "t_change_requests")
public class ChangeRequestEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

  
    @NotNull(message = "institutionId is mandatory")
    @Column(name="institution_id", nullable = false)
    private UUID institutionId;

  
    @Lob
    @Column(name="field_name", columnDefinition="TEXT")
    private String fieldName;

  
    @Lob
    @Column(name="current_value", columnDefinition="TEXT")
    private String currentValue;

  
    @Lob
    @Column(name="justification", columnDefinition="TEXT")
    private String justification;

  
    @Column(name="status")
    private String status;

  
    @Column(name="reviewer_id")
    private UUID reviewerId;

  
    @Lob
    @Column(name="reviewer_comment", columnDefinition="TEXT")
    private String reviewerComment;

     @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "activity_id")
   private TacticalActivitiesEntity activityId;


}