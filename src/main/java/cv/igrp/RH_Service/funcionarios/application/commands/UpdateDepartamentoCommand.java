package cv.igrp.RH_Service.funcionarios.application.commands;

import cv.igrp.framework.core.domain.Command;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import cv.igrp.RH_Service.funcionarios.application.dto.DepartamentoRequestDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDepartamentoCommand implements Command {

  
  private DepartamentoRequestDTO departamentorequest;
  @NotBlank(message = "The field <departamentoId> is required.")
  private String departamentoId;

}