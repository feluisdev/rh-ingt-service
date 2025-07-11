package cv.igrp.RH_Service.contratos.application.commands.commands;

import cv.igrp.framework.core.domain.Command;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class InativarContratoCommand implements Command {

  @NotBlank(message = "The field <contratoId> is required.")
  private String contratoId;

}