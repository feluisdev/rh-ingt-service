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
public class OkrKeyResultRequestDTO  {

  @NotBlank(message = "The field <title> is required")

  private String title ;
  @NotNull(message = "The field <targetValue> is required")

  private BigDecimal targetValue ;
  @NotBlank(message = "The field <unit> is required")

  private String unit ;
  @NotNull(message = "The field <weight> is required")

  private BigDecimal weight ;

}