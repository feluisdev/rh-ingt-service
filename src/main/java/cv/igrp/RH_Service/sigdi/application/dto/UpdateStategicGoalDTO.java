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

  @Size(min = 5, message = "O campo <title> deve ter pelo menos 5 caracteres")
	@Size(max = 100, message = "O campo <title> não pode ter mais de 100 caracteres")
  
  private String title ;
  @Size(min = 1, message = "O campo <description> deve ter pelo menos 1 caractere")
	@Size(max = 500, message = "O campo <description> não pode ter mais de 500 caracteres")
  
  private String description ;
  
  private BigDecimal weight ;

  @Min(value = 2000, message = "O campo <year> não pode ser inferior a 2000")
  @Max(value = 2100, message = "O campo <year> não pode ser superior a 2100")
  private Integer year ;

  private java.util.List<StrategicIndicatorDTO> indicators = new java.util.ArrayList<>();

}