/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME */

package cv.igrp.RH_Service.sigdi.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor


@IgrpDTO
public class CreateOkrDTO  {

  @NotBlank(message = "The field <strategicGoalId> is required")

  private String strategicGoalId ;
  @NotBlank(message = "The field <title> is required")

  private String title ;
  @NotBlank(message = "The field <cycle> is required")

  private String cycle ;


  @NotEmpty(message = "The field <keyResults> must not be empty")
  private List<OkrKeyResultRequestDTO> keyResults = new ArrayList<>();

}