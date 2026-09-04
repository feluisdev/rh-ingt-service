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

  @NotBlank(message = "O campo <title> é obrigatório")

  private String title ;
  @NotNull(message = "O campo <targetValue> é obrigatório")

  private BigDecimal targetValue ;
  @NotBlank(message = "O campo <unit> é obrigatório")

  private String unit ;
  @NotNull(message = "O campo <weight> é obrigatório")

  private BigDecimal weight ;

}