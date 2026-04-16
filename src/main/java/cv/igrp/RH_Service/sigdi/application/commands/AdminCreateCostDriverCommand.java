package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.framework.core.domain.Command;
import cv.igrp.RH_Service.sigdi.application.dto.AdminCreateCostDriverRequestDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCreateCostDriverCommand implements Command {

  private AdminCreateCostDriverRequestDTO body;

}
