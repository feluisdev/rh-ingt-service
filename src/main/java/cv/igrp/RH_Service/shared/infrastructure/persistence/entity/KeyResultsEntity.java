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
import java.util.List;
import java.util.ArrayList;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;


@Getter
@Setter
@IgrpEntity
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_key_results")
public class KeyResultsEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

  
    @NotBlank(message = "title is mandatory")
    @Column(name="title", nullable = false)
    private String title;

  
    @Column(name="target_value")
    private BigDecimal targetValue;

  
    @Column(name="current_value")
    private BigDecimal currentValue;

  
    @Column(name="metric_unit")
    private String metricUnit;

  


  @OneToMany(mappedBy = "keyResultId", fetch = FetchType.LAZY, cascade = { CascadeType.ALL }, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
private List<KeyResultsCheckinEntity> keyResultCheckins = new ArrayList<>();   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "activity_id")
   private TacticalActivitiesEntity activityId;


}