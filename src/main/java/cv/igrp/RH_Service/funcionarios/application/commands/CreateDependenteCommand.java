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
public class CreateDependenteCommand implements Command {

  
  private DependenteRequestDTO dependenterequest;
  @NotBlank(message = "The field <funcionarioId> is required.")
  private String funcionarioId;

}