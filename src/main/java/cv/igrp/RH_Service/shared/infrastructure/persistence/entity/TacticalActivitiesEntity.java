/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME */

package cv.igrp.RH_Service.shared.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.math.BigDecimal;


@Getter
@Setter
@IgrpEntity
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_tactical_activities")
public class TacticalActivitiesEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

  
    @NotNull(message = "strategicGoalId is mandatory")
    @Column(name="strategic_goal_id", nullable = false)
    private UUID strategicGoalId;

  
    @Column(name="organic_unit_id")
    private String organicUnitId;

  
    @Column(name="title")
    private String title;

  
    @Lob
    @Column(name="description_what", columnDefinition="TEXT")
    private String descriptionWhat;

  
    @Lob
    @Column(name="justification_why", columnDefinition="TEXT")
    private String justificationWhy;

  
    @Column(name="location_where")
    private String locationWhere;

  
    @Column(name="responsible_who")
    private String responsibleWho;

  
    @Lob
    @Column(name="methodology_how", columnDefinition="TEXT")
    private String methodologyHow;

  
    @Column(name="start_date")
    private LocalDate startDate;

  
    @Column(name="end_date")
    private LocalDate endDate;

  
    @Column(name="budget_estimated")
    private BigDecimal budgetEstimated;

  
    @Column(name="economic_classifier")
    private String economicClassifier;

  
    @Column(name="status")
    private String status;

  
    @Column(name="version")
    private Integer version;

  
}