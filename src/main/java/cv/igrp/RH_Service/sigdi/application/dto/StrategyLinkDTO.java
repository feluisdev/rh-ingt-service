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

  @NotBlank(message = "The field <sourceGoalId> is required")
  private String sourceGoalId;

  @NotBlank(message = "The field <targetGoalId> is required")
  private String targetGoalId;

  @NotBlank(message = "The field <relationshipType> is required")
  private String relationshipType;
}

