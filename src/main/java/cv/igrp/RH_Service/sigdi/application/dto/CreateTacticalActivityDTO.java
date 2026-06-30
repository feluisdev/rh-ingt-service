/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME */

package cv.igrp.RH_Service.sigdi.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
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
public class CreateTacticalActivityDTO  {

  @NotNull(message = "The field <strategicGoalId> is required")
  
  private UUID strategicGoalId ;
  @NotNull(message = "The field <organicUnitId> is required")
  
  private UUID organicUnitId ;
  @NotBlank(message = "The field <title> is required")
	@Size(min = 5, message = "The field length <title> must be at least 5 characters")
	@Size(max = 200, message = "The field length <title> cannot be more than 200 characters")
  
  private String title ;
  @Size(min = 1, message = "The field length <descriptionWhat> must be at least 1 characters")
	@Size(max = 2000, message = "The field length <descriptionWhat> cannot be more than 2000 characters")
  
  private String descriptionWhat ;
  @Size(min = 1, message = "The field length <justificationWhy> must be at least 1 characters")
	@Size(max = 2000, message = "The field length <justificationWhy> cannot be more than 2000 characters")
  
  private String justificationWhy ;
  @Size(min = 1, message = "The field length <locationWhere> must be at least 1 characters")
	@Size(max = 2000, message = "The field length <locationWhere> cannot be more than 2000 characters")
  
  private String locationWhere ;
  private UUID responsibleWho ;
  @Size(min = 1, message = "The field length <methodologyHow> must be at least 1 characters")
	@Size(max = 2000, message = "The field length <methodologyHow> cannot be more than 2000 characters")
  
  private String methodologyHow ;
  @NotNull(message = "The field <startDate> is required")
  
  private LocalDate startDate ;
  @NotNull(message = "The field <endDate> is required")
  
  private LocalDate endDate ;
  private BigDecimal budgetEstimated ;
  private String economicClassifier ;

  // PAA Level: UNIT_LEVEL (default) or INDIVIDUAL_LEVEL
  private String paaLevel;

}