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

  @NotBlank(message = "O campo <name> é obrigatório")

  private String name ;
  @NotBlank(message = "O campo <type> é obrigatório")

  private String type ;
  @NotNull(message = "O campo <percentage> é obrigatório")

  private BigDecimal percentage ;
  @NotBlank(message = "O campo <scope> é obrigatório")

  private String scope ;
  @NotNull(message = "O campo <fiscalYear> é obrigatório")

  private Integer fiscalYear ;


  private String priorityCriteria ;


  private Boolean excludeObligatory ;


  private String targetId ;

}
