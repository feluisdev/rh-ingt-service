/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME */

package cv.igrp.RH_Service.sigdi.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor


@IgrpDTO
public class GoalPositionResponseDTO  {

  @NotBlank(message = "O campo <goalId> é obrigatório")

  private String goalId ;
  @NotNull(message = "O campo <x> é obrigatório")

  private Double x ;
  @NotNull(message = "O campo <y> é obrigatório")

  private Double y ;

}