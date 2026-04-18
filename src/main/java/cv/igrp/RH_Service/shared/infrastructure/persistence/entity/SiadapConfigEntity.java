/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME */

package cv.igrp.RH_Service.shared.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;


@Getter
@Setter
@IgrpEntity
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_siadap_config")
public class SiadapConfigEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

  
    @NotNull(message = "fiscalYear is mandatory")
    @Column(name="fiscal_year", nullable = false, unique = true)
    private Integer fiscalYear;

  
    @Column(name="good_score")
    private BigDecimal goodScore;

  
    @Column(name="excellent_score")
    private BigDecimal excellentScore;

  
    @Column(name="excellent_quota")
    private BigDecimal excellentQuota;

  
    @Column(name="min_collaborators_for_quota")
    private Integer minCollaboratorsForQuota;

  
    @Column(name="results_weight")
    private BigDecimal resultsWeight;

  
    @Column(name="competencies_weight")
    private BigDecimal competenciesWeight;

  
}