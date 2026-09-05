package cv.igrp.RH_Service.estrutura.application.commands;

import cv.igrp.RH_Service.estrutura.application.dto.PositionRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdatePositionCommand implements Command {
    private final PositionRequestDTO request;
    private final String positionId;
}
