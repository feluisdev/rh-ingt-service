package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.framework.core.domain.Command;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import cv.igrp.RH_Service.sigdi.application.dto.CostDriverRequestDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCostDriverCommand implements Command {

  
  private CostDriverRequestDTO costdriverrequest;
  @NotBlank(message = "O campo <id> é obrigatório")
  private String id;

}