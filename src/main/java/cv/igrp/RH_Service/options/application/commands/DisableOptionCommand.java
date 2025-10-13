package cv.igrp.RH_Service.options.application.commands;

import cv.igrp.framework.core.domain.Command;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisableOptionCommand implements Command {

  @NotBlank(message = "The field <optionId> is required")
  private String optionId;

}