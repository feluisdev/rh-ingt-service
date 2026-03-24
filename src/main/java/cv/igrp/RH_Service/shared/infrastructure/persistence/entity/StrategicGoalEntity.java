/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME */

package cv.igrp.RH_Service.shared.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


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

     @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "identity_id")
   private InstitutionalIdentityEntity identityId;


}