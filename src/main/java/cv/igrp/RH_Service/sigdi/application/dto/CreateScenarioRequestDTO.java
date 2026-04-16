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
public class CreateScenarioRequestDTO  {

  @NotBlank(message = "The field <name> is required")

  private String name ;
  @NotBlank(message = "The field <type> is required")

  private String type ;
  @NotNull(message = "The field <percentage> is required")

  private BigDecimal percentage ;
  @NotBlank(message = "The field <scope> is required")

  private String scope ;
  @NotNull(message = "The field <fiscalYear> is required")

  private Integer fiscalYear ;


  private String priorityCriteria ;


  private Boolean excludeObligatory ;

}
