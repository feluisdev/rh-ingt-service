package cv.igrp.RH_Service.funcionarios.application.commands;

import cv.igrp.framework.core.domain.Command;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import cv.igrp.RH_Service.funcionarios.application.dto.TipoDocumentoRequestDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTipoDocumentoCommand implements Command {

  
  private TipoDocumentoRequestDTO tipodocumentorequest;
  @NotBlank(message = "The field <tipoDocumentoId> is required")
  private String tipoDocumentoId;

}