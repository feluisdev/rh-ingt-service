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
public class CreateInstitutionRequestDTO  {

  @NotBlank(message = "O campo <code> é obrigatório")

  private String code ;
  @NotBlank(message = "O campo <name> é obrigatório")

  private String name ;
  @NotBlank(message = "O campo <type> é obrigatório")

  private String type ;


  private String contactEmail ;

}
