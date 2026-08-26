/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME */

package cv.igrp.RH_Service.sigdi.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

@IgrpDTO
public class StrategyLinkDTO {

  @NotBlank(message = "O campo <sourceGoalId> é obrigatório")
  private String sourceGoalId;

  @NotBlank(message = "O campo <targetGoalId> é obrigatório")
  private String targetGoalId;

  @NotBlank(message = "O campo <relationshipType> é obrigatório")
  private String relationshipType;
}

