package cv.igrp.RH_Service.funcionarios.application.commands;

import cv.igrp.framework.core.domain.Command;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import cv.igrp.RH_Service.funcionarios.application.dto.ContratoRequestDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateContratoCommand implements Command {

  
  private ContratoRequestDTO contratorequest;
  @NotBlank(message = "The field <funcionarioId> is required.")
  private String funcionarioId;

}