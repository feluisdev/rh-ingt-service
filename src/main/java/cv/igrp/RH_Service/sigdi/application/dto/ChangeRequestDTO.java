/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME */

package cv.igrp.RH_Service.sigdi.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class ChangeRequestDTO {

  @NotBlank(message = "O campo <fieldName> é obrigatório")
  private String fieldName;

  private String currentValue;

  private String proposedValue;

  @NotBlank(message = "O campo <justification> é obrigatório")
  @Size(min = 50, message = "O campo <justification> deve ter pelo menos 50 caracteres")
  @Size(max = 1000, message = "O campo <justification> não pode ter mais de 1000 caracteres")
  private String justification;
}
