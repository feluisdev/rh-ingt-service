package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.framework.core.domain.Command;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import cv.igrp.RH_Service.sigdi.application.dto.CreateTacticalActivityDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTacticalActivityCommand implements Command {

  private CreateTacticalActivityDTO tacticalActivity;
  
  @NotBlank(message = "The field <id> is required")
  private String id;

}
