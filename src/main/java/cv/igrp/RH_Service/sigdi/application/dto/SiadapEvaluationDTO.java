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
public class SiadapEvaluationDTO  {



  private String id ;


  private String employeeId ;


  private String organicUnitId ;


  private String organicUnitName ;


  private String year ;


  private BigDecimal objectivesScore ;


  private BigDecimal competenciesScore ;


  private BigDecimal finalScore ;


  private String meritRating ;


  private Boolean quotaValidated ;


  private String status ;


  private String lastUpdatedAt ;

}
