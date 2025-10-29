package cv.igrp.RH_Service.funcionarios.application.commands;

import cv.igrp.framework.core.domain.Command;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import cv.igrp.RH_Service.funcionarios.application.dto.DocumentoRequestDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDocumentoCommand implements Command {

  
  private DocumentoRequestDTO documentorequest;
  @NotBlank(message = "The field <DocumentoId> is required.")
  private String documentoId;

}