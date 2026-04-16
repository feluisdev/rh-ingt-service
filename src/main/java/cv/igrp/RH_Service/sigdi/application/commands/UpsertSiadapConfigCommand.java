package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.framework.core.domain.Command;
import cv.igrp.RH_Service.sigdi.application.dto.SiadapConfigRequestDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpsertSiadapConfigCommand implements Command {

  private Integer year;
  private SiadapConfigRequestDTO body;

}
