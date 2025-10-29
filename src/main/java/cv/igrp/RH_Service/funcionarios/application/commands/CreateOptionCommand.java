package cv.igrp.RH_Service.funcionarios.application.commands;

import cv.igrp.framework.core.domain.Command;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import cv.igrp.RH_Service.funcionarios.application.dto.OptionRequestDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOptionCommand implements Command {

  
  private OptionRequestDTO optionrequest;

}