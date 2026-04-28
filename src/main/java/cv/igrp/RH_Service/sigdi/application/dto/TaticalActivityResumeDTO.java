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
public class TaticalActivityResumeDTO  {

  
  
  private UUID id ;
  
  
  private UUID strategicGoalId ;
  
  
  private String title ;
  
  
  private String responsible_who ;

  private String responsibleName ;

  private String organicUnitId ;

  private String organicUnitName ;
  
  
  private BigDecimal budget_estimated ;
  
  
  private String start_date ;
  
  
  private String end_date ;
  
  
  private String status ;
  
  
  private String statusDesc ;

}