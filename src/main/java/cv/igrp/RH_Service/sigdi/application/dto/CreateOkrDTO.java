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

  @NotBlank(message = "O campo <strategicGoalId> é obrigatório")

  private String strategicGoalId ;
  @NotBlank(message = "O campo <title> é obrigatório")

  private String title ;
  @NotBlank(message = "O campo <cycle> é obrigatório")

  private String cycle ;


  @NotEmpty(message = "O campo <keyResults> não pode estar vazio")
  private List<OkrKeyResultRequestDTO> keyResults = new ArrayList<>();

}