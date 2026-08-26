package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.sigdi.application.dto.ChangeRequestDTO;
import cv.igrp.framework.core.domain.Command;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateChangeRequestCommand implements Command {

  private ChangeRequestDTO changerequest;

  @NotBlank(message = "O campo <activityId> é obrigatório")
  private String activityId;
}
