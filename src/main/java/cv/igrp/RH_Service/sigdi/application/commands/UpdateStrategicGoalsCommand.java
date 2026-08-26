package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.framework.core.domain.Command;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import cv.igrp.RH_Service.sigdi.application.dto.UpdateStategicGoalDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStrategicGoalsCommand implements Command {

  
  private UpdateStategicGoalDTO updatestategicgoal;
  @NotBlank(message = "O campo <id> é obrigatório")
  private String id;

}