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
public class UpdateStategicGoalDTO  {

  @Size(min = 5, message = "The field length <title> must be at least 5 characters")
	@Size(max = 100, message = "The field length <title> cannot be more than 100 characters")
  
  private String title ;
  @Size(min = 1, message = "The field length <description> must be at least 1 characters")
	@Size(max = 500, message = "The field length <description> cannot be more than 500 characters")
  
  private String description ;
  
  private BigDecimal weight ;

  private java.util.List<StrategicIndicatorDTO> indicators = new java.util.ArrayList<>();

}