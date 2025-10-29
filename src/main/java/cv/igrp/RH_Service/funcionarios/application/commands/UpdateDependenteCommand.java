package cv.igrp.RH_Service.funcionarios.application.commands;

import cv.igrp.framework.core.domain.Command;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import cv.igrp.RH_Service.funcionarios.application.dto.DependenteRequestDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDependenteCommand implements Command {

  
  private DependenteRequestDTO dependenterequest;
  @NotBlank(message = "The field <dependenteId> is required")
  private String dependenteId;

}