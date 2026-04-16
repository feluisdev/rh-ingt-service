package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.framework.core.domain.Command;
import cv.igrp.RH_Service.sigdi.application.dto.CreateDelegationRequestDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDelegationCommand implements Command {

  private String delegatorUserId;
  private CreateDelegationRequestDTO body;

}
