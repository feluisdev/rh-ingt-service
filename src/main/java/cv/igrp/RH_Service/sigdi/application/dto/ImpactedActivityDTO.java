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

@Data
@NoArgsConstructor
@AllArgsConstructor


@IgrpDTO
public class ImpactedActivityDTO  {



  private String activityId ;


  private String activityTitle ;


  private String organicUnitName ;


  private String strategicGoalTitle ;


  private BigDecimal currentBudget ;


  private BigDecimal simulatedBudget ;


  private BigDecimal reduction ;


  private String viability ;


  private String suggestedAction ;


  private String actionReason ;


  private BigDecimal priorityScore ;

}
