package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.sigdi.application.dto.GoalPositionRequestDTO;
import cv.igrp.framework.core.domain.Command;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGoalPositionCommand implements Command {

  private GoalPositionRequestDTO updategoalposition;

  @NotBlank(message = "The field <goalId> is required")
  private String goalId;

}