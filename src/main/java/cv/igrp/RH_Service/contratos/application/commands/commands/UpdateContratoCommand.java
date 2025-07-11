package cv.igrp.RH_Service.contratos.application.commands.commands;

import cv.igrp.framework.core.domain.Command;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import cv.igrp.RH_Service.contratos.application.dto.ContratoRequestDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateContratoCommand implements Command {

  
  private ContratoRequestDTO contratorequest;

}