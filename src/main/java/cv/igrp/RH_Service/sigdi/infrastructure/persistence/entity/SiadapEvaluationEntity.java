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

import org.hibernate.envers.Audited;

@Audited
@Getter
@Setter
@IgrpEntity
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_siadap_evaluations")
public class SiadapEvaluationEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

  
    @NotBlank(message = "employeeId is mandatory")
    @Column(name="employee_id", nullable = false)
    private String employeeId;

  
    @Column(name="year")
    private String year;

  
    @Column(name="objectives_score")
    private BigDecimal objectivesScore;

  
    @Column(name="competencies_score")
    private BigDecimal competenciesScore;

  
    @Column(name="final_score")
    private BigDecimal finalScore;

  
    @Column(name="merit_rating")
    private String meritRating;

  
    @Column(name="validated_quota")
    private boolean validatedQuota;

  
}