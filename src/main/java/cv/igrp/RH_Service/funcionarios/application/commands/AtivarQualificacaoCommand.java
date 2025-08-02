package cv.igrp.RH_Service.funcionarios.application.commands;

import cv.igrp.framework.core.domain.Command;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtivarQualificacaoCommand implements Command {

  @NotBlank(message = "The field <qualificacaoId> is required")
  private String qualificacaoId;

}