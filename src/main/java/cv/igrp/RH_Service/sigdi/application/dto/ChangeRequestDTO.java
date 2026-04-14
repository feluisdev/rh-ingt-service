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

  @NotBlank(message = "The field <fieldName> is required")
  private String fieldName;

  private String currentValue;

  private String proposedValue;

  @NotBlank(message = "The field <justification> is required")
  @Size(min = 50, message = "The field length <justification> must be at least 50 characters")
  @Size(max = 1000, message = "The field length <justification> cannot be more than 1000 characters")
  private String justification;
}
