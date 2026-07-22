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
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor


@IgrpDTO
public class StategicGoalSumaryDTO  {

  
  
  private UUID id ;
  
  
  private String title ;
  
  
  private String perspective ;
  
  
  private BigDecimal weight ;
  
  
  private String status ;
  
  
  private String statusDes ;
  
  
  private Double progress ;


  private Integer linkedActivities ;

  private Integer year ;

}