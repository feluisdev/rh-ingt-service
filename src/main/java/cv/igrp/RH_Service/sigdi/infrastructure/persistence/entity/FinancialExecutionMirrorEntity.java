/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME */

package cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity;

import cv.igrp.RH_Service.shared.config.AuditEntity;
import cv.igrp.framework.stereotype.IgrpEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@Setter
@IgrpEntity
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_financial_execution_mirror",
  uniqueConstraints = {
    @UniqueConstraint(
      name = "uq_fin_exec_classifier_unit_year",
      columnNames = {
        "classifier","organic_unit","fiscal_year"
      }
    )
  })
public class FinancialExecutionMirrorEntity extends AuditEntity {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

  
    @NotBlank(message = "classifier is mandatory")
    @Column(name="classifier", nullable = false)
    private String classifier;

  
    @NotBlank(message = "organicUnit is mandatory")
    @Column(name="organic_unit", nullable = false)
    private String organicUnit;

  
    @NotNull(message = "fiscalYear is mandatory")
    @Column(name="fiscal_year", nullable = false)
    private Integer fiscalYear;

  
    @Column(name="amount_committed")
    private BigDecimal amountCommitted;

  
    @Column(name="amount_liquidated")
    private BigDecimal amountLiquidated;

  
    @Column(name="amount_paid")
    private BigDecimal amountPaid;

  
    @Column(name="last_sync")
    private LocalDateTime lastSync;

  
}