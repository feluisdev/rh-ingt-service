/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME */

package cv.igrp.RH_Service.sigdi.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class WorkflowInboxItemDTO {

  private UUID id;
  private String title;
  private String currentStatus;
  private BigDecimal budgetEstimated;
  private String economicClassifier;
  private String type;
  private String requestedBy;
  private LocalDate requestedDate;
  private Integer pendingSinceDays;
}
