package cv.igrp.RH_Service.funcionarios.application.commands;

import cv.igrp.framework.core.domain.Command;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import cv.igrp.RH_Service.funcionarios.application.dto.QualificacaoRequestDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateQualificacaoCommand implements Command {

  
  private QualificacaoRequestDTO qualificacaorequest;
  @NotBlank(message = "The field <funcionarioId> is required")
  private String funcionarioId;

}