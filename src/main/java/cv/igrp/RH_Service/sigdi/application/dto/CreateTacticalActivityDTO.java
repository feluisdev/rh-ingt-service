/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME */

package cv.igrp.RH_Service.sigdi.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

@IgrpDTO
public class CreateTacticalActivityDTO {

  @NotBlank(message = "The field <strategicGoalId> is required")
  private String strategicGoalId;

  @NotBlank(message = "The field <organicUnitId> is required")
  private String organicUnitId;

  @NotBlank(message = "The field <title> is required")
  private String title;

  private String descriptionWhat;
  private String justificationWhy;
  private String locationWhere;
  private String responsibleWho;
  private String methodologyHow;

  @NotNull(message = "The field <startDate> is required")
  private LocalDate startDate;

  @NotNull(message = "The field <endDate> is required")
  private LocalDate endDate;

  private BigDecimal budgetEstimated;
  private String economicClassifier;
}

