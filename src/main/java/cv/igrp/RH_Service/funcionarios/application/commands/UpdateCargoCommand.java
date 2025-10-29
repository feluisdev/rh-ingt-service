package cv.igrp.RH_Service.funcionarios.application.commands;

import cv.igrp.framework.core.domain.Command;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import cv.igrp.RH_Service.funcionarios.application.dto.CargoRequestDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCargoCommand implements Command {

  
  private CargoRequestDTO cargorequest;
  @NotBlank(message = "The field <cargoId> is required")
  private String cargoId;

}