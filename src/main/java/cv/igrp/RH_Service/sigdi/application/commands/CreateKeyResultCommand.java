package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.framework.core.domain.Command;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import cv.igrp.RH_Service.sigdi.application.dto.KeyResultRequestDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateKeyResultCommand implements Command {

  
  private KeyResultRequestDTO keyresultrequest;

}