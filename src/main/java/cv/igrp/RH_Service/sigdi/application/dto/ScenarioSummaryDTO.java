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
public class ScenarioSummaryDTO  {



  private Integer totalActivities ;


  private Integer activitiesViable ;


  private Integer activitiesAtRisk ;


  private Integer activitiesInfeasible ;


  private BigDecimal totalBudgetBefore ;


  private BigDecimal totalBudgetAfter ;


  private BigDecimal totalSaving ;

}
