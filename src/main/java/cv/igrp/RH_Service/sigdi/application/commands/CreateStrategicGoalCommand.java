package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.framework.core.domain.Command;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import cv.igrp.RH_Service.sigdi.application.dto.CreateStategicGoalDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateStrategicGoalCommand implements Command {

  
  private CreateStategicGoalDTO createstategicgoal;

}